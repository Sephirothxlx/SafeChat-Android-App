package com.safechat.safechat.message;

import java.io.Serializable;

public abstract class AbstractMessage implements Serializable {
    private Type type;
    private byte[] cipherTimeStamp;

    public AbstractMessage(Type type, byte[] cipherTimeStamp) {
        this.type = type;
        this.cipherTimeStamp = cipherTimeStamp;
    }

    public enum Type {
        ACCEPT_FRIEND_RESPONSE,
        ACCEPT_LOGIN_RESPONSE,
        ACCEPT_REGISTER_RESPONSE,
        ACCEPT_SESSION_KEY_RESPONSE,
        ACCEPT_ESTABLISH_CHAT_RESPONSE,
        BUY_FILE_MESSAGE,
        BUY_FILE_RESPONSE,
        CHAT_MESSAGE,
        ESTABLISH_CHAT_REQUEST,
        FILE_MESSAGE,
        FILE_KEY_REQUEST,
        FILE_KEY_RESPONSE,
        FILE_KEY_FAIL,
        FORWARD_MESSAGE,
        FRIEND_LIST_RESPONSE,
        FRIEND_LIST_REQUEST,
        FRIEND_REQUEST,
        LOGIN_REQUEST,
        REGISTER_REQUEST,
        REJECT_FRIEND_RESPONSE,
        REJECT_LOGIN_RESPONSE,
        REJECT_REGISTER_RESPONSE,
        REJECT_SESSION_KEY_RESPONSE,
        REJECT_ESTABLISH_CHAT_RESPONSE,
        SEND_SESSION_KEY_REQUEST,
        USER_NOT_EXIST_RESPONSE,
        USER_OFFLINE_RESPONSE,
        USER_CLOSE,
    }

    public Type getType() {
        return type;
    }

    public byte[] getCipherTimeStamp() {
        return cipherTimeStamp;
    }
}
