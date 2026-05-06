package com.gzy.pestdetectionsystem.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class PasswordUtil {

    public static boolean verifyPassword(String password, String salt, String dbPassword) {
        try{
            MessageDigest md5 = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hashPassword = md5.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashPassword) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().equalsIgnoreCase(dbPassword);
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException("服务器加密算法错误",e);
        }
    }

    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        StringBuilder hex = new StringBuilder();
        for (byte b : salt) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    public static String encryptPassword(String password, String salt) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hashPassword = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b :  hashPassword) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException("服务器加密算法错误", e);
        }
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
