package com.application.anongps;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryptor extends CryptoHandler implements Serializable {
    private String uuid;

    public Encryptor() {
        generateKeyAndIV();
    } //called when no keys are saved in Preferences

    public Encryptor(String key, String iv){
        byte[] keyBytes = hexStringToBytes(key);
        byte[] ivBytes = hexStringToBytes(iv);
        secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        ivParameterSpec = new IvParameterSpec(ivBytes);
    }

    private void generateKeyAndIV() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(AES_KEY_SIZE);
            secretKey = keyGenerator.generateKey();

            SecureRandom secureRandom = new SecureRandom();
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
            ivParameterSpec = new IvParameterSpec(iv);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return bytesToHexString(encryptedBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException |
                 BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getKey() {
        return bytesToHexString(secretKey.getEncoded());
    }

    public String getIV() {
        return bytesToHexString(ivParameterSpec.getIV());
    }


    public void genUUID(){
        uuid = UUID.randomUUID().toString().replace("-", "");
    }

    public String getUuid() {
        return uuid;
    }

    public boolean hasUuid(){
        return (uuid != null);
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    //required methods because ivParameterSpec and SecretKey are not Serializable
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        byte[] keyBytes = secretKey.getEncoded();
        byte[] ivBytes = ivParameterSpec.getIV();
        out.writeObject(keyBytes);
        out.writeObject(ivBytes);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        byte[] keyBytes = (byte[]) in.readObject();
        byte[] ivBytes = (byte[]) in.readObject();
        secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        ivParameterSpec = new IvParameterSpec(ivBytes);
    }

}
