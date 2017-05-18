package com.safechat.safechat.message;

import java.security.Key;

public class RegisterRequest extends AbstractMessage {
    private String senderID;
    private byte[] senderPublicKey;
    public RegisterRequest(byte[] cipherTimeStamp, String senderID, byte[] senderPublicKey) {
        super(Type.REGISTER_REQUEST, cipherTimeStamp);
        this.senderID = senderID;
        this.senderPublicKey = senderPublicKey;
    }

    public String getSenderID() {
        return senderID;
    }

    public byte[] getSenderPublicKey() {
        return senderPublicKey;
    }
}
