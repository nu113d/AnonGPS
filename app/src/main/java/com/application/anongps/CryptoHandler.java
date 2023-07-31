package com.application.anongps;

import java.io.Serializable;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class CryptoHandler {
    protected static final String AES_ALGORITHM = "AES";
    protected static final String AES_MODE = "AES/CBC/PKCS5Padding";

    protected SecretKey secretKey;
    protected IvParameterSpec ivParameterSpec;

    protected byte[] hexStringToBytes(String hexString) {
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    protected String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
