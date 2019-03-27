import org.apache.log4j.Logger;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.*;
import org.influxdb.impl.InfluxDBResultMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class BasicUsageTest {
    static final Logger logger = Logger.getLogger(BasicUsageTest.class);

    public static InfluxDB influxdb;
    public static InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

    @BeforeAll
    public static void beforeAll() {
        logger.info("Connect to influxdb");
        influxdb = InfluxDBFactory.connect("http://localhost:8086");
        influxdb.query(new Query("create database test"));
        influxdb.query(new Query("create retention policy rp on test duration 1h replication 1"));
        influxdb.setLogLevel(InfluxDB.LogLevel.BASIC);
        influxdb.setConsistency(InfluxDB.ConsistencyLevel.ALL);
    }

    @AfterAll
    public static void afterAll() {
        influxdb.close();
    }

    @AfterEach
    public void truncate() {
        influxdb.query(new Query("drop series from metric", "test"));
    }

    @Measurement(name = "metric", database = "test", retentionPolicy = "rp")
    public static class Metric {
        @Column(name = "c1")
        private int c1;
        @Column(name = "c2")
        private int c2;
        @Column(name = "c3")
        private int c3;

        public Metric() {
        }

        public Metric(int c1, int c2, int c3) {
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
        }

        public int getC1() {
            return c1;
        }

        public void setC1(int c1) {
            this.c1 = c1;
        }

        public int getC2() {
            return c2;
        }

        public void setC2(int c2) {
            this.c2 = c2;
        }

        public int getC3() {
            return c3;
        }

        public void setC3(int c3) {
            this.c3 = c3;
        }

        @Override
        public String toString() {
            return "Metric{" +
                    "c1=" + c1 +
                    ", c2=" + c2 +
                    ", c3=" + c3 +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Metric metric = (Metric) o;
            return c1 == metric.c1 &&
                    c2 == metric.c2 &&
                    c3 == metric.c3;
        }

        @Override
        public int hashCode() {
            return Objects.hash(c1, c2, c3);
        }
    }

    @Test
    public void select() {
        Point point = Point.measurement("metric")
                .addField("c1", 1)
                .addField("c2", 2)
                .addField("c3", 3)
                .build();

        influxdb.write("test", "rp", point);
        QueryResult result = influxdb.query(new Query("select * from test.rp.metric"));
        List<Metric> metricList = resultMapper.toPOJO(result, Metric.class);

        assertEquals(1, metricList.size());
        assertEquals(new Metric(1, 2, 3), metricList.get(0));
    }

    @Measurement(name = "metric")
    public static class Count {
        @Column(name = "count")
        long count;
    }

    @Test
    public void batchWriteAsync() {
        // flush every 10 points or each 500 ms
        // batch will create internal thread pool which should be stopped explicitly using influxdb.close()
        influxdb.enableBatch(10, 500, TimeUnit.MILLISECONDS);
        IntStream.range(0, 50).forEach(i -> influxdb.write("test", "rp", Point.measurementByPOJO(Metric.class)
                // time is necessary, otherwise some points will be squashed
                .time(System.currentTimeMillis() + i, TimeUnit.MILLISECONDS)
                .addFieldsFromPOJO(new Metric(i, i, i))
                .build()));

        influxdb.flush();
        influxdb.disableBatch();
        QueryResult result = influxdb.query(new Query("select count(c1) from test.rp.metric"));
        assertEquals(50, resultMapper.toPOJO(result, Count.class).get(0).count);
    }

    @Test
    public void batchWriteAsyncWithoutExplicitTime() {
        // there is another option to customize batch options
        // influxdb.enableBatch(BatchOptions.DEFAULTS.exceptionHandler(((points, throwable) -> {
        //            // do something
        //        })).actions(10).flushDuration(500));
        influxdb.enableBatch(10, 500, TimeUnit.MILLISECONDS);
        IntStream.range(0, 50).forEach(i -> influxdb.write("test", "rp", Point.measurementByPOJO(Metric.class)
                .addFieldsFromPOJO(new Metric(i, i, i))
                .build()));

        influxdb.flush();
        influxdb.disableBatch();
        QueryResult result = influxdb.query(new Query("select count(c1) from test.rp.metric"));
        // only 5 points were written, because there were 5 batches and inside each batch points have the same timestamp
        assertEquals(5, resultMapper.toPOJO(result, Count.class).get(0).count);
    }

    @Test
    public void batchWriteSyncUsingBatchPoints() {
        BatchPoints batchPoints = BatchPoints
                .database("test")
                .retentionPolicy("rp")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

        IntStream.range(0, 50).forEach(i -> batchPoints.point(Point.measurementByPOJO(Metric.class)
                .time(System.currentTimeMillis() + i, TimeUnit.MILLISECONDS)
                .addFieldsFromPOJO(new Metric(i, i, i))
                .build()));

        influxdb.write(batchPoints);
        QueryResult result = influxdb.query(new Query("select count(c1) from test.rp.metric"));
        assertEquals(50, resultMapper.toPOJO(result, Count.class).get(0).count);
    }

    @Test
    public void parameterBinding() {
        IntStream.range(0, 50).forEach(i -> influxdb.write("test", "rp", Point.measurementByPOJO(Metric.class)
                .time(System.currentTimeMillis() + i, TimeUnit.MILLISECONDS)
                .addFieldsFromPOJO(new Metric(i, i, i))
                .build()));

        QueryResult result = influxdb.query(BoundParameterQuery.QueryBuilder
                .newQuery("select count(c1) from test.rp.metric where c1 >= $i")
                .bind("i", 25)
                .create());
        assertEquals(25, resultMapper.toPOJO(result, Count.class).get(0).count);
    }

    @Test
    public void selectUsingCallback() {
        influxdb.query(new Query("select * from test.rp.metric"),
                queryResult -> logger.info("Success"),
                throwable -> logger.error(throwable.getLocalizedMessage()));
    }
}
