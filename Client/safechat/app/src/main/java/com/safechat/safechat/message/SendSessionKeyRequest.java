package com.safechat.safechat.message;

import java.security.Key;

public class SendSessionKeyRequest extends AbstractMessage {
    private Key sessionKey;
    public SendSessionKeyRequest(byte[] cipherTimeStamp, Key sessionKey) {
        super(Type.SEND_SESSION_KEY_REQUEST, cipherTimeStamp);
        this.sessionKey = sessionKey;
    }

    public Key getSessionKey() {
        return sessionKey;
    }
}
