package com.safechat.safechat.entity;

/**
 * Created by Sephiroth on 17/5/14.
 */

import android.app.Application;

import com.safechat.safechat.dao.Dao;
import com.safechat.safechat.encryptor.AES;
import com.safechat.safechat.encryptor.FileUtil;
import com.safechat.safechat.encryptor.RSA;
import com.safechat.safechat.message.*;
import com.safechat.safechat.user.ClientUser;

import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;

public class Client extends Application {
    public ClientUser loginUser;
    public Key kcs;
    public final String HOST = "192.168.1.104";
    public final int PORT = 8000;
    public Socket socket;

    public void initDao() {
        Dao.init(getApplicationContext());
    }

    public void initSocket() {
        if (socket == null || socket.isClosed()) {
            try {
                socket = new Socket(HOST, PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeSocket() {
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void saveUser(ClientUser user) {
        loginUser = user;
    }

    public ClientUser getUser() {
        return loginUser;
    }

    public Key getKcs() {
        return kcs;
    }

    public void setKcs(Key kcs) {
        this.kcs = kcs;
    }

    public AbstractMessage getMessage() {
        AbstractMessage message = null;
        boolean timeStampValid = false;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Datagram datagram = (Datagram) objectInputStream.readObject();
            if (datagram.getMessageEncryptType() == Datagram.MessageEncryptType.RSA) {
                message = RSA.decryptMessage(datagram.getCipherMessageBytes(), getUser().getPrivateKey());
                timeStampValid = RSA.checkTimeStamp(message.getCipherTimeStamp(), Dao.getServerPublicKey());
            } else {
                message = AES.decryptMessage(datagram.getCipherMessageBytes(), getKcs());
                timeStampValid = AES.checkTimeStamp(message.getCipherTimeStamp(), getKcs());
            }
        } catch (Exception e) {
e.printStackTrace();
        }
        if (timeStampValid) {
            return message;
        }
        return null;
    }

    public void sendMessage(AbstractMessage message, Key key, Datagram.MessageEncryptType messageEncryptType) {
        byte[] cipherMessageBytes;
        if (messageEncryptType == Datagram.MessageEncryptType.RSA) {
            cipherMessageBytes = RSA.encryptMessage(message, key);
        } else {
            cipherMessageBytes = AES.encryptMessage(message, key);
        }
        Datagram datagram = new Datagram(cipherMessageBytes, messageEncryptType);
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(datagram);
            objectOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void register(String userId) {
        KeyPair keyPair = RSA.geneRSAKeyPair();
        ClientUser user = new ClientUser(userId, keyPair.getPrivate());
        saveUser(user);
        AbstractMessage message = new RegisterRequest(RSA.getCipherTimeStamp(keyPair.getPrivate()), userId, keyPair.getPublic().getEncoded());
        sendMessage(message, Dao.getServerPublicKey(), Datagram.MessageEncryptType.RSA);
    }

    public void login(String userId, String privateKeyPath) {
        Key privateKey = Dao.getPrivateKeyWithPath(privateKeyPath);
        AbstractMessage message = new LoginRequest(RSA.getCipherTimeStamp(privateKey), userId);
        ClientUser user = new ClientUser(userId, privateKey);
        saveUser(user);
        sendMessage(message, Dao.getServerPublicKey(), Datagram.MessageEncryptType.RSA);
    }

    public void askFriendListRequest() {
        if (getKcs() != null && getUser() != null) {
            AbstractMessage message = new FriendListRequest(AES.getCipherTimeStamp(getKcs()), getUser().getID());
            sendMessage(message, getKcs(), Datagram.MessageEncryptType.AES);
        }
    }

    public void friend(String friendID) {
        if (getKcs() != null && getUser() != null) {
            AbstractMessage message = new FriendRequest(AES.getCipherTimeStamp(getKcs()), getUser().getID(), friendID);
            sendMessage(message, getKcs(), Datagram.MessageEncryptType.AES);
        }
    }

    public void acceptFriend(String askerID) {
        if (getKcs() != null && getUser() != null) {
            AbstractMessage message = new AcceptFriendResponse(AES.getCipherTimeStamp(getKcs()), getUser().getID(), askerID);
            sendMessage(message, getKcs(), Datagram.MessageEncryptType.AES);
        }
    }

    public void rejectFriend(String askerID) {
        if (getKcs() != null && getUser() != null) {
            AbstractMessage message = new RejectFriendResponse(AES.getCipherTimeStamp(getKcs()), getUser().getID(), askerID);
            sendMessage(message, getKcs(), Datagram.MessageEncryptType.AES);
        }
    }

    public void establishChatSocket(Socket s) {
        AbstractMessage message = new EstablishChatRequest(RSA.getCipherTimeStamp(Dao.getUserPrivateKey()), getUser().getID());
        byte[] cipherMessageBytes = RSA.encryptMessage(message, Dao.getServerPublicKey());
        Datagram datagram = new Datagram(cipherMessageBytes, Datagram.MessageEncryptType.AES);
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(s.getOutputStream());
            objectOutputStream.writeObject(datagram);
            objectOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chat(Socket s,Key kcs,String receiverId, String content) {
        if (getUser() != null) {
            Key sessionKey = getUser().getFriendWithId(receiverId).getSessionKey();
            AbstractMessage subMessage = new ChatMessage(AES.getCipherTimeStamp(sessionKey), content);
            byte[]innerBytes = AES.encryptMessage(subMessage, sessionKey);
            Datagram innerDatagram= new Datagram(innerBytes, Datagram.MessageEncryptType.AES);
            AbstractMessage message = new ForwardMessage(AES.getCipherTimeStamp(kcs), getUser().getID(), receiverId, innerDatagram, 1);
            byte[] outBytes = AES.encryptMessage(message, kcs);
            Datagram outDatagram = new Datagram(outBytes, Datagram.MessageEncryptType.AES);
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(s.getOutputStream());
                objectOutputStream.writeObject(outDatagram);
                objectOutputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendSessionKey(String receiverId) {
        if (getKcs() != null && getUser() != null) {
            Key sessionKey = AES.geneAESKey();
            AbstractMessage subMessage = new SendSessionKeyRequest(RSA.getCipherTimeStamp(getUser().getPrivateKey()), sessionKey);
            byte[] cipherMessageBytes;
            getUser().getFriendWithId(receiverId).setSessionKey(sessionKey);
            cipherMessageBytes = RSA.encryptMessage(subMessage, getUser().getFriendWithId(receiverId).getPublicKey());
            Datagram datagram = new Datagram(cipherMessageBytes, Datagram.MessageEncryptType.RSA);
            AbstractMessage message = new ForwardMessage(AES.getCipherTimeStamp(getKcs()), getUser().getID(), receiverId, datagram, 0);
            sendMessage(message, getKcs(), Datagram.MessageEncryptType.AES);
        }
    }

    public void acceptSessionKey(String receiverId) {
        if (getKcs() != null && getUser() != null) {
            AbstractMessage subMessage = new AcceptSessionKeyResponse(RSA.getCipherTimeStamp(getUser().getPrivateKey()));
            byte[] cipherMessageBytes;
            cipherMessageBytes = RSA.encryptMessage(subMessage, getUser().getFriendWithId(receiverId).getPublicKey());
            Datagram datagram = new Datagram(cipherMessageBytes, Datagram.MessageEncryptType.RSA);
            AbstractMessage message = new ForwardMessage(AES.getCipherTimeStamp(getKcs()), getUser().getID(), receiverId, datagram, 0);
            sendMessage(message, getKcs(), Datagram.MessageEncryptType.AES);
        }
    }

    public void rejectSessionKey(String receiverId) {
        if (getKcs() != null && getUser() != null) {
            AbstractMessage subMessage = new RejectSessionKeyResponse(RSA.getCipherTimeStamp(getUser().getPrivateKey()), receiverId);
            byte[] cipherMessageBytes;
            cipherMessageBytes = RSA.encryptMessage(subMessage, getUser().getFriendWithId(receiverId).getPublicKey());
            Datagram datagram = new Datagram(cipherMessageBytes, Datagram.MessageEncryptType.RSA);
            AbstractMessage message = new ForwardMessage(AES.getCipherTimeStamp(getKcs()), getUser().getID(), receiverId, datagram, 0);
            sendMessage(message, getKcs(), Datagram.MessageEncryptType.AES);
        }
    }

    public void sendFile(Socket s,Key kcs,String receiverId, File f) throws NoSuchAlgorithmException, InvalidKeyException {
        if (getUser() != null) {
            Key sessionKey = getUser().getFriendWithId(receiverId).getSessionKey();
            String path = f.getPath();
            int a = path.lastIndexOf('.');
            if (a == -1)
                path = "";
            path = path.substring(a, path.length());
            byte[] fi = FileUtil.file2Byte(f);
            if (fi == null) {
                return;
            } else {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(sessionKey);
                byte[] macCode = mac.doFinal(fi);
                AbstractMessage subMessage = new FileMessage(AES.getCipherTimeStamp(sessionKey), fi, macCode, path);
                byte[] cipherMessageBytes = AES.encryptMessage(subMessage, sessionKey);
                Datagram datagram = new Datagram(cipherMessageBytes, Datagram.MessageEncryptType.AES);
                AbstractMessage message = new ForwardMessage(AES.getCipherTimeStamp(kcs), getUser().getID(), receiverId, datagram, 1);
                byte[] outBytes = AES.encryptMessage(message, kcs);
                Datagram outDatagram = new Datagram(outBytes, Datagram.MessageEncryptType.AES);
                try {
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(s.getOutputStream());
                    objectOutputStream.writeObject(outDatagram);
                    objectOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void close() {
        if (getKcs() != null) {
            AbstractMessage message = new UserClose(AES.getCipherTimeStamp(getKcs()));
            sendMessage(message, getKcs(), Datagram.MessageEncryptType.AES);
        }

    }


}