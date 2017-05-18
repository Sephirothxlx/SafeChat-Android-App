package com.safechat.safechat.message;

/**
 * Created by Sephiroth on 17/5/18.
 */

import com.safechat.safechat.message.AbstractMessage.Type;

import java.security.Key;

public class AcceptEstablishChatResponse extends AbstractMessage{
    private String receiverId;
    private Key kcs;
    public AcceptEstablishChatResponse(byte[] cipherTimeStamp, String receiverId,Key kcs) {
        super(Type.ACCEPT_ESTABLISH_CHAT_RESPONSE, cipherTimeStamp);
        this.receiverId = receiverId;
        this.kcs=kcs;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public Key getKcs(){
        return kcs;
    }
}
