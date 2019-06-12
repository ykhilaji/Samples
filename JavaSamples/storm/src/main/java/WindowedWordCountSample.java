import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.apache.storm.windowing.TimestampExtractor;
import org.apache.storm.windowing.TupleWindow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WindowedWordCountSample {
    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        List<String> sentences = Arrays.asList("a b c", "d e f", "c b a", "f e d", "l h f g a", "abc cba");

        BaseRichSpout spout = new SproutSentences(sentences);
        builder.setSpout("sentences", spout);

        BaseRichBolt splitBolt = new BoltSplitSentenceToWords();
        builder.setBolt("split", splitBolt, 2)
                .setNumTasks(2)
                .shuffleGrouping("sentences");

        // Fixed window
        BaseWindowedBolt windowCountWord = new BoltWindowCountWord()
                .withTumblingWindow(BaseWindowedBolt.Count.of(15))
                .withTumblingWindow(BaseWindowedBolt.Duration.seconds(5))
                .withTimestampExtractor((TimestampExtractor) tuple -> System.currentTimeMillis()); // or use timestamp field

        builder.setBolt("count", windowCountWord)
                .fieldsGrouping("split", new Fields("word"));

        BoltPrinter boltPrinter = new BoltPrinter();
        builder.setBolt("print", boltPrinter)
                .globalGrouping("count");

        Config config = new Config();
        config.setDebug(false);
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("Test", config, builder.createTopology());
    }

    static class SproutSentences extends BaseRichSpout {
        private SpoutOutputCollector outputCollector;
        private List<String> sentences;
        private int i = 0;

        public SproutSentences(List<String> sentences) {
            this.sentences = sentences;
        }

        public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
            this.outputCollector = spoutOutputCollector;
        }

        public void nextTuple() {
            Utils.sleep(1000);
            this.outputCollector.emit(new Values(sentences.get(i++ % sentences.size())), i);
        }

        public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
            outputFieldsDeclarer.declare(new Fields("sentence"));
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

    static class BoltSplitSentenceToWords extends BaseRichBolt {
        private OutputCollector outputCollector;

        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.outputCollector = collector;
        }

        public void execute(Tuple input) {
            try {
                String sentence = input.getStringByField("sentence");
                // input -> anchor
                Stream.of(sentence.split(" ")).forEach(word -> outputCollector.emit(input, new Values(word)));
                outputCollector.ack(input);
            } catch (Exception e) {
                e.printStackTrace();
                outputCollector.fail(input);
            }
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word"));
        }
    }

    static class BoltWindowCountWord extends BaseWindowedBolt {
        private OutputCollector outputCollector;

        @Override
        public void execute(TupleWindow inputWindow) {
            inputWindow.get().stream()
                    .map(tuple -> tuple.getStringByField("word"))
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                    .forEach((word, count) -> outputCollector.emit(new Values(word, count)));
        }

        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.outputCollector = collector;
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word", "count"));
        }
    }

    static class BoltPrinter extends BaseRichBolt {
        private OutputCollector outputCollector;

        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.outputCollector = collector;
        }

        public void execute(Tuple input) {
            System.out.println(String.format("Word: %s, Count: %s", input.getStringByField("word"), input.getLongByField("count")));
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }
    }
}
