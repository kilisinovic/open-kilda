package org.flint.si.kilda.models.device;

import java.io.Serializable;


public class DeviceInfo implements Serializable {
    String name;
    String hostname;
    String username;
    Integer port;
    String password;
    String payload;
    private static final long serialVersionUID = 100L;

    public DeviceInfo(String name, String hostname, Integer port, String username, String password, String payload)  {
        this.name = name;
        this.hostname = hostname;
        this.username = username;
        this.port = port;
        this.password = password;
        this.payload = payload;
    }

    public String getName() {
        return name;
    }

    public void setName() {
        this.name = name;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname() {
        this.hostname = hostname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername() {
        this.username = username;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort() {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword() {
        this.password = password;
    }

    public String getPayload(){
        return payload;
    }
    
    public void setPayload(){
        this.payload = payload;
    }
}