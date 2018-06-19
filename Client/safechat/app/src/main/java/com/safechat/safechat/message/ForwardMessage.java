package com.safechat.safechat.message;

/**
 * Created by Sephiroth on 17/5/18.
 */

public class ForwardMessage extends AbstractMessage {
    private String senderID;
    private String receiverID;
    private Datagram datagram;
    private int mode;
    public ForwardMessage(byte[] cipherTimeStamp, String senderID, String receiverID, Datagram datagram,int mode) {
        super(Type.FORWARD_MESSAGE, cipherTimeStamp);
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.datagram = datagram;
        this.mode=mode;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public Datagram getDatagram() {
        return datagram;
    }

    public int getMode(){
        return mode;
    }
}
