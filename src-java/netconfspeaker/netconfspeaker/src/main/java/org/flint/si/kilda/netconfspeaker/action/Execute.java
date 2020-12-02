package org.flint.si.kilda.netconfspeaker.action;

import org.flint.si.kilda.models.device.NetconfDevice;

import org.xml.sax.SAXException;
import net.juniper.netconf.Device;
import net.juniper.netconf.XML;

import java.io.IOException;

import java.util.Map;
import java.util.List;


public class Execute implements Runnable {
    private NetconfDevice netconfDevice;
    private Device device;
    private String payload;
    private XML response;
    private Map<NetconfDevice, String> editError;
    private List<Exception> exceptions;


    public Execute(NetconfDevice netconfDevice, List<Exception> exceptions, Map<NetconfDevice, String> editError){
        this.netconfDevice = netconfDevice;
        this.device = netconfDevice.getDevice();
        this.exceptions = exceptions;
        this.payload = netconfDevice.getDeviceInfo().getPayload();
        this.editError = editError;
    }

    public void run(){
        try{
            if(!device.isConnected()){ 
                device.connect();
            }
           device.lockConfig();
           response = editConfig(device, payload);
           if(response.toString().contains("error")){
               editError.put(netconfDevice, response.toString());
           }
        }catch(IOException | SAXException e){
            exceptions.add(e);
        }
    }
    
    private XML editConfig(Device device, String payload) throws SAXException, IOException {
        String rpc_payload = "<edit-config>" +
                "<target>" +
                "<candidate/>" +
                "</target>" +
                  payload +
                "</edit-config>";
        XML response = device.executeRPC(rpc_payload);
        return response;
    }
}