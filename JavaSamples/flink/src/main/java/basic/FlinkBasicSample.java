import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;


public class FlinkBasicSample {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        DataSet<Integer> dataSet = env.fromElements(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        dataSet
                .filter(a -> a % 2 == 0)
                .reduce((a, b) -> a + b)
                .print();

        DataSet<Integer> set_one = env.fromElements(1, 3, 5, 7, 9, 3, 2, 5, 9, 10);
        DataSet<Integer> set_two = env.fromElements(2, 4, 6, 8, 10, 3, 2, 1, 5, 8);

        set_one
                .map(new MapFunction<Integer, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> map(Integer integer) throws Exception {
                        return new Tuple2<>(integer, 1);
                    }
                })
                .groupBy(0)
                .sum(1)
                .join(set_two
                        .map(new MapFunction<Integer, Tuple1<Integer>>() {
                            @Override
                            public Tuple1<Integer> map(Integer integer) throws Exception {
                                return new Tuple1<>(integer);
                            }
                        }))
                .where(0).equalTo(0).with(new JoinFunction<Tuple2<Integer, Integer>, Tuple1<Integer>, Tuple2<Integer, Integer>>() {
            @Override
            public Tuple2<Integer, Integer> join(Tuple2<Integer, Integer> integerIntegerTuple2, Tuple1<Integer> integer) throws Exception {
                return new Tuple2<>(integerIntegerTuple2.f0, integerIntegerTuple2.f1 + 1);
            }
        })
                .print();

    }
}
