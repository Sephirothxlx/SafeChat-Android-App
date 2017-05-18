package com.safechat.safechat.message;

import java.io.Serializable;

public class Datagram implements Serializable {
    private byte[] cipherMessageBytes;
    private MessageEncryptType messageEncryptType;

    public Datagram(byte[] cipherMessageBytes, MessageEncryptType messageEncryptType) {
        this.cipherMessageBytes = cipherMessageBytes;
        this.messageEncryptType = messageEncryptType;
    }

    public enum MessageEncryptType {
        RSA, AES
    }

    public byte[] getCipherMessageBytes() {
        return cipherMessageBytes;
    }

    public MessageEncryptType getMessageEncryptType() {
        return messageEncryptType;
    }
}
