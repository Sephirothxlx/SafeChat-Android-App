package com.safechat.safechat.encryptor;

/**
 * Created by Sephiroth on 17/5/14.
 */


import android.util.Base64;

import com.safechat.safechat.message.AbstractMessage;

import org.json.JSONObject;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RSA {
    private static final int RSA_KEY_SIZE = 1024;
    private static final int CIPHER_SEGMENT_LENGTH = 128;
    private static final int PLAIN_SEGMENT_LENGTH = 117;
    private static final long MAX_TIME_DIFF = 50000;


    public static KeyPair geneRSAKeyPair() {
        // generate RSA key pair
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(RSA_KEY_SIZE);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("no such algorithm");
        }
        return null;
    }

    public static byte[] decrypt(byte[] cipherBytes, Key key)  {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(cipherBytes);
            return result;
        }
        catch (Exception e){
            return null;
        }
    }

    public static AbstractMessage decryptMessage(byte[] cipherMessageBytes, Key key) throws Exception {
        AbstractMessage message = null;
        ByteArrayOutputStream plainByteArrayStream = new ByteArrayOutputStream();
        // decrypt the message bytes
        int segmentNum = cipherMessageBytes.length / CIPHER_SEGMENT_LENGTH;
        for (int i = 0; i < segmentNum; i++) {
            byte[] bytes = new byte[CIPHER_SEGMENT_LENGTH];
            for (int j = 0; j < bytes.length; j++) {
                bytes[j] = cipherMessageBytes[i * CIPHER_SEGMENT_LENGTH + j];
            }
            plainByteArrayStream.write(decrypt(bytes, key));
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(plainByteArrayStream.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        message = (AbstractMessage) objectInputStream.readObject();
        byteArrayInputStream.close();
        objectInputStream.close();
        return message;
    }

    public static byte[] encrypt(byte[] plainBytes, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plainBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plainBytes;
    }

    public static byte[] encryptMessage(AbstractMessage message, Key key) {
        byte[] cipherMessageBytes = null;
        try {
            ByteArrayOutputStream plainByteArrayStream = new ByteArrayOutputStream();
            new ObjectOutputStream(plainByteArrayStream).writeObject(message);
            byte[] plainMessageByte = plainByteArrayStream.toByteArray();
            plainByteArrayStream.close();
            ByteArrayOutputStream cipherByteArrayStream = new ByteArrayOutputStream();
            // encrypt the message
            int segmentNum = plainMessageByte.length / PLAIN_SEGMENT_LENGTH;
            int remainder = plainMessageByte.length % PLAIN_SEGMENT_LENGTH;
            for (int i = 0; i < segmentNum; i++) {
                byte[] bytes = new byte[PLAIN_SEGMENT_LENGTH];
                for (int j = 0; j < bytes.length; j++) {
                    bytes[j] = plainMessageByte[i * PLAIN_SEGMENT_LENGTH + j];
                }
                cipherByteArrayStream.write(encrypt(bytes, key));
            }
            if (remainder != 0) {
                byte[] bytes = new byte[remainder];
                for (int k = 0; k < remainder; k++) {
                    bytes[k] = plainMessageByte[(segmentNum) * PLAIN_SEGMENT_LENGTH + k];
                }
                cipherByteArrayStream.write(encrypt(bytes, key));
            }
            cipherMessageBytes = cipherByteArrayStream.toByteArray();
        } catch (Exception e) {
            System.err.println("can not encrypt message");
            e.printStackTrace();
        }
        return cipherMessageBytes;
    }

    public static byte[] getCipherTimeStamp(Key key) {
        long currentTime = System.currentTimeMillis();
        byte[] plainTimeStampBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(currentTime).array();
        return encrypt(plainTimeStampBytes, key);
    }

    public static boolean checkTimeStamp(byte[] cipherTimeStampBytes, Key key) {
        long timeStamp = 0;
        try {

            byte[] plainTimeStampBytes = decrypt(cipherTimeStampBytes, key);
            if(plainTimeStampBytes==null){
                return false;
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
            byteBuffer.put(plainTimeStampBytes, 0, plainTimeStampBytes.length);
            byteBuffer.flip();
            timeStamp = byteBuffer.getLong();
        } catch (Exception e) {
            System.err.println("can not decrypt time stamp");
            e.printStackTrace();
            return false;
        }
        //为什么不同步？？？
        long timeDiff = Math.abs(System.currentTimeMillis() - timeStamp);
        if (timeDiff > MAX_TIME_DIFF) {
            return false;
        } else {
            return true;
        }
    }
}

