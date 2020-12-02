package org.flint.si.kilda.netconfspeaker.queue;

import org.flint.si.kilda.models.messages.NetconfSpeakerResponse;

import java.util.Objects;

public class QueueMessage {
    String topic;
    NetconfSpeakerResponse response;

    public QueueMessage(String topic, NetconfSpeakerResponse response) {
        Objects.requireNonNull(topic);
        this.topic = topic;
        this.response = response;
    }

    public String getTopic() {
        return topic;
    }

    public NetconfSpeakerResponse getResponse() {
        return response;
    }
}
