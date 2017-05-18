package com.safechat.safechat.user;

/**
 * Created by Sephiroth on 17/5/14.
 */

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