package com.safechat.safechat.message;

public class AcceptFriendResponse extends AbstractMessage {
    private String senderID;
    private String receiverID;

    public AcceptFriendResponse(byte[] cipherTimeStamp, String senderID, String receiverID) {
        super(Type.ACCEPT_FRIEND_RESPONSE, cipherTimeStamp);
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
