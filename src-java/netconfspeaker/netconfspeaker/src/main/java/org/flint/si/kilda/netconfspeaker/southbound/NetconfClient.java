package org.flint.si.kilda.netconfspeaker.southbound;

import org.flint.si.kilda.models.device.DeviceInfo;
import org.flint.si.kilda.netconfspeaker.southbound.DeviceExecutor;
import org.flint.si.kilda.netconfspeaker.exception.RunTasksException;

import java.util.Map;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfClient {
    private final Logger logger = LoggerFactory.getLogger(NetconfClient.class);

    public NetconfClient(){}

    public Map<String,String> get(List<DeviceInfo> devices) throws RunTasksException {
        DeviceExecutor deviceTasks = new DeviceExecutor(devices);
        Map<String, String> getResponses;
        try{
             // Create devices
             logger.info("Creating " + devices.size() + " device(s)...");
             deviceTasks.create();
             logger.info("Finished creating device(s)...");

              // Initialize get config
              getResponses = deviceTasks.getConfig();
              logger.info("All GETCONFIG threads have finished...");
        }catch(RunTasksException e){
            throw e;
        }finally{
            deviceTasks.close();
        }
        return getResponses;
    }

    public String execute(List<DeviceInfo> devices) throws RunTasksException {
        DeviceExecutor deviceTasks = new DeviceExecutor(devices);
        String response = "";
        try{
             // Create devices
             logger.info("Creating " + devices.size() + " device(s)...");
             deviceTasks.create();
             logger.info("Finished creating device(s)...");

             // Initialize edit config and check responses
             deviceTasks.execute();
             logger.info("All EXECUTE threads have finished...");
 
             // Check for device session errors
             // If its ok proceed with commiting to RUNNING
             if (deviceTasks.getExceptions().isEmpty()) {
                 logger.info("NO errors have been found...");
                 deviceTasks.commit();
                 logger.info("All COMMIT threads have finished...");
                 response = "All devices COMMIT(s) were successful..!!!";
            }
        }catch(RunTasksException e){
            throw e;
        }finally{
            deviceTasks.close();
        }
        return response;
    }
}
