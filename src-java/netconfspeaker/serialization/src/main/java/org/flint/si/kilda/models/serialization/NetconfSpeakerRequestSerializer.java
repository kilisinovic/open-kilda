package org.flint.si.kilda.models.serialization;

import org.apache.kafka.common.serialization.Serializer;
import org.flint.si.kilda.models.messages.NetconfSpeakerRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

public class NetconfSpeakerRequestSerializer implements Serializer<NetconfSpeakerRequest> {

    public byte[] serialize(String topic, NetconfSpeakerRequest data) {
        if (data == null) return null;

        byte[] output = new byte[0];
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(data);
            oos.flush();

            output = bos.size() > 0 ? bos.toByteArray() : output;
        } catch (IOException e) {
            // TODO
        }
        return output;
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // TODO Auto-generated method stub
        //Serializer.super.configure(configs, isKey);
    }

}
