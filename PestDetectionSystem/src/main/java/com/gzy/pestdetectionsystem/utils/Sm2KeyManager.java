package com.gzy.pestdetectionsystem.utils;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.*;

/**
 * SM2密钥对生成与管理
 * 应用启动时自动生成SM2密钥对，提供公钥hex和私钥解密功能
 */
@Slf4j
@Component
public class Sm2KeyManager {

    private AsymmetricCipherKeyPair keyPair;
    private ECDomainParameters domainParams;
    private static final String CURVE_NAME = "sm2p256v1";

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        X9ECParameters ecParams = GMNamedCurves.getByName(CURVE_NAME);
        this.domainParams = new ECDomainParameters(
                ecParams.getCurve(), ecParams.getG(), ecParams.getN(), ecParams.getH()
        );

        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(new ECKeyGenerationParameters(domainParams, new SecureRandom()));
        this.keyPair = generator.generateKeyPair();

        log.info("SM2密钥对生成成功，公钥: {}...", getPublicKeyHex().substring(0, 16));
    }

    /**
     * 获取十六进制公钥（64字节，不含04前缀）
     * 前端使用此公钥加密密码
     */
    public String getPublicKeyHex() {
        ECPublicKeyParameters pub = (ECPublicKeyParameters) keyPair.getPublic();
        // 返回64字节未压缩公钥（去掉"04"前缀）
        return Hex.toHexString(pub.getQ().getEncoded(false)).substring(2);
    }

    /**
     * 使用私钥解密前端传来的密文
     * @param cipherData SM2加密的密文数据
     * @return 解密后的明文
     */
    public byte[] decrypt(byte[] cipherData) {
        try {
            org.bouncycastle.crypto.engines.SM2Engine engine = new org.bouncycastle.crypto.engines.SM2Engine();
            engine.init(false, new ECPrivateKeyParameters(
                    (ECPrivateKeyParameters) keyPair.getPrivate(),
                    domainParams));
            return engine.processBlock(cipherData, 0, cipherData.length);
        } catch (Exception e) {
            throw new RuntimeException("SM2解密失败", e);
        }
    }

    /**
     * 获取私钥参数（供Sm2Util使用）
     */
    public ECPrivateKeyParameters getPrivateKeyParams() {
        return (ECPrivateKeyParameters) keyPair.getPrivate();
    }

    /**
     * 获取域名参数（供Sm2Util使用）
     */
    public ECDomainParameters getDomainParams() {
        return domainParams;
    }
}
