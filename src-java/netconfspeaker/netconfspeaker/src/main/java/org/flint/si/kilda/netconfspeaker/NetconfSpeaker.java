package org.flint.si.kilda.netconfspeaker;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.flint.si.kilda.models.device.DeviceInfo;
import org.flint.si.kilda.models.messages.NetconfSpeakerRequest;
import org.flint.si.kilda.models.messages.NetconfSpeakerResponse;
import org.flint.si.kilda.netconfspeaker.queue.QueueMessage;
import org.flint.si.kilda.models.serialization.NetconfSpeakerRequestDeserializer;
import org.flint.si.kilda.models.serialization.NetconfSpeakerResponseSerializer;
import org.flint.si.kilda.netconfspeaker.southbound.NetconfClient;
import org.flint.si.kilda.netconfspeaker.exception.RunTasksException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetconfSpeaker {
    private final Logger logger = LoggerFactory.getLogger(NetconfSpeaker.class);
    private BlockingQueue<QueueMessage> queue;
    private Map<String, ConsumerRunnable> consumers;
    private Map<String, ProducerRunnable> producers;
    private Thread[] consumerThreads;
    private Thread[] producerThreads;
    private Configuration config;

    public NetconfSpeaker() {
        config = null;
        Configurations configs = new Configurations();
        try {
            config = configs.properties("config.properties");
        } catch (ConfigurationException e) {
            logger.error("Unable to load properties");
            logStackTrace(e, "error");
            System.exit(-1);
        }

        consumers = new HashMap<>();
        producers = new HashMap<>();

        producerThreads = new Thread[config.getInt("producer.instances.amount")];
        consumerThreads = new Thread[config.getInt("consumer.instances.amount")];

        try {
            queue = new LinkedBlockingQueue<QueueMessage>(config.getInt("internal.queue.size"));
        } catch (NoSuchElementException e) {
            queue = new LinkedBlockingQueue<QueueMessage>();
        }
    }

    public static void main(String[] args) {
        new NetconfSpeaker().run();
    }

    public void run() {
        try{
            initiateConsumers();
            initiateProducers();
        } catch(NoSuchElementException e){
            logger.error("Unable to read properties. Some configuration parameters might be missing.");
            logStackTrace(e, "error");
            System.exit(-1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Activating shutdown hook");
            for (HashMap.Entry<String, ConsumerRunnable> consumerEntry : consumers.entrySet()) {
                consumerEntry.getValue().shutdown();
            }
            for (HashMap.Entry<String, ProducerRunnable> producerEntry : producers.entrySet()) {
                producerEntry.getValue().shutdown();
            }
        }));

        this.startThreads();
        this.joinThreads();
    }

    private void initiateProducers() {
        final String bootstrapServers = config.getString("bootstrap.servers");
        final int numOfProducers = config.getInt("consumer.instances.amount");

        for (int i = 0; i < numOfProducers; i++) {
            String producerId = "producer" + i; // needed better id assignment
            ProducerRunnable producer = new ProducerRunnable(producerId, bootstrapServers, queue);
            producers.put(producerId, producer);
            producerThreads[i] = new Thread(producer);
        }
    }

    private void initiateConsumers() {
        final String bootstrapServers = config.getString("bootstrap.servers");
        final String consumerGroupId = config.getString("consumer.group");
        final String consumerTopic = config.getString("consumer.topic");
        final int numOfConsumers = config.getInt("consumer.instances.amount");

        consumerThreads = new Thread[numOfConsumers];
        for (int i = 0; i < numOfConsumers; i++) {
            String consumerId = "consumer" + i; // needed better id assignment
            ConsumerRunnable consumer = new ConsumerRunnable(consumerId, bootstrapServers, consumerGroupId, consumerTopic, queue);
            consumers.put(consumerId, consumer);
            consumerThreads[i] = new Thread(consumer);
        }
    }

    private void startThreads() {
        for (int i = 0; i < consumerThreads.length; i++) {
            consumerThreads[i].start();
        }
        for (int i = 0; i < producerThreads.length; i++) {
            producerThreads[i].start();
        }
    }

    private void joinThreads() {
        try {
            for (int i = 0; i < consumerThreads.length; i++) {
                consumerThreads[i].join();
            }
            for (int i = 0; i < producerThreads.length; i++) {
                producerThreads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class ConsumerRunnable implements Runnable {
        KafkaConsumer<String, NetconfSpeakerRequest> consumer;
        String groupId;
        String topics;
        String bootstrapServers;
        String id;
        BlockingQueue<QueueMessage> queue;
        NetconfClient netconf;
        QueueMessage producerResponse;

        public ConsumerRunnable(String consumerId, String bootstrapServers, String groupId, String topics, BlockingQueue<QueueMessage> queue) {
            this.bootstrapServers = bootstrapServers;
            this.groupId = groupId;
            this.topics = topics;
            this.id = consumerId;
            this.queue = queue;

            this.netconf = new NetconfClient();

            // configure consumer properties
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, NetconfSpeakerRequestDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            consumer = new KafkaConsumer<String, NetconfSpeakerRequest>(properties);
        }

        @Override
        public void run() {
            consumer.subscribe(Collections.singleton(topics));
            try {
                while (true) {
                    ConsumerRecords<String, NetconfSpeakerRequest> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, NetconfSpeakerRequest> record : records) {
                        if(record.value() != null) {
                            logger.info("Reading value...");
                            NetconfSpeakerRequest netSpeakRequest = record.value();
                            List<DeviceInfo> devices = netSpeakRequest.getDevices();
                            String action = netSpeakRequest.getAction();
                            try{
                                switch(action){
                                    case "get-config":
                                        Map<String, String> getRes = netconf.get(devices);
                                        producerResponse = createQueueMessage(getRes, netSpeakRequest.getTransactionId());
                                        break;
                                    case "edit-config":
                                        String editRes = netconf.execute(devices);
                                        producerResponse = createQueueMessage(editRes, netSpeakRequest.getTransactionId());
                                        break;
                                }
                            } catch (RunTasksException e) {
                                producerResponse = createQueueMessage(e.getMessage(), netSpeakRequest.getTransactionId());
                                e.printStackTrace();
                            } finally {
                                queue.add(producerResponse);
                            }
                        } else {
                            logger.info("topic empty...");
                        }
                    }
                }
            } catch (WakeupException e) {
                logger.info("Shutting down consumer " + id);
            } finally {
                consumer.close();
            }
        }

        public void shutdown() {
            consumer.wakeup();
        }
        
        // TODO FIX THIS!
        private QueueMessage createQueueMessage(String response, int transactionId) {
            //TODO create topic according to request context
            NetconfSpeakerResponse operationResponse = new NetconfSpeakerResponse(response, transactionId);
            return new QueueMessage("netconfspeaker-response", operationResponse);
        }

        private QueueMessage createQueueMessage(Map<String, String> response, int transactionId) {
            //TODO create topic according to request context
            NetconfSpeakerResponse operationResponse = new NetconfSpeakerResponse(response, transactionId);
            return new QueueMessage("netconfspeaker-response", operationResponse);
        }
    }

    public class ProducerRunnable implements Runnable {
        KafkaProducer<String, NetconfSpeakerResponse> producer;
        String bootstrapServers;
        BlockingQueue<QueueMessage> queue;
        String id;
        private boolean shutdownFlag;

        public ProducerRunnable(String id, String bootstrapServers, BlockingQueue<QueueMessage> queue) {
            this.bootstrapServers = bootstrapServers;
            this.queue = queue;
            this.id = id;
            shutdownFlag = false;

            // configure consumer properties
            Properties properties = new Properties();
            properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, NetconfSpeakerResponseSerializer.class.getName());

            producer = new KafkaProducer<>(properties);
        }

        @Override
        public void run() {
            while (!shutdownFlag) {
                try {
                    QueueMessage message = queue.take();
                    ProducerRecord<String, NetconfSpeakerResponse> record = new ProducerRecord<>(message.getTopic(), message.getResponse());
                    producer.send(record, new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                            if (e == null) {
                                logger.info("Producer Id: " + id  + "\n" +
                                        "Topic: " + recordMetadata.topic() + "\n" +
                                        "partition: " + recordMetadata.partition() + "\n" +
                                        "offset: " + recordMetadata.offset() + "\n" +
                                        "Timestamp: " + recordMetadata.timestamp());
                            } else {
                                logger.info(e.getMessage());
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    logStackTrace(e, "error");
                }
            }
            producer.close();
        }

        public void shutdown() {
            shutdownFlag = true;
        }
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
