package org.flint.si.kilda.models.device;

import net.juniper.netconf.Device;

import org.flint.si.kilda.models.device.DeviceInfo;

import java.io.Serializable;

public class NetconfDevice implements Serializable {
    DeviceInfo deviceInfo;
    Device device;
    private static final long serialVersionUID = 100L;

    public NetconfDevice(DeviceInfo deviceInfo, Device device)  {
        this.deviceInfo = deviceInfo;
        this.device = device;
    }

    public DeviceInfo getDeviceInfo(){
        return deviceInfo;
    }

    public void setDeviceInfo(){
        this.deviceInfo = deviceInfo;
    }

    public Device getDevice(){
        return device;
    }

    public void setDevice(){
        this.device = device;
    }
}