import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;
import java.util.Random;

public class AckUsageSample {
    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();

        BaseRichSpout spout = new SproutRequiredAck();
        builder.setSpout("random", spout);

        BaseRichBolt bolt = new BoltWithAckConfirmation();
        builder.setBolt("print", bolt)
                .shuffleGrouping("random");

        Config config = new Config();
        config.setDebug(false);
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("Test", config, builder.createTopology());
    }

    static class SproutRequiredAck extends BaseRichSpout {
        private Random random;
        private SpoutOutputCollector outputCollector;

        public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
            this.random = new Random();
            this.outputCollector = spoutOutputCollector;
        }

        public void nextTuple() {
            Utils.sleep(1000);
            this.outputCollector.emit(new Values(random.nextInt(100)), "id");
        }

        public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
            outputFieldsDeclarer.declare(new Fields("nextInt"));
        }

        @Override
        public void ack(Object msgId) {
            System.out.println(String.format("Success: %s", msgId));
        }

        @Override
        public void fail(Object msgId) {
            System.out.println(String.format("Fail: %s", msgId));
        }
    }

    static class BoltWithAckConfirmation extends BaseRichBolt {
        private OutputCollector outputCollector;

        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.outputCollector = collector;
        }

        public void execute(Tuple input) {
            int val = input.getIntegerByField("nextInt");

            if (val % 2 != 0) {
                System.out.println("OK");
                outputCollector.ack(input);
            } else {
                System.out.println("NOT OK");
                outputCollector.fail(input);
            }
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {

        }
    }
}