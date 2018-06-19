package com.safechat.safechat.message;

public class AcceptSessionKeyResponse extends AbstractMessage {
    public AcceptSessionKeyResponse(byte[] cipherTimeStamp) {
        super(Type.ACCEPT_SESSION_KEY_RESPONSE, cipherTimeStamp);
    }
}
