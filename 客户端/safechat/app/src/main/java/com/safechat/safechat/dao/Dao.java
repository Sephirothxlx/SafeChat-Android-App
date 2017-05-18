package com.safechat.safechat.dao;

/**
 * Created by Sephiroth on 17/5/14.
 */

import android.content.Context;

import com.safechat.safechat.user.ClientUser;
import com.safechat.safechat.user.Friend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.util.HashMap;

public class Dao {
    private static final String SERVER_PUBLIC_KEY_FILE = "server/public.key";
    private static Key serverPublicKey;
    public static Key userPrivateKey;
    private static String USER_KEY_PATH_PREFIX = "/user/";

    public static HashMap<Friend, Key> friendSessionKeyHashmap = new HashMap<>();

    public static void init(Context context) {
        try {
            // read server public key
            ObjectInputStream publicKeyInputStream = new ObjectInputStream(context.getAssets().open(SERVER_PUBLIC_KEY_FILE));
            serverPublicKey = (Key) publicKeyInputStream.readObject();
            publicKeyInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("can not load server public key");
            e.printStackTrace();
        }
    }

    public static Key getServerPublicKey() {
        return serverPublicKey;
    }

    public static Key getUserPrivateKey() {
        return userPrivateKey;
    }

    public static void addSessionKey(ClientUser loginUser, String friendId, Key sessionKey) {
        Friend friend = loginUser.getFriendWithId(friendId);
        friendSessionKeyHashmap.put(friend, sessionKey);
    }

    public static Key getSessionKeyWithFriendId(ClientUser loginUser,String friendId) {
        Friend friend = loginUser.getFriendWithId(friendId);
        return friend.getSessionKey();
    }

    public static Key getPrivateKeyWithPath(String privateKeyPath) {
        try {
            File privateKeyFile = new File(privateKeyPath);
            if (privateKeyFile.exists()) {
                // read client private key from file
                ObjectInputStream privateKeyInputStream = new ObjectInputStream(new FileInputStream(privateKeyFile));
                userPrivateKey = (Key) privateKeyInputStream.readObject();
                privateKeyInputStream.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("can not read private key of user");
            e.printStackTrace();
        }
        return userPrivateKey;
    }

    public static void storeUserPrivateKey(Context context,ClientUser user) {
        try {
            File dir = new File(context.getExternalFilesDir(null)+ USER_KEY_PATH_PREFIX);
            File keyFile = new File(context.getExternalFilesDir(null)+ USER_KEY_PATH_PREFIX+user.getID() + ".key");
            if(!dir.exists()){
                dir.mkdir();
            }
            if (!keyFile.exists()) {
                keyFile.createNewFile();
            }
            ObjectOutputStream privateKeyOutputStream = new ObjectOutputStream(new FileOutputStream(keyFile));
            privateKeyOutputStream.writeObject(user.getPrivateKey());
            privateKeyOutputStream.flush();
            privateKeyOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
