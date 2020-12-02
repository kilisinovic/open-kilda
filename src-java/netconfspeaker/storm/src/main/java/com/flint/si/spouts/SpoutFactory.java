package com.flint.si.spouts;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.storm.kafka.spout.ByTopicRecordTranslator;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.kafka.spout.KafkaSpoutConfig.FirstPollOffsetStrategy;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

public class SpoutFactory {
    
    private static KafkaSpoutConfig<String, String> getKafkaSpoutConfig(String bootstrapServers) {
        
        ByTopicRecordTranslator<String, String> trans = new ByTopicRecordTranslator<>(
                (r) ->
                new Values(r.topic(), r.partition(), r.offset(), r.key(), r.value()),
                new Fields("topic", "partition", "offset", "key", "value"), "node-administrator-request");
        
        
        return KafkaSpoutConfig.builder(bootstrapServers, new String[]{"node-administrator-request"})
            .setProp(ConsumerConfig.GROUP_ID_CONFIG, "network-node-administrator")
            .setRecordTranslator(trans)
            .setOffsetCommitPeriodMs(10_000)
            .setFirstPollOffsetStrategy(FirstPollOffsetStrategy.UNCOMMITTED_EARLIEST)
            .setMaxUncommittedOffsets(250)
            .build();
    }

    public static IRichSpout getConfigSpout(){
        return new ConfigSpout();
    }
    
    public static IRichSpout getKafkaSpout(String bootstrapServers){
        return new KafkaSpout<String, String>(getKafkaSpoutConfig(bootstrapServers));
    }
}

