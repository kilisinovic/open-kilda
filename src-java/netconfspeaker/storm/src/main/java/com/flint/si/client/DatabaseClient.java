package com.flint.si.client;

import java.io.IOException;

import com.flint.si.context.EnvironmentContext;
import com.flint.si.security.CredentialManager;
import com.flint.si.security.Credentials;

import org.xml.sax.SAXException;

import net.juniper.netconf.Device;
import net.juniper.netconf.NetconfException;
import net.juniper.netconf.XML;

public class DatabaseClient {
    private Device db;
    private static DatabaseClient client;

    private DatabaseClient() throws NetconfException{
        try {
            EnvironmentContext envContext = EnvironmentContext.getInstance();
            DeviceInfo deviceInfo = new DeviceInfo(envContext.getProperty("db.address"),
                    Integer.parseInt(envContext.getProperty("db.port")));
            Credentials credentials = CredentialManager.getCredentials(CredentialManager.DATABASE);

            // WARNING
            /*
             * TODO FUTURE DEVELOPMENT: this library defeats all effort of trying to delete
             * password from memory as soon as possible. It stores password as string which
             * should be changed. We can either find more secure lib for this or we can
             * overwrite the lib to store pass as byte array. I vote for second option :)
             * 
             * Also, password should be encoded when it's used in framework and should only
             * be decoded when sending config to device which should happen on
             * netconf-speaker only
             */
            db = Device.builder().hostName(deviceInfo.getHostname()).userName(credentials.getUsername())
                    .password(new String(credentials.getPassword())).port(deviceInfo.getPort())
                    .strictHostKeyChecking(false).build();
        } catch (NetconfException e) {
            db = null;
            throw e;
        }
    }

    public static DatabaseClient getInstance(){
        if (client == null){
            try {
                client = new DatabaseClient();
            } catch(NetconfException e){
                client=null;
            }
        }
        return client;
    }

    public String execute(String command, String xmlPayload, String target, String operation)
            throws IOException, SAXException {

        if (!db.isConnected()) {
            db.connect();
        }

        String response = null;

        switch (command) {
            case "edit-config":
                response = editConfig(db, xmlPayload, target, operation);
                break;
            case "get-config":
                response = getConfig(db, xmlPayload, target, operation);
                break;
            default:
                throw new UnsupportedOperationException("Action " + command + "is currently unsupported.");
        }

        return response;
    }

    private String editConfig(Device device, String xmlPayload, String target, String operation)
            throws IOException, SAXException {
        // Send <edit-config>
        String rpc_payload = "<edit-config>" + "<target>" + getTarget(target) + "</target>" + "<default-operation>"
                + operation + "</default-operation>" + "<config>" + xmlPayload + "</config>" + "</edit-config>";

        XML rpc_reply = device.executeRPC(rpc_payload);

        String reply = rpc_reply.toString();
        return reply;
    }

    private String getConfig(Device device, String xmlPayload, String target, String operation)
            throws IOException, SAXException {
        // Send <get-config>
        XML rpc_reply = device.getRunningConfig(xmlPayload);
        String response = rpc_reply.toString();

        return response;
    }

    private String getTarget(String target) {
        switch (target) {
            case "candidate":
                return "<candidate/>";
            case "running":
            default:
                return "<running/>";
        }
    }

    public void closeConnection() {
        if (db == null || db.isConnected() == false) {
            return;
        }
        db.close();
    }
}
