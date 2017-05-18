package com.safechat.safechat.message;

/**
 * Created by Sephiroth on 17/5/14.
 */

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
