package basic;

import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.EventTimeTrigger;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

/**
 * Result:
 * ([1],[a, b, c])
 * ([2],[d])
 * ([4],[])
 * ([3],[])
 * ([],[g, h, i])
 * ([8],[])
 * ([9],[])
 * ([7],[])
 * ([6],[])
 * ([5],[])
 * ([],[e, f])
 * ([10],[])
 * ([],[j])
 */
public class FlinkStreamCoGroup {
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

        DataStream<Tuple3<Integer, String, Long>> second = env.fromElements(
                new Tuple3<>(1, "a", 1000L),
                new Tuple3<>(1, "b", 2000L),
                new Tuple3<>(1, "c", 3000L),
                new Tuple3<>(2, "d", 4000L),
                new Tuple3<>(2, "e", 5000L),
                new Tuple3<>(2, "f", 6000L),
                new Tuple3<>(3, "g", 7000L),
                new Tuple3<>(3, "h", 8000L),
                new Tuple3<>(3, "i", 9000L),
                new Tuple3<>(4, "j", 10000L)
        );

        first
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Tuple2<Integer, Long>>() {
                    @Override
                    public long extractAscendingTimestamp(Tuple2<Integer, Long> integerLongTuple2) {
                        return integerLongTuple2.f1;
                    }
                })
                .coGroup(second.assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Tuple3<Integer, String, Long>>() {
                    @Override
                    public long extractAscendingTimestamp(Tuple3<Integer, String, Long> t) {
                        return t.f2;
                    }
                }))
                .where(new KeySelector<Tuple2<Integer, Long>, Integer>() {
                    @Override
                    public Integer getKey(Tuple2<Integer, Long> integerLongTuple2) throws Exception {
                        return integerLongTuple2.f0;
                    }
                })
                .equalTo(new KeySelector<Tuple3<Integer, String, Long>, Integer>() {
                    @Override
                    public Integer getKey(Tuple3<Integer, String, Long> integerStringLongTuple3) throws Exception {
                        return integerStringLongTuple3.f0;
                    }
                })
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .trigger(EventTimeTrigger.create())
                .apply(new CoGroupFunction<Tuple2<Integer, Long>, Tuple3<Integer, String, Long>, Tuple2<List<Integer>, List<String>>>() {
                    @Override
                    public void coGroup(Iterable<Tuple2<Integer, Long>> iterable, Iterable<Tuple3<Integer, String, Long>> iterable1, Collector<Tuple2<List<Integer>, List<String>>> collector) throws Exception {
                        List<Integer> a = new ArrayList<>();
                        List<String> b = new ArrayList<>();
                        iterable.forEach(e -> a.add(e.f0));
                        iterable1.forEach(e -> b.add(e.f1));
                        collector.collect(Tuple2.of(a, b));
                    }
                })
                .print();

        env.execute();
    }
}
