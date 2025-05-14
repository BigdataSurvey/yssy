package com.zywl.app.base.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AES128Util {
    private static final Integer ZERO = 0;
    private static final Integer MAC = 16;
    private static final Integer DEFAULT_GCM_LV = 12;
    private static final Integer SECRET_LENGTH_128 = 128;
    private static final String AES = "AES";
    private static final String ALGORITHM = "AES/GCM/PKCS5Padding";
    private static final String UTF8= "utf-8";

    /**
     * 转换16进制字符串
     **/
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /***
     * @version 1.0 aes-128-gcm 加密
     * @params content 为加密信息 secretKey 为32位的16进制key
     * @return 返回base64编码
     **/
    public static String encryptByAES128Gcm(String content, String secretKey) {
        try {
            SecretKey key = new SecretKeySpec(parseHexStr2Byte(secretKey), AES);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] iv = cipher.getIV();//oracle jdk default 12
            byte[] encryptData = cipher.doFinal(content.getBytes(UTF8));
            byte[] message = new byte[DEFAULT_GCM_LV + content.getBytes(UTF8).length + MAC];
            System.arraycopy(iv, ZERO, message, ZERO, DEFAULT_GCM_LV);
            System.arraycopy(encryptData, ZERO, message, DEFAULT_GCM_LV, encryptData.length);
            return Base64.getEncoder().encodeToString(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /***
     * @version 1.0 aes-128-gcm 解密
     * @return  返回解密字符串
     */
    public static String decryptByAES128Gcm(String content, String secretKey) throws Exception {
        SecretKey key = new SecretKeySpec(parseHexStr2Byte(secretKey), AES);
        byte[] message = Base64.getDecoder().decode(content);
        Cipher cipher= Cipher.getInstance(ALGORITHM );
        GCMParameterSpec params = new GCMParameterSpec(SECRET_LENGTH_128, message, ZERO, DEFAULT_GCM_LV);
        cipher.init(Cipher.DECRYPT_MODE, key, params);
        byte[]  decryptData = cipher.doFinal(message, DEFAULT_GCM_LV, message.length - DEFAULT_GCM_LV);
        return new String(decryptData);
    }

    public static final String ENCODE_ALGORITHM = "SHA-256";

    public static String done(String str) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(ENCODE_ALGORITHM);
            messageDigest.update(str.getBytes());
            byte[] outputDigest_sign = messageDigest.digest();
            return bytesToHexString(outputDigest_sign);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

}
