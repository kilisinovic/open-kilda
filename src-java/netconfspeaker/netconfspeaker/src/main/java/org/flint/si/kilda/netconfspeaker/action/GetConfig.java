package org.flint.si.kilda.netconfspeaker.action;

import org.flint.si.kilda.models.device.NetconfDevice;

import org.xml.sax.SAXException;
import net.juniper.netconf.Device;
import net.juniper.netconf.XML;

import java.io.IOException;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetConfig implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(GetConfig.class);
    private NetconfDevice netconfDevice;
    private Device device;
    private XML response;
    private List<Exception> exceptions;
    private Map<String, String> getResponses;

    public GetConfig(NetconfDevice netconfDevice, List<Exception> exceptions, Map<String, String> getResponses){
        this.netconfDevice = netconfDevice;
        this.device = netconfDevice.getDevice();
        this.exceptions = exceptions;
        this.getResponses = getResponses;
    }

    public void run(){
        try{
            if(!device.isConnected()){
                device.connect();
            }
            response = getRunningConfiguration(device);            
            getResponses.put(netconfDevice.getDeviceInfo().getName(), response.toString());
        }catch(IOException | SAXException e){
            exceptions.add(e);
        }
    }

    private XML getRunningConfiguration(Device device) throws SAXException, IOException {
        String rpc_payload = "<get-config>" +
                                "<source>" +
                                    "<running/>"  +
                                "</source>" +
                             "</get-config>";

        XML response = device.executeRPC(rpc_payload);
        return response;
    }
}