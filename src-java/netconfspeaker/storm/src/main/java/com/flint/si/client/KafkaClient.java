package com.flint.si.client;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class KafkaClient<T> {
    KafkaProducer<String, T> producer;
    
    public KafkaClient(KafkaProducer<String, T> producer){
        this.producer = producer;
    }

    public void send(String topic, T value) {

        ProducerRecord<String, T> record = new ProducerRecord<>(topic, value);
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e == null) {
                    System.out.println("Topic: " + recordMetadata.topic() + "\n" + "partition: "
                            + recordMetadata.partition() + "\n" + "offset: " + recordMetadata.offset() + "\n"
                            + "Timestamp: " + recordMetadata.timestamp());
                } else {
                    e.printStackTrace();
                }
            }
        });

        producer.close();
    }

}
