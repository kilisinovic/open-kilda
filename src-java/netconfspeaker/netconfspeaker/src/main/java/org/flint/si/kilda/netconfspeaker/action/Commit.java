package org.flint.si.kilda.netconfspeaker.action;

import org.flint.si.kilda.models.device.NetconfDevice;

import net.juniper.netconf.Device;

import org.xml.sax.SAXException;

import java.io.IOException;

import java.util.List;

public class Commit implements Runnable {
    private NetconfDevice netconfDevice;
    private Device device;
    private List<Exception> exceptions;

    public Commit(NetconfDevice netconfDevice, List<Exception> exceptions){
        this.netconfDevice = netconfDevice;
        this.device = netconfDevice.getDevice();
        this.exceptions = exceptions;
    }

    public void run(){
        try{
            if(!device.isConnected()){
                throw new IOException("Device is not connected!");
            }
            device.commit();
            device.unlockConfig();
            device.close();
       }catch(IOException | SAXException e){
            exceptions.add(e);
      }
    }   
}