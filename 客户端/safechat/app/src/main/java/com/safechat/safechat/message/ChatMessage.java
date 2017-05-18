package com.safechat.safechat.message;

public class ChatMessage extends AbstractMessage {
    private String content;
    public ChatMessage(byte[] cipherTimeStamp, String content) {
        super(Type.CHAT_MESSAGE, cipherTimeStamp);
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
