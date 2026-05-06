package com.gzy.pestdetectionsystem.utils;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;

/**
 * SM2密钥对生成与管理
 * 支持文件持久化：启动时从文件加载密钥，重启后密钥不丢失
 */
@Slf4j
@Component
public class Sm2KeyManager {

    private AsymmetricCipherKeyPair keyPair;
    private ECDomainParameters domainParams;
    private static final String CURVE_NAME = "sm2p256v1";
    private static final String PRIVATE_KEY_FILE = "sm2-private.key";
    private static final String PUBLIC_KEY_FILE = "sm2-public.key";

    @Value("${sm2.key-path:./keys}")
    private String keyPath;

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        X9ECParameters ecParams = GMNamedCurves.getByName(CURVE_NAME);
        this.domainParams = new ECDomainParameters(
                ecParams.getCurve(), ecParams.getG(), ecParams.getN(), ecParams.getH()
        );

        // 尝试从文件加载密钥，若不存在则生成并保存
        if (!loadKeys()) {
            generateAndSaveKeys();
        }

        log.info("SM2密钥加载成功，公钥: {}...", getPublicKeyHex().substring(0, 16));
    }

    /**
     * 从文件加载密钥
     * @return 加载成功返回true，文件不存在返回false
     */
    private boolean loadKeys() {
        Path privateKeyPath = Paths.get(keyPath, PRIVATE_KEY_FILE);
        Path publicKeyPath = Paths.get(keyPath, PUBLIC_KEY_FILE);

        if (!Files.exists(privateKeyPath) || !Files.exists(publicKeyPath)) {
            log.info("SM2密钥文件不存在，将重新生成并持久化到: {}", keyPath);
            return false;
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(privateKeyPath.toFile()))) {
            // 读取私钥D值
            byte[] privateKeyBytes = new byte[32];
            dis.readFully(privateKeyBytes);
            ECPrivateKeyParameters privateKeyParams = new ECPrivateKeyParameters(
                    new java.math.BigInteger(1, privateKeyBytes), domainParams);

            // 读取公钥
            byte[] publicKeyBytes;
            try (DataInputStream disPub = new DataInputStream(new FileInputStream(publicKeyPath.toFile()))) {
                publicKeyBytes = new byte[64];
                disPub.readFully(publicKeyBytes);
            }
            ECPublicKeyParameters publicKeyParams = new ECPublicKeyParameters(
                    domainParams.getCurve().decodePoint(publicKeyBytes), domainParams);

            this.keyPair = new AsymmetricCipherKeyPair(publicKeyParams, privateKeyParams);
            log.info("已从文件加载SM2密钥");
            return true;
        } catch (Exception e) {
            log.warn("加载SM2密钥文件失败，将重新生成: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 生成新密钥对并持久化到文件
     */
    private void generateAndSaveKeys() {
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(new ECKeyGenerationParameters(domainParams, new SecureRandom()));
        this.keyPair = generator.generateKeyPair();

        try {
            // 确保目录存在
            Path keyDir = Paths.get(keyPath);
            Files.createDirectories(keyDir);

            // 保存私钥（D值，32字节）
            ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
            byte[] privateKeyD = privateKey.getD().toByteArray();
            // 去掉可能的符号位，保留最后32字节
            if (privateKeyD.length > 32) {
                privateKeyD = java.util.Arrays.copyOfRange(privateKeyD, privateKeyD.length - 32, privateKeyD.length);
            }
            try (DataOutputStream dos = new DataOutputStream(
                    new FileOutputStream(Paths.get(keyPath, PRIVATE_KEY_FILE).toFile()))) {
                dos.write(privateKeyD);
            }

            // 保存公钥（未压缩格式，64字节）
            ECPublicKeyParameters publicKey = (ECPublicKeyParameters) keyPair.getPublic();
            byte[] publicKeyBytes = publicKey.getQ().getEncoded(false);
            // 去掉"04"前缀
            byte[] publicKeyQ = java.util.Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length);
            try (DataOutputStream dos = new DataOutputStream(
                    new FileOutputStream(Paths.get(keyPath, PUBLIC_KEY_FILE).toFile()))) {
                dos.write(publicKeyQ);
            }

            log.info("SM2密钥已持久化到: {}", keyPath);
        } catch (IOException e) {
            throw new RuntimeException("SM2密钥持久化失败", e);
        }
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
