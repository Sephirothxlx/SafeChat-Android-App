package com.safechat.safechat.message;

public class RejectLoginResponse extends AbstractMessage {
    public RejectLoginResponse(byte[] cipherTimeStamp) {
        super(Type.REJECT_LOGIN_RESPONSE, cipherTimeStamp);
    }
}
