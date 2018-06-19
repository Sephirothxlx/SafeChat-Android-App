package com.safechat.safechat.dao;

import com.safechat.safechat.encryptor.RSA;
import com.safechat.safechat.server.Server;
import java.io.*;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class ServerDao {
    private static final String SERVER_PUBLIC_KEY_PATH = "res/server/public.key";
    private static final String SERVER_PRIVATE_KEY_PATH = "res/server/private.key";

    private static Key publicKey;
    private static Key privateKey;
    private static ArrayList<Server> onlineUserThreadList;

    public static void init() {
        onlineUserThreadList = new ArrayList<>();
        try {
            File publicKeyFile = new File(SERVER_PUBLIC_KEY_PATH);
            File privateKeyFile = new File(SERVER_PRIVATE_KEY_PATH);
            if (publicKeyFile.exists() && privateKeyFile.exists()) {
                ObjectInputStream publicKeyInputStream = new ObjectInputStream(new FileInputStream(publicKeyFile));
                ObjectInputStream privateKeyInputStream = new ObjectInputStream(new FileInputStream(privateKeyFile));
                publicKey = (PublicKey) publicKeyInputStream.readObject();
                privateKey = (PrivateKey) privateKeyInputStream.readObject();
                publicKeyInputStream.close();
                privateKeyInputStream.close();
            } else {
                KeyPair keyPair = RSA.geneRSAKeyPair();
                publicKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();
                ObjectOutputStream publicKeyOutputStream = new ObjectOutputStream(new FileOutputStream(publicKeyFile));
                ObjectOutputStream privateKeyOutputStream = new ObjectOutputStream(new FileOutputStream(privateKeyFile));
                publicKeyOutputStream.writeObject(publicKey);
                privateKeyOutputStream.writeObject(privateKey);
                publicKeyOutputStream.close();
                privateKeyOutputStream.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("can not generate key pair for server");
        }
    }

    public static Key getPrivateKey() {
        return privateKey;
    }

    public static void addOnlineUserThread(Server s) {
        onlineUserThreadList.add(s);
        System.out.println(onlineUserThreadList.size());
    }
    
    public static void removeOnlineUserThread(Server s) {
        onlineUserThreadList.remove(s);
        System.out.println(onlineUserThreadList.size());
    }
    
    public static Server getOnlineUserThreadWithId(String userId) {
        for (Server handleAClient: onlineUserThreadList) {
            if (handleAClient.getThreadUser().getID().equals(userId)) {
                return handleAClient;
            }
        }
        return null;
    }
}
