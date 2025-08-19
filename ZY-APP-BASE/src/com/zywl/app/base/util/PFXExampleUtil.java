package com.zywl.app.base.util;

import java.io.FileInputStream;
import java.security.KeyStore;

public class PFXExampleUtil {

    public static KeyStore loadKeyStore(String pfxFilePath, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        FileInputStream inputStream = new FileInputStream(pfxFilePath);
        keyStore.load(inputStream, password.toCharArray());
        inputStream.close();
        return keyStore;
    }
}