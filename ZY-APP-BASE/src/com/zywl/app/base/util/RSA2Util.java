package com.zywl.app.base.util;

import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.asymmetric.SignAlgorithm;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA2工具
 *
 * @author wfeil211@foxmail.com
 */
public class RSA2Util {

    // 算法类别
    private final static String SIGN_TYPE = "RSA";
    // 算法位数
    private final static Integer KEY_SIZE = 2048;

    /**
     * 生成公私钥
     */
    public Map<String, String> getPublicPrivateKey() {
        Map<String, String> pubPriKey = new HashMap<>();
        KeyPair keyPair = KeyUtil.generateKeyPair(SIGN_TYPE, KEY_SIZE);
        String publicKeyStr = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKeyStr = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        pubPriKey.put("publicKey", publicKeyStr);
        pubPriKey.put("privateKey", privateKeyStr);
        return pubPriKey;
    }

    /**
     * 签名
     */
    public static String sign256(String signData, String priKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(priKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(SIGN_TYPE);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            Signature si = Signature.getInstance(SignAlgorithm.SHA256withRSA.getValue());
            si.initSign(privateKey);
            si.update(signData.getBytes());
            byte[] sign = si.sign();
            return Base64.getEncoder().encodeToString(sign);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 验签
     */
    public static boolean verify256(byte[] dataBytes, String sign, String pubkey) {
        boolean flag = false;
        try {
            byte[] signByte = Base64.getDecoder().decode(sign);
            byte[] encodedKey = Base64.getDecoder().decode(pubkey);
            Signature verf = Signature.getInstance(SignAlgorithm.SHA256withRSA.getValue());
            KeyFactory keyFac = KeyFactory.getInstance(SIGN_TYPE);
            PublicKey puk = keyFac.generatePublic(new X509EncodedKeySpec(encodedKey));
            verf.initVerify(puk);
            verf.update(dataBytes);
            flag = verf.verify(signByte);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return flag;
    }
}
