package com.flint.si.security;

import java.io.Serializable;

public class Credentials implements Serializable {
    byte[] password;
    String username;

    private static final long serialVersionUID = 100L;

    public Credentials(String username, byte[] password) {
        this.username = username;
        this.password = password;
    }

    public byte[] getPassword() {
        return password;
    }

    public String getUsername(){
        return username;
    }

    public void destroy(){
        username = null;
        for(int i = 0; i<this.password.length; i++){
            this.password[i] = 0;
        }
    }
}