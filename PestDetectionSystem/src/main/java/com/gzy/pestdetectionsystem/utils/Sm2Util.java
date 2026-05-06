package com.gzy.pestdetectionsystem.utils;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;

/**
 * SM2加密解密工具类
 * 提供公钥加密和私钥解密功能
 */
public class Sm2Util {

    private static final String CURVE_NAME = "sm2p256v1";

    /**
     * 使用公钥加密（前端调此方法加密密码）
     * @param plaintext 明文字符串
     * @param publicKeyHex 十六进制公钥（64字节，不含04前缀）
     * @return Base64编码的密文
     */
    public static String encrypt(String plaintext, String publicKeyHex) {
        try {
            X9ECParameters ecParams = GMNamedCurves.getByName(CURVE_NAME);
            ECDomainParameters domainParams = new ECDomainParameters(
                    ecParams.getCurve(), ecParams.getG(), ecParams.getN(), ecParams.getH()
            );

            // 将hex公钥转换为ECPublicKeyParameters
            ECPublicKeyParameters pubKey = createPublicKeyFromHex(publicKeyHex, domainParams);

            SM2Engine engine = new SM2Engine();
            engine.init(true, new ParametersWithRandom(pubKey, new java.security.SecureRandom()));

            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] cipher = engine.processBlock(plaintextBytes, 0, plaintextBytes.length);

            return Base64.toBase64String(cipher);
        } catch (Exception e) {
            throw new RuntimeException("SM2加密失败", e);
        }
    }

    /**
     * 使用私钥解密（后端调此方法解密密码）
     * @param ciphertextBase64 Base64编码的密文
     * @param privateKey 私钥参数
     * @param domainParams 曲线参数
     * @return 解密后的明文字符串
     */
    public static String decrypt(String ciphertextBase64, ECPrivateKeyParameters privateKey, ECDomainParameters domainParams) {
        try {
            byte[] cipherData = Base64.decode(ciphertextBase64);

            SM2Engine engine = new SM2Engine();
            engine.init(false, new ECPrivateKeyParameters(privateKey, domainParams));

            byte[] decrypted = engine.processBlock(cipherData, 0, cipherData.length);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM2解密失败", e);
        }
    }

    /**
     * 将十六进制公钥转换为ECPublicKeyParameters
     * @param hex 64字节十六进制公钥（不含04前缀）
     * @param domainParams 曲线参数
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters createPublicKeyFromHex(String hex, ECDomainParameters domainParams) {
        // 在hex前面加上"04"前缀表示未压缩格式
        String fullHex = "04" + hex;
        byte[] pubBytes = Hex.decode(fullHex);
        org.bouncycastle.math.ec.ECPoint point = domainParams.getCurve().decodePoint(pubBytes);
        return new ECPublicKeyParameters(point, domainParams);
    }
}
