package com.gzy.pestdetectionsystem.utils;

import java.security.SecureRandom;



public class PasswordUtil {

    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        StringBuilder hex = new StringBuilder();
        for (byte b : salt) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    
    /**
     * 使用SM3哈希密码（国密版本）
     * @param password 密码
     * @param salt 盐值
     * @return SM3哈希后的十六进制字符串
     */
    public static String encryptPasswordSm3(String password, String salt) {
        return Sm3Util.hash(password, salt);
    }

    /**
     * 验证密码（SM3国密版本）
     * @param password 明文密码
     * @param salt 盐值
     * @param dbPassword 数据库中存储的哈希值
     * @return 是否匹配
     */
    public static boolean verifyPasswordSm3(String password, String salt, String dbPassword) {
        return Sm3Util.verify(password, salt, dbPassword);
    }


}
