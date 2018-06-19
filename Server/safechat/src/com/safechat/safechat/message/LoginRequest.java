package com.safechat.safechat.message;

public class LoginRequest extends AbstractMessage {
    private String senderID;
    public LoginRequest(byte[] cipherTimeStamp, String senderID) {
        super(Type.LOGIN_REQUEST, cipherTimeStamp);
        this.senderID = senderID;
    }

    public String getSenderID() {
        return senderID;
    }
}
