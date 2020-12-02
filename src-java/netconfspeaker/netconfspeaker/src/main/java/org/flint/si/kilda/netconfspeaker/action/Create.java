package org.flint.si.kilda.netconfspeaker.action;

import org.flint.si.kilda.models.device.NetconfDevice;
import org.flint.si.kilda.models.device.DeviceInfo;

import net.juniper.netconf.NetconfException;
import net.juniper.netconf.Device;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Create implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(Create.class);
    private Device device;
    private NetconfDevice netconfDevice;
    private DeviceInfo deviceInfo;
    private List<NetconfDevice> buildedDevices;
    private String payload;
    private String filterTag;
    private List<Exception> exceptions;

    public Create(DeviceInfo deviceInfo, List<NetconfDevice> buildedDevices, List<Exception> exceptions){
        this.deviceInfo = deviceInfo;
        this.buildedDevices = buildedDevices;
        this.exceptions = exceptions;
    }

    public void run() {
        try {
            device = deviceBuild(deviceInfo.getHostname(),deviceInfo.getUsername(), deviceInfo.getPort(), deviceInfo.getPassword());
            netconfDevice = new NetconfDevice(deviceInfo, device);
            logger.info("Created device: " + netconfDevice.getDeviceInfo().getName());
            buildedDevices.add(netconfDevice);
        }catch(IOException e){
            exceptions.add(e);
        }
    }

    public static Device deviceBuild(String hostName, String userName, Integer port, String password) throws NetconfException {
        return Device.builder().hostName(hostName).userName(userName).port(port).password(password).strictHostKeyChecking(false).build();
    }

} 