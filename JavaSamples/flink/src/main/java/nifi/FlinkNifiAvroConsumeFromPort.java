package nifi;

import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.nifi.NiFiDataPacket;
import org.apache.flink.streaming.connectors.nifi.NiFiSource;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;

import java.io.ByteArrayInputStream;

/**
 * nifi.properties:
 * # Site to Site properties
 nifi.remote.input.host=
 nifi.remote.input.secure=false
 nifi.remote.input.socket.port=10000
 nifi.remote.input.http.enabled=true
 nifi.remote.input.http.transaction.ttl=30 sec
 nifi.remote.contents.cache.expiration=30 secs
 */
public class FlinkNifiAvroConsumeFromPort {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        SiteToSiteClientConfig clientConfig = new SiteToSiteClient.Builder()
                .url("http://localhost:8080/nifi")
                .portName("flink")
                .requestBatchCount(5)
                .buildConfig();

        DataStream<NiFiDataPacket> source = env.addSource(new NiFiSource(clientConfig));
        source.map(new RichMapFunction<NiFiDataPacket, GenericRecord>() {
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
        }).print();

        env.execute("Nifi -> flink");
    }
}
