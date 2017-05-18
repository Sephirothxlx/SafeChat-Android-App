package com.safechat.safechat.message;

public class FriendRequest extends AbstractMessage {
    private String senderID;
    private String receiverID;
    public FriendRequest(byte[] cipherTimeStamp, String senderID, String receiverID) {
        super(Type.FRIEND_REQUEST, cipherTimeStamp);
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
