package com.safechat.safechat.user;

import java.io.Serializable;
import java.security.Key;

public class Friend extends User implements Serializable{
    private Key publicKey;
    private Key sessionKey;
    public Friend(String id, Key publicKey) {
        super(id);
        this.publicKey = publicKey;
    }

    public Key getPublicKey() {
        return publicKey;
    }

    public Key getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(Key sessionKey) {
        this.sessionKey = sessionKey;
    }

    @Override
    public String toString() {
        return getID();
    }
}
