package com.flint.si.client;

import java.io.Serializable;

public class DeviceInfo implements Serializable {
    String hostname;
    Integer port;
    private static final long serialVersionUID = 100L;

    public DeviceInfo(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname(){return hostname;}

    public void setHostname(String hostname){
        this.hostname = hostname;
    }

    public Integer getPort(){return port;}

    public void setPort(Integer port){this.port = port;}

}