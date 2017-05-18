package com.safechat.safechat.message;

/**
 * Created by Administrator on 2016/5/24.
 */
public class UserClose extends AbstractMessage {

    public UserClose( byte[] cipherTimeStamp) {
        super(Type.USER_CLOSE, cipherTimeStamp);
    }
}
