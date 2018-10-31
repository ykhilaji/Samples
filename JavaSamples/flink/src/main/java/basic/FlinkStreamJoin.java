package basic;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * Result:
 * (1,1)
 *  Skip (2, 2) because of end of the window (The second 2 comes at the end of the window - 3000L)
 * (3,3)
 * (4,4)
 * Skip (5, 5) because of end of the window (The second 5 comes at the end of the window - 6000L)
 * (6,6)
 * (7,7)
 * Skip (8, 8) because of end of the window (The second 8 comes at the end of the window - 9000L)
 * (9,9)
 * (10,10)
 */
public class FlinkStreamJoin {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        DataStream<Tuple2<Integer, Long>> first = env.fromElements(
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

        DataStream<Tuple2<Integer, Long>> second = env.fromElements(
                new Tuple2<>(1, 2000L),
                new Tuple2<>(2, 3000L),
                new Tuple2<>(3, 4000L),
                new Tuple2<>(4, 5000L),
                new Tuple2<>(5, 6000L),
                new Tuple2<>(6, 7000L),
                new Tuple2<>(7, 8000L),
                new Tuple2<>(8, 9000L),
                new Tuple2<>(9, 10000L),
                new Tuple2<>(10, 11000L)
        );

        DataStream<Tuple2<Integer, Long>> firstWithTimeStamp = first.assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Tuple2<Integer, Long>>() {
            @Override
            public long extractAscendingTimestamp(Tuple2<Integer, Long> integerLongTuple2) {
                return integerLongTuple2.f1;
            }
        });

        DataStream<Tuple2<Integer, Long>> secondWithTimeStamp = second.assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Tuple2<Integer, Long>>() {
            @Override
            public long extractAscendingTimestamp(Tuple2<Integer, Long> integerLongTuple2) {
                return integerLongTuple2.f1;
            }
        });

        firstWithTimeStamp
                .join(secondWithTimeStamp)
                .where(new KeySelector<Tuple2<Integer, Long>, Integer>() {
                    @Override
                    public Integer getKey(Tuple2<Integer, Long> integerLongTuple2) throws Exception {
                        return integerLongTuple2.f0;
                    }
                })
                .equalTo(new KeySelector<Tuple2<Integer, Long>, Integer>() {
                    @Override
                    public Integer getKey(Tuple2<Integer, Long> integerLongTuple2) throws Exception {
                        return integerLongTuple2.f0;
                    }
                })
                .window(TumblingEventTimeWindows.of(Time.seconds(3)))
                .apply(new JoinFunction<Tuple2<Integer, Long>, Tuple2<Integer, Long>, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> join(Tuple2<Integer, Long> integerLongTuple2, Tuple2<Integer, Long> integerLongTuple22) throws Exception {
                        return Tuple2.of(integerLongTuple2.f0, integerLongTuple22.f0);
                    }
                })
                .print();

        env.execute();
    }
}
