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

public class BasicUsageSample {
    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();

        BaseRichSpout spout = new TestSpout();
        builder.setSpout("random", spout);

        BaseBasicBolt bolt = new TestBolt();
        builder.setBolt("print", bolt)
        .shuffleGrouping("random");

        Config config = new Config();
        config.setDebug(false);
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("Test", config, builder.createTopology());
    }
}

// BaseRichSpout -> IRichSpout extends ISpout implements IComponent
class TestSpout extends BaseRichSpout {
    private Random random;
    private SpoutOutputCollector outputCollector;

    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.random = new Random();
        this.outputCollector = spoutOutputCollector;
    }

    public void nextTuple() {
        Utils.sleep(1000);
        this.outputCollector.emit(new Values(random.nextInt(100)));
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("nextInt"));
    }
}

// BaseBasicBolt extends IBasicBolt implements IComponent
class TestBolt extends BaseBasicBolt {
    public void execute(Tuple input, BasicOutputCollector collector) {
        System.out.println(String.format("Next int: %d", input.getIntegerByField("nextInt")));
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}