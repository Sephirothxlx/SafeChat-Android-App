package com.safechat.safechat.message;

public class RejectRegisterResponse extends AbstractMessage {
    private String receiverId;
    public RejectRegisterResponse(byte[] cipherTimeStamp, String receiverId) {
        super(Type.REJECT_REGISTER_RESPONSE, cipherTimeStamp);
        this.receiverId = receiverId;
    }

    public String getReceiverId() {
        return receiverId;
    }
}
