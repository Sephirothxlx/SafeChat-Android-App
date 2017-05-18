package com.safechat.safechat.message;

public class RejectSessionKeyResponse extends AbstractMessage {
    private String senderId;
    public RejectSessionKeyResponse(byte[] cipherTimeStamp, String senderId) {
        super(Type.REJECT_SESSION_KEY_RESPONSE, cipherTimeStamp);
        this.senderId = senderId;
    }

    public String getSenderId() {
        return senderId;
    }
}
