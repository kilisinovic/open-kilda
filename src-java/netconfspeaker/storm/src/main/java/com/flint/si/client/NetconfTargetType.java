package com.flint.si.client;

import java.io.Serializable;

public enum NetconfTargetType implements Serializable {
    RUNNING("<running/>"), CANDIDATE("<candidate/>");
    String target;
    private static final long serialVersionUID = 100L;

    NetconfTargetType(String target){
        this.target = target;
    }

    @Override
    public String toString() {
        return target;
    }
}
