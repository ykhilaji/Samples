package basic;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;

import java.util.ArrayList;
import java.util.List;

/**
 * Result:
 * ([3, 4, 5], time)
 * ([6, 7, 8], time)
 */
public class FlinkStreamGlobalWindowAggregateSample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
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

        stream
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Tuple2<Integer, Long>>() {
                    @Override
                    public long extractAscendingTimestamp(Tuple2<Integer, Long> integerLongTuple2) {
                        return integerLongTuple2.f1;
                    }
                })
                .windowAll(TumblingEventTimeWindows.of(Time.seconds(3)))
                .trigger(CountTrigger.of(3))
                .aggregate(new AggregateFunction<Tuple2<Integer, Long>, List<Integer>, Tuple2<List<Integer>, Long>>() {
                    @Override
                    public List<Integer> createAccumulator() {
                        return new ArrayList<>();
                    }

                    @Override
                    public List<Integer> add(Tuple2<Integer, Long> integerLongTuple2, List<Integer> integers) {
                        integers.add(integerLongTuple2.f0);
                        return integers;
                    }

                    @Override
                    public Tuple2<List<Integer>, Long> getResult(List<Integer> integers) {
                        return Tuple2.of(integers, System.currentTimeMillis());
                    }

                    @Override
                    public List<Integer> merge(List<Integer> integers, List<Integer> acc1) {
                        integers.addAll(acc1);
                        return integers;
                    }
                })
                .print();

        env.execute();
    }
}
