package com.safechat.safechat.user;

import java.security.Key;
import java.util.ArrayList;

public class ServerUser extends User {
    private Key publicKey;
    private ArrayList<Friend> friendList;
    public ServerUser(String id, Key publicKey) {
        super(id);
        this.publicKey = publicKey;
        this.friendList = new ArrayList<>();
    }

    public Key getPublicKey() {
        return publicKey;
    }

    public void addFriend(Friend friend) {
        friendList.add(friend);
    }

    public ArrayList<Friend> getFriendList() {
        return friendList;
    }
}
