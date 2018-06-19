package com.safechat.safechat.user;

import java.io.Serializable;

public abstract class User implements Serializable {
    private String id;

    public User(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

}
