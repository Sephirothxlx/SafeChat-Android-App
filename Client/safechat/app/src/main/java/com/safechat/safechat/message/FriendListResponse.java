package com.safechat.safechat.message;

import com.safechat.safechat.user.Friend;

import java.util.ArrayList;

public class FriendListResponse extends AbstractMessage {
    private ArrayList<Friend> friendList;
    public FriendListResponse(byte[] cipherTimeStamp, ArrayList<Friend> friendList) {
        super(Type.FRIEND_LIST_RESPONSE, cipherTimeStamp);
        this.friendList = friendList;
    }

    public ArrayList<Friend> getFriendList() {
        return friendList;
    }
}
