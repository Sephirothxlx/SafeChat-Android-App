package com.safechat.safechat.message;

/**
 * Created by Sephiroth on 17/5/18.
 */

import com.safechat.safechat.message.AbstractMessage.Type;

public class EstablishChatRequest extends AbstractMessage{
    private String senderID;
    public EstablishChatRequest(byte[] cipherTimeStamp, String senderID) {
        super(Type.ESTABLISH_CHAT_REQUEST, cipherTimeStamp);
        this.senderID = senderID;
    }

    public String getSenderID() {
        return senderID;
    }
}