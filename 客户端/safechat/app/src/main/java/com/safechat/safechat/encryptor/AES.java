package com.safechat.safechat.encryptor;

/**
 * Created by Sephiroth on 17/5/14.
 */

import com.safechat.safechat.message.AbstractMessage;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AES {
    private static final int AES_KEY_SIZE = 128;
    private static final long MAX_TIME_DIFF = 50000;

    public static Key geneAESKey() {
        // generate AES key
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            return new SecretKeySpec(enCodeFormat, "AES");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("no such algorithm");
        }
        return null;
    }



    public static byte[] decrypt(byte[] cipherBytes, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherBytes);
    }

    public static AbstractMessage decryptMessage(byte[] cipherMessageBytes, Key key) throws Exception{
        AbstractMessage message = null;
        byte[] plainMessageBytes = decrypt(cipherMessageBytes, key);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(plainMessageBytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        message = (AbstractMessage) objectInputStream.readObject();
        byteArrayInputStream.close();
        objectInputStream.close();
        return message;
    }

    public static byte[] encrypt(byte[] plainBytes, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plainBytes);
        } catch (Exception e) {
            System.err.println("can not encrypt");
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
            // encrypt the message
            cipherMessageBytes = encrypt(plainMessageByte, key);
        } catch (IOException e) {
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
            ByteBuffer byteBuffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
            byteBuffer.put(plainTimeStampBytes, 0, plainTimeStampBytes.length);
            byteBuffer.flip();//need flip
            timeStamp = byteBuffer.getLong();
        } catch (Exception e) {
            System.err.println("can not decrypt time stamp");
            e.printStackTrace();
        }
        long timeDiff = Math.abs(System.currentTimeMillis() - timeStamp);
        if (timeDiff > MAX_TIME_DIFF) {
            return false;
        } else {
            return true;
        }
    }
}
