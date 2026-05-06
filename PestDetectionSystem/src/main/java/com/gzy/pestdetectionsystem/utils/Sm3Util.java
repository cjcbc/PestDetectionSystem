package com.gzy.pestdetectionsystem.utils;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

/**
 * SM3哈希工具类
 * 替代SHA-256用于密码哈希
 */
public class Sm3Util {

    /**
     * SM3哈希（data + salt）
     * @param data 数据
     * @param salt 盐值
     * @return 十六进制哈希字符串
     */
    public static String hash(String data, String salt) {
        try {
            SM3Digest digest = new SM3Digest();
            String combined = data + salt;
            byte[] input = combined.getBytes(StandardCharsets.UTF_8);
            digest.update(input, 0, input.length);
            byte[] result = new byte[digest.getDigestSize()];
            digest.doFinal(result, 0);
            return Hex.toHexString(result);
        } catch (Exception e) {
            throw new RuntimeException("SM3哈希计算失败", e);
        }
    }

    /**
     * 验证SM3哈希
     * @param data 数据
     * @param salt 盐值
     * @param expectedHash 期望的哈希值
     * @return 是否匹配
     */
    public static boolean verify(String data, String salt, String expectedHash) {
        return hash(data, salt).equalsIgnoreCase(expectedHash);
    }
}
