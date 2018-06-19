package com.safechat.safechat.message;

/**
 * Created by Sephiroth on 17/5/18.
 */

public class UserOfflineResponse extends AbstractMessage {
    private String receiverId;
    public UserOfflineResponse(byte[] cipherTimeStamp, String receiverId) {
        super(Type.USER_OFFLINE_RESPONSE, cipherTimeStamp);
        this.receiverId = receiverId;
    }

    public String getReceiverId() {
        return receiverId;
    }
}

