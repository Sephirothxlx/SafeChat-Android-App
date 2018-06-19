package com.safechat.safechat.message;

import com.safechat.safechat.message.AbstractMessage.Type;

public class RejectEstablishChatResponse extends AbstractMessage{
	private String receiverId;
    public RejectEstablishChatResponse(byte[] cipherTimeStamp, String receiverId) {
        super(Type.REJECT_ESTABLISH_CHAT_RESPONSE, cipherTimeStamp);
        this.receiverId = receiverId;
    }

    public String getReceiverId() {
        return receiverId;
    }
}
