package com.safechat.safechat.message;

public class RejectFriendResponse extends AbstractMessage {
    private String senderID;
    private String receiverID;
    public RejectFriendResponse(byte[] cipherTimeStamp, String senderID, String receiverID) {
        super(Type.REJECT_FRIEND_RESPONSE, cipherTimeStamp);
        this.senderID = senderID;
        this.receiverID = receiverID;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }
}
