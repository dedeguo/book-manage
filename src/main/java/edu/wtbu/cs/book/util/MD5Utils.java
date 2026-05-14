package edu.wtbu.cs.book.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 加密工具类
 */
public class MD5Utils {

    /**
     * MD5 加密
     * @param str 原始字符串
     * @return 加密后的 32 位十六进制字符串
     */
    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                int num = b & 0xff;
                String hex = Integer.toHexString(num);
                if (hex.length() == 1) {
                    sb.append("0");
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verify(String rawPassword, String encodedPassword) {
        return md5(rawPassword).equals(encodedPassword);
    }
}