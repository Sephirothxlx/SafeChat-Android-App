package com.safechat.safechat.message;

public class AcceptRegisterResponse extends AbstractMessage {
    private String receiverId;
    public AcceptRegisterResponse(byte[] cipherTimeStamp, String receiverId) {
        super(Type.ACCEPT_REGISTER_RESPONSE, cipherTimeStamp);
        this.receiverId = receiverId;
    }

    public String getReceiverId() {
        return receiverId;
    }
}
