package benchmark;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 Benchmark                                                          Mode  Cnt      Score     Error  Units
 InMemoryIndexSearchVsStringContains.inMemoryLuceneIndex           thrpt   20  31774,276 ± 905,612  ops/s
 InMemoryIndexSearchVsStringContains.inMemoryLuceneIndexWildCards  thrpt   20   7704,991 ± 159,091  ops/s
 InMemoryIndexSearchVsStringContains.stringContains                thrpt   20   3058,281 ± 116,272  ops/s
 */
public class InMemoryIndexSearchVsStringContains {
    private final static List<String> urls = new ArrayList<>();
    private final static MemoryIndex idx = new MemoryIndex();
    private final static Analyzer analyzer = new StandardAnalyzer();
    private final static QueryParser parser = new QueryParser("url", analyzer);

    static {
        try {
            urls.addAll(Files.readAllLines(Paths.get("urls.txt")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        idx.addField("url", idx.keywordTokenStream(urls));
        // make this index fully thread-safe
        idx.freeze();
    }

    public static void main(String[] args) throws IOException, RunnerException {
        org.openjdk.jmh.Main.main(args);
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 5)
    @Measurement(iterations = 20)
    public void stringContains(ExecutionPlan plan) {
        plan.result = urls.stream().anyMatch(url -> UUID.randomUUID().toString().contains(url));
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 5)
    @Measurement(iterations = 20)
    public void inMemoryLuceneIndex(ExecutionPlan plan) throws ParseException {
        plan.similarity = idx.search(parser.parse(UUID.randomUUID().toString()));
    }

    @Fork(value = 1, warmups = 1)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 5)
    @Measurement(iterations = 20)
    public void inMemoryLuceneIndexWildCards(ExecutionPlan plan) throws ParseException {
        plan.similarity = idx.search(parser.parse(UUID.randomUUID().toString() + "*"));
    }

    @State(Scope.Benchmark)
    public static class ExecutionPlan {
        volatile boolean result;
        volatile float similarity;

        @Setup(Level.Invocation)
        public void setUp() {
        }
    }
}
