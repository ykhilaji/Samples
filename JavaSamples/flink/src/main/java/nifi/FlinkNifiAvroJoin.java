package nifi;

import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.nifi.NiFiDataPacket;
import org.apache.flink.streaming.connectors.nifi.NiFiSource;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;

import java.io.ByteArrayInputStream;

public class FlinkNifiAvroJoin {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);

        SiteToSiteClientConfig firstConfig = new SiteToSiteClient.Builder()
                .url("http://localhost:8080/nifi")
                .portName("first")
                .requestBatchCount(5)
                .buildConfig();
        SiteToSiteClientConfig secondConfig = new SiteToSiteClient.Builder()
                .url("http://localhost:8080/nifi")
                .portName("second")
                .requestBatchCount(5)
                .buildConfig();

        DataStream<NiFiDataPacket> first = env.addSource(new NiFiSource(firstConfig));
        DataStream<NiFiDataPacket> second = env.addSource(new NiFiSource(secondConfig));
        DataStream<GenericRecord> firstMapped = first
                .map(new ByteToGenericRecord())
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<GenericRecord>() {
                    @Override
                    public long extractAscendingTimestamp(GenericRecord element) {
                        return System.currentTimeMillis();
                    }
                });
        firstMapped.print();
        DataStream<GenericRecord> secondMapped = second
                .map(new ByteToGenericRecord())
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<GenericRecord>() {
                    @Override
                    public long extractAscendingTimestamp(GenericRecord element) {
                        return System.currentTimeMillis();
                    }
                });
        secondMapped.print();
        firstMapped
                .join(secondMapped)
                .where(new KeySelector("key"))
                .equalTo(new KeySelector("key"))
                .window(TumblingEventTimeWindows.of(Time.seconds(2)))
                .apply(new JoinFunction<GenericRecord, GenericRecord, Tuple3<String, String, String>>() {
                    @Override
                    public Tuple3<String, String, String> join(GenericRecord first, GenericRecord second) throws Exception {
                        return Tuple3.of(first.get("key").toString(), first.get("value").toString(), second.get("value").toString());
                    }
                })
                .print();

        env.execute("Join");
    }

    public static class ByteToGenericRecord extends RichMapFunction<NiFiDataPacket, GenericRecord> {
        private DatumReader<GenericRecord> reader;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            reader = new GenericDatumReader<>();
        }

        /*
        value.content is a byte array represents a message which includes avro schema.
        In current implementation Nifi pipeline is: ExecuteSQL -> SplitAvro > output port
         */
        @Override
        public GenericRecord map(NiFiDataPacket value) throws Exception {
            ByteArrayInputStream bis = new ByteArrayInputStream(value.getContent());
            DataFileStream<GenericRecord> dataFileStream = new DataFileStream<>(bis, reader);
            return dataFileStream.next();
        }
    }

    public static class KeySelector implements org.apache.flink.api.java.functions.KeySelector<GenericRecord, String> {
        private String key;

        public KeySelector(String key) {
            this.key = key;
        }

        @Override
        public String getKey(GenericRecord value) throws Exception {
            return String.valueOf(value.get(key));
        }
    }
}
