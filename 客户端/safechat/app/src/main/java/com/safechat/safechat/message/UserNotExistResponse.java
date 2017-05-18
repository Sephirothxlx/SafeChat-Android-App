package com.safechat.safechat.message;

/**
 * Created by Sephiroth on 17/5/18.
 */

public class UserNotExistResponse extends AbstractMessage {
    private String receiverId;
    public UserNotExistResponse(byte[] cipherTimeStamp, String receiverId) {
        super(Type.USER_NOT_EXIST_RESPONSE, cipherTimeStamp);
        this.receiverId = receiverId;
    }

    public String getReceiverId() {
        return receiverId;
    }
}

