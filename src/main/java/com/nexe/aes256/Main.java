package com.nexe.aes256;

public class Main {

    public static void main(String[] args) throws Exception {
        String Key = "QrXyE2pitXVzV31fKep4Vr760vo09krVqYVql6RL3ow=";
        String text = "I'm Nitesh Kharose";
        String encodedText = AES256EncryDecry.encrypt(text, Key);
        System.out.println(encodedText);
        String decodedText = AES256EncryDecry.decrypt(encodedText, Key);
        System.out.println(decodedText);

    }
}
