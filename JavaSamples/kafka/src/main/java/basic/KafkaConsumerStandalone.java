package basic;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class KafkaConsumerStandalone {
    public static void main(String[] args) {
        KafkaConsumer consumer = new KafkaConsumer(new Properties());

        List<PartitionInfo> partitionInfoList = consumer.partitionsFor("topicName");
        List<TopicPartition> topicPartitions = new ArrayList<>();

        for (PartitionInfo partitionInfo : partitionInfoList) {
            topicPartitions.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
        }

        consumer.assign(topicPartitions);

        // while true ... consumer.poll(...) ...
    }
}
