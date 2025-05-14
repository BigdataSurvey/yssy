package com.zywl.app.base.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * 和 RSA 相关的工具类。
 * Padding 方式为默认的 OPENSSL_PKCS1_PADDING；
 * RSA 密钥对的 bit 数默认为 1024。
 */
public class RSAUtil {

    /**
     * 加密算法名称
     */
    public static final String ENCRYPTION_ALGORITHM_NAME = "RSA";

    /**
     * RSA 加密算法获取密钥对中的公钥使用的 key
     */
    public static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * RSA 加密算法获取密钥对中的密钥使用的 key
     */
    public static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * RSA 密钥对的 bit 数(密钥对的长度)。
     * 常用 1024、2048，密钥对的 bit 数，越大越安全，但是越大对服务器的消耗越大
     */
    private static int keySize = 1024;

    /**
     * keySize bit 数下的 RSA 密钥对所能够加密的最大明文大小。
     * RSA 算法一次能加密的明文长度与密钥长度(RSA 密钥对的 bit 数)成正比，
     * 默认情况下，Padding 方式为 OPENSSL_PKCS1_PADDING，RSA 算法会使
     * 用 11 字节的长度用于填充，所以默认情况下，RSA 所能够加密的最大明文大
     * 小为 (keySize / 8 - 11) byte
     */
    private static int maxEncryptPlainTextLen = 117;

    /**
     * keySize bit 数下的 RSA 密钥对所能够解密的最大密文大小。
     * (keySize / 8) byte
     */
    private static int maxDecryptCipherTextLen = 128;

    private RSAUtil() {
    }

    /**
     * 获取 RSA 密钥对的 bit 数
     *
     * @return RSA 密钥对的 bit 数
     */
    public static int getKeySize() {
        return keySize;
    }

    /**
     * 获取 keySize bit 数下的 RSA 密钥对所能够加密的最大明文大小。
     *
     * @return keySize bit 数下的 RSA 密钥对所能够加密的最大明文大小。
     */
    public static int getMaxEncryptPlainTextLen() {
        return maxEncryptPlainTextLen;
    }

    /**
     * 获取 keySize bit 数下的 RSA 密钥对所能够解密的最大密文大小。
     *
     * @return keySize bit 数下的 RSA 密钥对所能够解密的最大密文大小。
     */
    public static int getMaxDecryptCipherTextLen() {
        return maxDecryptCipherTextLen;
    }

    /**
     * 为 RSA 密钥对的 bit 数赋值，同时重新计算 keySize bit 数下的
     * RSA 密钥对所能够加密的最大明文大小、所能够解密的最大密文大小
     *
     * @param keySize RSA 密钥对的 bit 数
     */
    public static void setKeySize(int keySize) {
        // 为 RSA 密钥对的 bit 数赋值
        RSAUtil.keySize = keySize;
        // 更新 keySize bit 数下的 RSA 密钥对所能够加密的最大明文大小
        maxEncryptPlainTextLen = keySize / 8 - 11;
        // 更新 keySize bit 数下的 RSA 密钥对所能够解密的最大密文大小
        maxDecryptCipherTextLen = keySize / 8;
    }

    /**
     * 生成 RSA 加密算法密钥对
     *
     * @return RSA 加密算法密钥对
     */
    public static Map<String, Object> generateRSAKeyPair() throws NoSuchAlgorithmException {
        // 获取 RSA 加密算法密钥对生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ENCRYPTION_ALGORITHM_NAME);
        // 初始化 RSA 密钥对生成器，设置密钥对的 bit 数
        keyPairGenerator.initialize(keySize);
        // 生成 RSA 密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        // 获取 RSA 密钥对公钥
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        // 获取 RSA 密钥对私钥
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        // 以集合的形式返回 RSA 密钥对
        HashMap<String, Object> keyPairMap = new HashMap<>();
        keyPairMap.put(PUBLIC_KEY, rsaPublicKey);
        keyPairMap.put(PRIVATE_KEY, rsaPrivateKey);
        return keyPairMap;
    }

    /**
     * 生成 RSA 加密算法密钥对
     *
     * @param keySize 默认 1024，密钥对的 bit 数，越大越安全，但是越大对服务器的消耗越大
     * @return RSA 加密算法密钥对
     */
    public static Map<String, Object> generateRSAKeyPair(Integer keySize) throws NoSuchAlgorithmException {
        // 如果传递了 keySize，进行 keySize 的更新
        if (Objects.nonNull(keySize)) {
            setKeySize(keySize);
        }
        return generateRSAKeyPair();
    }

    /**
     * 生成 RSA 加密算法密钥对，其中密钥是以字符串形式进行返回
     *
     * @param keySize RSA 密钥对的 bit 数
     * @return RSA 加密算法密钥对
     */
    public static Map<String, String> generateRSAKeyPairStr(Integer keySize) throws NoSuchAlgorithmException {
        // 生成 RSA 加密算法密钥对
        Map<String, Object> keyPair = generateRSAKeyPair(keySize);
        // 获取字符串形式的公钥
        String publicKeyStr = getPublicKeyStr(keyPair);
        // 获取字符串形式的私钥
        String privateKeyStr = getPrivateKeyStr(keyPair);
        // 以集合的形式返回 RSA 密钥对
        HashMap<String, String> keyPairStrMap = new HashMap<>();
        keyPairStrMap.put(PUBLIC_KEY, publicKeyStr);
        keyPairStrMap.put(PRIVATE_KEY, privateKeyStr);
        return keyPairStrMap;
    }

    /**
     * 生成 RSA 加密算法密钥对，其中密钥是以字符串形式进行返回
     *
     * @return RSA 加密算法密钥对
     */
    public static Map<String, String> generateRSAKeyPairStr() throws NoSuchAlgorithmException {
        return generateRSAKeyPairStr(null);
    }

    /**
     * 将字节数组形式的密钥使用 BASE64 编码为字符串形式的密钥
     *
     * @param key 字节数组形式的密钥
     * @return 字符串形式的密钥
     */
    public static String encryptBASE64(byte[] key) {
        return new String(Base64.getEncoder().encode(key));
    }

    /**
     * 将字符串形式的密钥使用 BASE64 解码为字节数组形式的密钥
     *
     * @param key 字符串形式的密钥
     * @return 字节数组形式的密钥
     */
    public static byte[] decryptBASE64(String key) {
        return Base64.getDecoder().decode(key);
    }

    /**
     * 获取 RSA 密钥对中的公钥
     *
     * @param keyPair RSA 密钥对
     * @return RSA 密钥对中的公钥
     */
    public static PublicKey getPublicKey(Map<String, Object> keyPair) {
        return ((PublicKey) keyPair.get(PUBLIC_KEY));
    }

    /**
     * 获取字符串形式的公钥
     *
     * @param keyPair RSA 密钥对
     * @return 字符串形式的公钥
     */
    public static String getPublicKeyStr(Map<String, Object> keyPair) {
        // 获取 RSA 密钥对中的公钥
        PublicKey publicKey = getPublicKey(keyPair);
        // 将 RSA 公钥使用 BASE64 编码为字符串形式
        return encryptBASE64(publicKey.getEncoded());
    }

    /**
     * 根据 RSA 公钥字符串获取 RSA Java 公钥对象
     *
     * @param publicKeyStr RSA 公钥字符串
     * @return RSA Java 公钥对象
     */
    public static PublicKey getPublicKeyByStr(String publicKeyStr)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // 将字符串形式的公钥使用 BASE64 解码为字节数组形式的公钥
        byte[] publicKeyBytes = decryptBASE64(publicKeyStr);
        // 根据 publicKeyBytes 获取 X.509 证书中的密钥规范
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        // 获取 RSA 加密算法的密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM_NAME);
        // 根据密钥规范，让密钥工厂生成 RSA 公钥
        return keyFactory.generatePublic(x509EncodedKeySpec);
    }

    /**
     * 通过字符串形式的密钥对获取字符串形式的公钥
     *
     * @param keyPairStr 字符串形式的密钥对
     * @return 字符串形式的公钥
     */
    public static String getPublicKeyStrByKeyPairStr(Map<String, String> keyPairStr) {
        return keyPairStr.get(PUBLIC_KEY);
    }

    /**
     * 获取 RSA 密钥对中的私钥
     *
     * @param keyPair RSA 密钥对
     * @return RSA 密钥对中的私钥
     */
    public static PrivateKey getPrivateKey(Map<String, Object> keyPair) {
        return ((PrivateKey) keyPair.get(PRIVATE_KEY));
    }

    /**
     * 获取字符串形式的私钥
     *
     * @param keyPair RSA 密钥对
     * @return 字符串形式的私钥
     */
    public static String getPrivateKeyStr(Map<String, Object> keyPair) {
        // 获取 RSA 密钥对中的私钥
        PrivateKey privateKey = getPrivateKey(keyPair);
        // 将 RSA 私钥使用 BASE64 编码为字符串形式
        return encryptBASE64(privateKey.getEncoded());
    }

    /**
     * 根据 RSA 私钥字符串获取 RSA Java 私钥对象
     *
     * @param privateKeyStr RSA 私钥字符串
     * @return RSA Java 私钥对象
     */
    public static PrivateKey getPrivateKeyByStr(String privateKeyStr)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // 将字符串形式的私钥使用 BASE64 解码为字节数组形式的私钥
        byte[] privateKeyBytes = decryptBASE64(privateKeyStr);
        // 根据 privateKeyBytes 获取密钥规范
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        // 获取 RSA 加密算法的密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM_NAME);
        // 根据密钥规范，让密钥工厂生成 RSA 私钥
        return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
    }

    /**
     * 通过字符串形式的密钥对获取字符串形式的私钥
     *
     * @param keyPairStr 字符串形式的密钥对
     * @return 字符串形式的私钥
     */
    public static String getPrivateKeyStrByKeyPairStr(Map<String, String> keyPairStr) {
        return keyPairStr.get(PRIVATE_KEY);
    }

    /**
     * 根据字符串形式的密钥对获取密钥对象形式的密钥对
     *
     * @param keyPairStr 字符串形式的密钥对
     * @return 密钥对象形式的密钥对
     */
    public static Map<String, Object> getKeyPairByStr(Map<String, String> keyPairStr)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        HashMap<String, Object> keyPair = new HashMap<>();
        // 获取字符串形式的公钥
        keyPair.put(PUBLIC_KEY, getPublicKeyByStr(getPublicKeyStrByKeyPairStr(keyPairStr)));
        // 获取字符串形式的私钥
        keyPair.put(PRIVATE_KEY, getPrivateKeyByStr(getPrivateKeyStrByKeyPairStr(keyPairStr)));
        return keyPair;
    }

    /**
     * 使用指定的密钥(公钥或私钥)进行 RSA 明文加密
     *
     * @param keyPair 密钥对象形式的密钥对
     * @param plainText 明文
     * @param key 从 Map 集合中获取密钥的 key
     * @return 明文加密后的密文字符串
     */
    public static String encrypt(Map<String, Object> keyPair, String plainText, String key)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        // 字节数组输出流，用于拼接存储中间分段加密结果，最终生成明文的完整加密密文
        ByteArrayOutputStream cipherText = null;
        try {
            // 获取要进行加密的明文的字节数组
            byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
            // 明文的字节数组长度
            int plainTextBytesLen = plainTextBytes.length;
            // 计算明文需要分段加密的次数
            int encryptCount = ((int) Math.ceil((plainTextBytesLen * 1.0) / maxEncryptPlainTextLen));
            // 字节数组输出流，用于拼接存储中间分段加密结果，最终生成明文的完整加密密文
            cipherText = new ByteArrayOutputStream();
            // 获取 RSA 加密算法对应的加密器
            Cipher rsaCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_NAME);
            // 初始化加密器，指定加密器的工作模式为加密，以及加密的密钥
            rsaCipher.init(Cipher.ENCRYPT_MODE, (Key) keyPair.get(key));
            // 进行明文的分段加密
            for (int i = 0; i < encryptCount; i++) {
                // 当前明文段距离开始位置的偏移量
                int offSet = i * maxEncryptPlainTextLen;
                // 存储当前段明文加密后的密文
                byte[] cipherTextBytes = null;
                if (offSet + maxEncryptPlainTextLen < plainTextBytesLen) {
                    // 需要进行加密的明文字节数组，偏移量，加密处理长度
                    cipherTextBytes = rsaCipher.doFinal(plainTextBytes, offSet, maxEncryptPlainTextLen);
                } else {
                    cipherTextBytes = rsaCipher.doFinal(plainTextBytes, offSet, plainTextBytesLen - offSet);
                }
                // 存储当前明文段加密后的密文
                cipherText.write(cipherTextBytes, 0, cipherTextBytes.length);
            }
            // 将 RSA 加密后的字节数组形式的密文使用 BASE64 编码转换为字符串形式的密文
            return encryptBASE64(cipherText.toByteArray());
        } finally {
            if (cipherText != null) {
                cipherText.close();
            }
        }
    }

    /**
     * 使用 RSA 公钥对明文进行加密
     *
     * @param keyPair   RSA 密钥对
     * @param plainText 明文
     * @return RSA 公钥加密后的密文
     */
    public static String encryptByPublic(Map<String, Object> keyPair, String plainText)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        return encrypt(keyPair, plainText, RSAUtil.PUBLIC_KEY);
    }

    /**
     * 使用 RSA 私钥对明文进行加密
     *
     * @param keyPair   RSA 密钥对
     * @param plainText 明文
     * @return RSA 私钥加密后的密文
     */
    public static String encryptByPrivate(Map<String, Object> keyPair, String plainText)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        return encrypt(keyPair, plainText, RSAUtil.PRIVATE_KEY);
    }

    /**
     * 使用字符串形式的 RSA 公钥对明文进行加密
     *
     * @param keyPairStr 字符串形式的 RSA 密钥对
     * @param plainText  明文
     * @return RSA 公钥加密后的密文
     */
    public static String encryptByPublicStr(Map<String, String> keyPairStr, String plainText)
            throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            IOException, InvalidKeyException {
        // 根据字符串形式的密钥对获取密钥对象形式的密钥对
        Map<String, Object> keyPair = getKeyPairByStr(keyPairStr);
        return encryptByPublic(keyPair, plainText);
    }

    /**
     * 使用字符串形式的 RSA 公钥对明文进行加密
     *
     * @param publicKeyStr 字符串形式的 RSA 公钥
     * @param plainText  明文
     * @return RSA 公钥加密后的密文
     */
    public static String encryptByPublicStr(String publicKeyStr, String plainText)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeyException {
        // 根据字符串形式的密钥获取密钥对象形式的密钥
        PublicKey publicKey = getPublicKeyByStr(publicKeyStr);
        // 生成只包含公钥的密钥对象形式的密钥对
        HashMap<String, Object> keyPair = new HashMap<>();
        keyPair.put(PUBLIC_KEY, publicKey);
        return encryptByPublic(keyPair, plainText);
    }

    /**
     * 使用字符串形式的 RSA 私钥对明文进行加密
     *
     * @param keyPairStr 字符串形式的 RSA 密钥对
     * @param plainText  明文
     * @return RSA 私钥加密后的密文
     */
    public static String encryptByPrivateStr(Map<String, String> keyPairStr, String plainText)
            throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            IOException, InvalidKeyException {
        // 根据字符串形式的密钥对获取密钥对象形式的密钥对
        Map<String, Object> keyPair = getKeyPairByStr(keyPairStr);
        return encryptByPrivate(keyPair, plainText);
    }

    /**
     * 使用字符串形式的 RSA 私钥对明文进行加密
     *
     * @param privateKeyStr 字符串形式的 RSA 私钥
     * @param plainText  明文
     * @return RSA 私钥加密后的密文
     */
    public static String encryptByPrivateStr(String privateKeyStr, String plainText)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeyException {
        // 根据字符串形式的密钥获取密钥对象形式的密钥
        PrivateKey privateKey = getPrivateKeyByStr(privateKeyStr);
        // 生成只包含密钥对象形式的私钥的密钥对
        HashMap<String, Object> keyPair = new HashMap<>();
        keyPair.put(PRIVATE_KEY, privateKey);
        return encryptByPrivate(keyPair, plainText);
    }

    /**
     * 使用指定的密钥(公钥或私钥)进行 RSA 密文解密
     *
     * @param keyPair 密钥对象形式的密钥对
     * @param cipherText 密文
     * @param key 从 Map 集合中获取密钥的 key
     * @return 密文解密后的明文字符串
     */
    public static String decrypt(Map<String, Object> keyPair, String cipherText, String key)
            throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        // 字节数组输出流，用于拼接存储中间分段解密结果，最终生成密文的完整明文
        ByteArrayOutputStream plainText = null;
        try {
            // 将被 BASE64 编码转为字符串的密文使用 BASE64 编码转换为字节数组形式的密文
            byte[] cipherTextBytes = decryptBASE64(cipherText);
            // 字节数组形式的密文的长度
            int cipherTextBytesLen = cipherTextBytes.length;
            // 计算需要分段解码的次数
            int decryptCount = ((int) Math.ceil((cipherTextBytesLen * 1.0) / maxDecryptCipherTextLen));
            // 字节数组输出流，用于拼接存储中间分段解密结果，最终生成密文的完整明文
            plainText = new ByteArrayOutputStream();
            // 获取 RSA 加密算法对应的加密器
            Cipher rsaCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_NAME);
            // 初始化加密器，指定加密器的工作模式为解密，以及解密的密钥
            rsaCipher.init(Cipher.DECRYPT_MODE, (Key) keyPair.get(key));
            // 进行密文的分段解密
            for (int i = 0; i < decryptCount; i++) {
                // 当前明文段距离开始位置的偏移量
                int offSet = i * maxDecryptCipherTextLen;
                // 存储当前段密文解密后的明文
                byte[] plainTextBytes = null;
                if (offSet + maxDecryptCipherTextLen < cipherTextBytesLen) {
                    // 需要进行解密的密文字节数组，偏移量，解密处理长度
                    plainTextBytes = rsaCipher.doFinal(cipherTextBytes, offSet, maxDecryptCipherTextLen);
                } else {
                    plainTextBytes = rsaCipher.doFinal(cipherTextBytes, offSet, cipherTextBytesLen - offSet);
                }
                // 存储当前密文段解密后的明文
                plainText.write(plainTextBytes, 0, plainTextBytes.length);
            }
            // 将 RSA 解密后的字节数组形式的明文转换为字符串形式的明文
            return plainText.toString();
        } finally {
            if (plainText != null) {
                plainText.close();
            }
        }
    }

    /**
     * 使用 RSA 私钥对密文进行解密
     *
     * @param keyPair 密钥对象形式的密钥对
     * @param cipherText 密文
     * @return 密文解密后的明文字符串
     */
    public static String decryptByPrivate(Map<String, Object> keyPair, String cipherText)
            throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        return decrypt(keyPair, cipherText, RSAUtil.PRIVATE_KEY);
    }

    /**
     * 使用 RSA 公钥对密文进行解密
     *
     * @param keyPair 密钥对象形式的密钥对
     * @param cipherText 密文
     * @return 密文解密后的明文字符串
     */
    public static String decryptByPublic(Map<String, Object> keyPair, String cipherText)
            throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        return decrypt(keyPair, cipherText, RSAUtil.PUBLIC_KEY);
    }

    /**
     * 使用字符串形式的 RSA 私钥对密文进行解密
     *
     * @param keyPairStr 字符串形式的 RSA 密钥对
     * @param cipherText  密文
     * @return RSA 私钥解密后的明文
     */
    public static String decryptByPrivateStr(Map<String, String> keyPairStr, String cipherText)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException {
        // 根据字符串形式的密钥对获取密钥对象形式的密钥对
        Map<String, Object> keyPair = getKeyPairByStr(keyPairStr);
        return decryptByPrivate(keyPair, cipherText);
    }

    /**
     * 使用字符串形式的 RSA 私钥对密文进行解密
     *
     * @param privateKeyStr 字符串形式的 RSA 私钥
     * @param cipherText  密文
     * @return RSA 私钥解密后的明文
     */
    public static String decryptByPrivateStr(String privateKeyStr, String cipherText)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException {
        // 获取对象形式的私钥
        PrivateKey privateKey = getPrivateKeyByStr(privateKeyStr);
        // 只包含私钥的密钥对
        HashMap<String, Object> keyPair = new HashMap<>();
        keyPair.put(PRIVATE_KEY, privateKey);
        return decryptByPrivate(keyPair, cipherText);
    }

    /**
     * 使用字符串形式的 RSA 公钥对密文进行解密
     *
     * @param keyPairStr 字符串形式的 RSA 密钥对
     * @param cipherText  密文
     * @return RSA 公钥解密后的明文
     */
    public static String decryptByPublicStr(Map<String, String> keyPairStr, String cipherText)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException {
        // 根据字符串形式的密钥对获取密钥对象形式的密钥对
        Map<String, Object> keyPair = getKeyPairByStr(keyPairStr);
        return decryptByPublic(keyPair, cipherText);
    }

    /**
     * 使用字符串形式的 RSA 公钥对密文进行解密
     *
     * @param publicKeyStr 字符串形式的 RSA 公钥
     * @param cipherText  密文
     * @return RSA 公钥解密后的明文
     */
    public static String decryptByPublicStr(String publicKeyStr, String cipherText)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException {
        // 获取对象形式的公钥
        PublicKey publicKey = getPublicKeyByStr(publicKeyStr);
        // 只包含公钥的密钥对
        HashMap<String, Object> keyPair = new HashMap<>();
        keyPair.put(PUBLIC_KEY, publicKey);
        return decryptByPublic(keyPair, cipherText);
    }

    public static void main(String[] args) throws Exception {
        // 获取密钥对象形式的密钥对
        Map<String, Object> rsaKeyPair = RSAUtil.generateRSAKeyPair();
        // 密文
        String plainText = "要加密的铭文";
        // 公钥加密，私钥解密
        String cipher = encryptByPublic(rsaKeyPair, plainText);
        String plain = decryptByPrivate(rsaKeyPair, cipher);
        System.out.println(cipher);
        System.out.println(plain);
        // 私钥加密，公钥解密
        cipher = encryptByPrivate(rsaKeyPair, plainText);
        plain = decryptByPublic(rsaKeyPair, cipher);
        System.out.println(cipher);
        System.out.println(plain);
        // 获取字符串形式的密钥对
        Map<String, String> rsaKeyPairStr = RSAUtil.generateRSAKeyPairStr();
        // 公钥加密，私钥解密
        cipher = encryptByPublicStr(rsaKeyPairStr, plainText);
        plain = decryptByPrivateStr(rsaKeyPairStr, cipher);
        System.out.println(cipher);
        System.out.println(plain);
        // 私钥加密，公钥解密
        cipher = encryptByPrivateStr(rsaKeyPairStr, plainText);
        plain = decryptByPublicStr(rsaKeyPairStr, cipher);
        System.out.println(cipher);
        System.out.println(plain);
    }

}

