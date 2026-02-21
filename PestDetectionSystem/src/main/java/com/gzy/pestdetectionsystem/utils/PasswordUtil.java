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


}
