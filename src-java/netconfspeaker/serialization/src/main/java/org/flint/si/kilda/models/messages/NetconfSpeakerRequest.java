package org.flint.si.kilda.models.messages;

import org.flint.si.kilda.models.device.DeviceInfo;

import java.util.List;
import java.io.Serializable;


public class NetconfSpeakerRequest implements Serializable {
        List<DeviceInfo> devices;
        String action;
        int transactionID ;
        private static final long serialVersionUID = 100L;

        public NetconfSpeakerRequest(){}

        public NetconfSpeakerRequest(List<DeviceInfo> devices, String action, int transactionID){
             this.devices = devices;
             this.action = action;
             this.transactionID = transactionID;
        }
        
        public List<DeviceInfo> getDevices(){
            return devices;
        }

        public void  setDevices(List<DeviceInfo> devices){
            this.devices = devices;
        }

        public String getAction(){
            return action;
        }

        public void setAction(String action){
            this.action = action;
        }

        public int getTransactionId(){
            return transactionID;
        }

        public void setTransactionID(int transactionID){
            this.transactionID = transactionID;
        }
}