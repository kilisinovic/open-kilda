package com.flint.si.client;

import java.util.Properties;

import com.flint.si.context.EnvironmentContext;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaProducerFactory {
    public static final String KILDA_RESPONSE_PRODUCER = "kilda-response";

	public static Object getInstance(String producerType) {
        if (producerType == KILDA_RESPONSE_PRODUCER){
            return getKildaResponseProducer();
        }
        return null;
    }

    private static KafkaProducer<String, String> getKildaResponseProducer() {
        EnvironmentContext envContext = EnvironmentContext.getInstance();

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, envContext.getProperty("kafka.bootstrapServers"));
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        return producer;
    }
}
