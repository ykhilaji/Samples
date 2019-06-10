import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;
import java.util.Random;

public class MultipleStreamsFromSingleSpout {
    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();

        BaseRichSpout spout = new SpoutWithMultipleStreams();
        builder.setSpout("random", spout);

        BaseBasicBolt bolt = new BoltConsumingMultipleStreams();
        builder.setBolt("print", bolt)
                .shuffleGrouping("random", "first")
                .shuffleGrouping("random", "second");

        Config config = new Config();
        config.setDebug(false);
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("Test", config, builder.createTopology());
    }
}

class SpoutWithMultipleStreams extends BaseRichSpout {
    private Random random;
    private SpoutOutputCollector outputCollector;

    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.random = new Random();
        this.outputCollector = spoutOutputCollector;
    }

    public void nextTuple() {
        Utils.sleep(1000);
        this.outputCollector.emit("first", new Values(random.nextInt(100)));
        this.outputCollector.emit("second", new Values(System.currentTimeMillis()));
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream("first", new Fields("nextInt"));
        outputFieldsDeclarer.declareStream("second", new Fields("timestamp"));
    }
}

class BoltConsumingMultipleStreams extends BaseBasicBolt {
    public void execute(Tuple input, BasicOutputCollector collector) {
        if ("first".equals(input.getSourceStreamId())) {
            System.out.println(String.format("Next int: %d", input.getIntegerByField("nextInt")));
        } else {
            System.out.println(String.format("Timestamp: %d", input.getLongByField("timestamp")));
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}