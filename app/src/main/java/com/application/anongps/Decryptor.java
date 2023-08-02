package com.application.anongps;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Decryptor extends CryptoHandler{
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_MODE = "AES/CBC/PKCS5Padding";

    private SecretKey secretKey;
    private IvParameterSpec ivParameterSpec;

    public Decryptor(String key, String iv) {
        byte[] keyBytes = hexStringToBytes(key);
        byte[] ivBytes = hexStringToBytes(iv);
        secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        ivParameterSpec = new IvParameterSpec(ivBytes);
    }

    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            byte[] decryptedBytes = cipher.doFinal(hexStringToBytes(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException |
                 BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
