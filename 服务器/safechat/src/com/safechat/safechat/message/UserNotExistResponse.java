package com.safechat.safechat.message;

/**
 * Created by Administrator on 2016/5/23.
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
