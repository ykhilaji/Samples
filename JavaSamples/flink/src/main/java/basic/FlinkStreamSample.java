package basic;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


/**
 * Basic sample of mapping
 */
public class FlinkStreamSample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(1);
        DataStream<Tuple2<Integer, Long>> stream = env.fromElements(
                new Tuple2<>(1, 1000L),
                new Tuple2<>(2, 2000L),
                new Tuple2<>(3, 3000L),
                new Tuple2<>(4, 4000L),
                new Tuple2<>(5, 5000L),
                new Tuple2<>(6, 6000L),
                new Tuple2<>(7, 7000L),
                new Tuple2<>(8, 8000L),
                new Tuple2<>(9, 9000L),
                new Tuple2<>(10, 10000L)
        );

        stream.map(new MapFunction<Tuple2<Integer, Long>, Integer>() {
            @Override
            public Integer map(Tuple2<Integer, Long> integerLongTuple2) throws Exception {
                return integerLongTuple2.f0 * 2;
            }
        }).print();

        env.execute();
    }
}
