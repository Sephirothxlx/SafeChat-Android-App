package com.safechat.safechat.message;

import java.security.Key;

public class AcceptLoginResponse extends AbstractMessage {
    private Key kcs;

    public AcceptLoginResponse(byte[] cipherTimeStamp, Key kcs) {
        super(Type.ACCEPT_LOGIN_RESPONSE, cipherTimeStamp);
        this.kcs = kcs;
    }

    public Key getKcs() {
        return kcs;
    }
}
