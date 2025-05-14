package com.zywl.app.base.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class AESEncryptUtil {
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String HASH_ALGORITHM = "SHA-256";

    public static String AESEncrypt(String data, String key) {
        try {
            SecretKeySpec secretKeySpec = generateSecretKeySpec(key);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String AESDecrypt(String data, String key) {
        try {
            SecretKeySpec secretKeySpec = generateSecretKeySpec(key);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decodedBytes = Base64.getDecoder().decode(data);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static SecretKeySpec generateSecretKeySpec(String key) throws Exception {
        MessageDigest sha = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] hashedBytes = sha.digest(keyBytes);
        byte[] truncatedBytes = new byte[16];
        System.arraycopy(hashedBytes, 0, truncatedBytes, 0, truncatedBytes.length);
        return new SecretKeySpec(truncatedBytes, SECRET_KEY_ALGORITHM);
    }

}