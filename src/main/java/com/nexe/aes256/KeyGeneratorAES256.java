package com.nexe.aes256;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;

public class KeyGeneratorAES256 {

    private static final KeyGeneratorAES256 INSTANCE = new KeyGeneratorAES256();

    private KeyGeneratorAES256() {
    }

    public static KeyGeneratorAES256 getInstance() {
        return INSTANCE;
    }

    // Generate AES-256 Secret Key
    private SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, SecureRandom.getInstanceStrong());
        return keyGenerator.generateKey();
    }

    // convert key to Base64
    private String toBase64(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static void main(String[] args) throws Exception {


        KeyGeneratorAES256 keyGeneratorAES = KeyGeneratorAES256.getInstance();
        SecretKey secretKey = keyGeneratorAES.generateKey();
        String key = keyGeneratorAES.toBase64(secretKey);
        System.out.println("AES-256 Keys : " + key);

    }
}
