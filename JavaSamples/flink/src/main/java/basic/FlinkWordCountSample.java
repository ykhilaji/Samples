package basic;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

public class FlinkWordCountSample {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        DataSource<String> dataSet = env.readTextFile("D:\\Program Files\\WORK\\Samples\\JavaSamples\\flink\\src\\main\\resources\\input.txt");

        dataSet
                .flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) throws Exception {
                        String[] tokens = s.toLowerCase().split("\\W+");

                        // emit the pairs
                        for (String token : tokens) {
                            if (token.length() > 0) {
                                collector.collect(new Tuple2<String, Integer>(token, 1));
                            }
                        }
                    }
                })
                .groupBy(0)
                .sum(1)
                .print();
    }
}
