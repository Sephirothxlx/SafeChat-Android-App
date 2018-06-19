package com.safechat.safechat.message;

import java.io.File;

public class FileMessage extends AbstractMessage {
    private byte[] file;
    private byte[] mac;
    private String fileType;
    // TODO
    public FileMessage(byte[] cipherTimeStamp,byte[] f,byte[] m,String fileType) {
        super(Type.FILE_MESSAGE, cipherTimeStamp);
        file=f;
        mac=m;
        this.fileType=fileType;
    }
    public byte[] getFile(){
        return file;
    }
    public byte[] getMac(){
        return mac;
    }
    public String getFileType(){
        return fileType;
    }
}
