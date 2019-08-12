package stream.filter.kafka.streams;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class StreamFilterTest {
    private TopologyTestDriver testDriver;
    private ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), new StringSerializer());


    @Before
    public void setup() {
        Config config = ConfigFactory.load().getConfig("filter");
        Properties properties = StreamFilter.properties(config);
        Topology topology = StreamFilter.topology(new InMemoryCacheBuilder(e -> Integer.parseInt(e.getKey()) % 2 != 0), config);

        testDriver = new TopologyTestDriver(topology, properties);
    }

    @After
    public void tearDown() {
        testDriver.close();
    }

    @Test
    public void filterInput() {
        testDriver.pipeInput(recordFactory.create("source", null, "{\"key\":\"1\"}"));
        OutputVerifier.compareKeyValue(testDriver.readOutput("target", new StringDeserializer(), new StringDeserializer()), null, "{\"key\":\"1\"}");
    }

    @Test
    public void filterEmptyOutput() {
        testDriver.pipeInput(recordFactory.create("source", null, "{\"key\":\"2\"}"));
        assertNull(testDriver.readOutput("target", new StringDeserializer(), new StringDeserializer()));
    }
}
