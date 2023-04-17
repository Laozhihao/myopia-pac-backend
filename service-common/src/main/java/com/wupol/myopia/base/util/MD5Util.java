package com.wupol.myopia.base.util;

import com.wupol.myopia.base.exception.BusinessException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * TODO:
 *
 * @author Simple4H
 */
public class MD5Util {

    private final static String salt = "vistel";

    // 生成加盐后的MD5值
    public static String generate(String password) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException("获取MD5实例失败");
        }
        messageDigest.update((password + salt).getBytes());
        byte[] digest = messageDigest.digest();
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : digest) {
            stringBuilder.append(String.format("%02x", b & 0xff));
        }
        return stringBuilder.toString();
    }

}
