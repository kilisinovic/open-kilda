package org.flint.si.kilda.models.serialization;


import org.apache.kafka.common.serialization.Deserializer;
import org.flint.si.kilda.models.messages.NetconfSpeakerRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfSpeakerRequestDeserializer implements Deserializer {
    private final Logger logger = LoggerFactory.getLogger(NetconfSpeakerRequestDeserializer.class);
    public NetconfSpeakerRequest deserialize(String topic, byte[] data) {
        NetconfSpeakerRequest message = null;
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bis);
            message = (NetconfSpeakerRequest) ois.readObject();
        } catch (IOException | ClassNotFoundException e){
            logStackTrace(e, "info");
        }
        return message;
    }
    public void logStackTrace(Exception e, String level) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        switch (level) {
            case "info":
                logger.info(sw.toString());
                break;
            case "warn":
                logger.warn(sw.toString());
                break;
            case "debug":
                logger.debug(sw.toString());
                break;
            case "error":
                logger.error(sw.toString());
                break;
            case "trace":
                logger.trace(sw.toString());
                break;
        }
    }
}
