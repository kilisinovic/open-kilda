package org.flint.si.kilda.netconfspeaker.southbound;

import org.flint.si.kilda.netconfspeaker.action.Commit;
import org.flint.si.kilda.netconfspeaker.action.Create;
import org.flint.si.kilda.netconfspeaker.action.Execute;
import org.flint.si.kilda.netconfspeaker.action.GetConfig;

import org.flint.si.kilda.models.device.NetconfDevice;
import org.flint.si.kilda.models.device.DeviceInfo;

import org.flint.si.kilda.netconfspeaker.exception.ExceptionHandlerException;
import org.flint.si.kilda.netconfspeaker.exception.RunTasksException;

import java.util.*;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DeviceExecutor {
    private final Logger logger = LoggerFactory.getLogger(DeviceExecutor.class);
    private List<Future<?>> futures;
    private List<DeviceInfo> devicesInfo;
    private Integer numOfThreads;
    private List<NetconfDevice> buildedDevices;
    private Map<NetconfDevice, String> editError;
    private Map<String, String> getResponses;
    private ExecutorService executor;
    private List<Exception> exceptions;

    public DeviceExecutor(List<DeviceInfo> devicesInfo) {
        this.futures = new ArrayList<Future<?>>();
        this.devicesInfo = devicesInfo;
        this.numOfThreads = devicesInfo.size();
        this.buildedDevices = new ArrayList<NetconfDevice>();
        this.editError = new HashMap<NetconfDevice, String>();
        this.executor = Executors.newFixedThreadPool(numOfThreads);
        this.exceptions = new ArrayList<Exception>();
        this.getResponses = new HashMap<String, String>();
    }

    public List<Exception> getExceptions(){
        return exceptions;
    }

    public void create() throws RunTasksException {
        runTasks("CREATE");
    }

    public Map<String, String> getConfig() throws RunTasksException {
        runTasks("GETCONFIG");
        return getResponses;
    }

    public void execute() throws RunTasksException {
        runTasks("EXECUTE");
    }

    public void commit()throws RunTasksException {
        runTasks("COMMIT");
    }

    private void runTasks(String taskAction) throws RunTasksException {
        for(int i=0; i<numOfThreads; i++) {
            try{
                logger.info("Starting "+taskAction+" thread.." + i);
                switch (taskAction){
                    case "CREATE":
                        Future<?> create = executor.submit(new Create(devicesInfo.get(i), buildedDevices, exceptions));
                        futures.add(create);
                        break;
                    case "GETCONFIG":
                        Future<?> getConfig = executor.submit(new GetConfig(buildedDevices.get(i), exceptions, getResponses));
                        futures.add(getConfig);
                        break;
                    case "EXECUTE":
                        Future<?> execute = executor.submit(new Execute(buildedDevices.get(i), exceptions, editError));
                        futures.add(execute);  
                        break;
                    case "COMMIT":
                        Future<?> commit = executor.submit(new Commit(buildedDevices.get(i), exceptions));
                        futures.add(commit); 
                        break;
                }
            }catch (RejectedExecutionException | NullPointerException e) {
                i = numOfThreads;
                throw new RunTasksException("Error submitting tasks..", e);
            }
        }

        // Wait for all tasks to finish
        for(Future<?> future: futures){
            try{
                future.get(); 
            }catch(InterruptedException | ExecutionException e){
                throw new RunTasksException("Error getting tasks..", e);
            }
        }

        // Check for caught exceptions 
        if(!exceptions.isEmpty() | !editError.isEmpty()){
            try{
                exceptionHandler(exceptions, editError);
            }catch(ExceptionHandlerException e){
                throw new RunTasksException("Error from exceptionHandler..", e);
            }
        }
        futures.removeAll(futures);
   }

   public void close(){
       try{
         for(int i=0; i<numOfThreads; i++){
                logger.info("Closing session for device.." + buildedDevices.get(i).getDeviceInfo().getName());
                buildedDevices.get(i).getDevice().close();
                logger.info("Closed session for device.." + buildedDevices.get(i).getDeviceInfo().getName());
            }    
            executor.shutdown();
            logger.info("All CLOSE SESSIONS threads have finished");
        }catch(Exception e){
            e.printStackTrace();
        }
   } 
   
   private void exceptionHandler(List<Exception> exceptions, Map<NetconfDevice, String> editError) throws ExceptionHandlerException {
        String error = "";
        if(!exceptions.isEmpty()){
            for(Exception e : exceptions) {
                String ex = e.getMessage();
                error += "\n" + ex;
            }
        }
        if(!editError.isEmpty()){
            for(Map.Entry<NetconfDevice, String> entry : editError.entrySet()){
                String response = "\n Response error: "+"\n"+entry.getValue()+" for device: "+ entry.getKey().getDeviceInfo().getName()+"(" + 
                entry.getKey().getDeviceInfo().getHostname()+":"+ entry.getKey().getDeviceInfo().getPort()+")"+"has been found!!!";
                error += "\n" + response;
            }
        }
        throw new ExceptionHandlerException(error);
   }
}

