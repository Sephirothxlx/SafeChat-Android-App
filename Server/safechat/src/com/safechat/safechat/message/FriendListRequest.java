package com.safechat.safechat.message;

public class FriendListRequest extends AbstractMessage {
    private String senderID;
    public FriendListRequest(byte[] cipherTimeStamp, String senderID) {
        super(Type.FRIEND_LIST_REQUEST, cipherTimeStamp);
        this.senderID = senderID;
    }

    public String getSenderID() {
        return senderID;
    }
}
