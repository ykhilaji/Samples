import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.*;
import org.apache.storm.trident.testing.CountAsAggregator;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.trident.windowing.InMemoryWindowsStoreFactory;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TridentWindowedWordCount {
    public static void main(String[] args) {
        List<String> sentences = Arrays.asList("a b c", "d e f", "c b a", "f e d", "l h f g a", "abc cba");
        BaseRichSpout spout = new SproutSentences(sentences);

        TridentTopology topology = new TridentTopology();

        topology.newStream("sentences", spout)
                .peek(new Consumer() {
                    @Override
                    public void accept(TridentTuple input) {
                        System.out.println(String.format("Sentence: %s", input.getStringByField("sentence")));
                    }
                })
                .flatMap(new FlatMapFunction() {
                    @Override
                    public Iterable<Values> execute(TridentTuple input) {
                        return Arrays.stream(input.getStringByField("sentence").split(" "))
                                .map(Values::new)
                                .collect(Collectors.toList());
                    }
                }, new Fields("word"))
                .peek(new Consumer() {
                    @Override
                    public void accept(TridentTuple input) {
                        System.out.println(String.format("Word: %s", input.getStringByField("word")));
                    }
                })
                .tumblingWindow(BaseWindowedBolt.Duration.seconds(5), new InMemoryWindowsStoreFactory(), new Fields("word"), new CountAsAggregator(), new Fields("count"))
                .peek(new Consumer() {
                    @Override
                    public void accept(TridentTuple input) {
                        System.out.println(String.format("Windowed: %s", input));
                    }
                });

        Config config = new Config();
        config.setDebug(false);
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("Test", config, topology.build());
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
    }
}
