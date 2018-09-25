import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import javax.annotation.Nullable;


public class FlinkStreamSample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<Tuple2<Integer, Integer>> stream = env.fromElements(
                new Tuple2<>(1, 1),
                new Tuple2<>(2, 2),
                new Tuple2<>(3, 3),
                new Tuple2<>(4, 4),
                new Tuple2<>(5, 5),
                new Tuple2<>(6, 6),
                new Tuple2<>(7, 7),
                new Tuple2<>(8, 8),
                new Tuple2<>(9, 9),
                new Tuple2<>(10, 10)
        );

        stream
                .assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks<Tuple2<Integer, Integer>>() {
                    @Nullable
                    @Override
                    public Watermark getCurrentWatermark() {
                        return null;
                    }

                    @Override
                    public long extractTimestamp(Tuple2<Integer, Integer> integerIntegerTuple2, long l) {
                        return integerIntegerTuple2.f0 * l;
                    }
                })
                .keyBy(0)
                .window(TumblingEventTimeWindows.of(Time.seconds(1)))
                .min(0)
                .print();

        env.execute();
    }
}
