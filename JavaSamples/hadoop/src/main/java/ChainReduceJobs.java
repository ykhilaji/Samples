import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.chain.ChainReducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.StringTokenizer;

public class ChainReduceJobs {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration(false);
        Job job = Job.getInstance(conf, "chainReduceJobs");

        job.setJarByClass(ChainReduceJobs.class);
        job.setSortComparatorClass(SortByLengthDesc.class);

        Configuration mapperConf = new Configuration(false);
        Configuration reducerConf = new Configuration(false);
        Configuration identityConf = new Configuration(false);

        ChainMapper.addMapper(job, MapperWordLength.class, Object.class, Text.class, LongWritable.class, Text.class, mapperConf);
        ChainReducer.setReducer(job, ReducerWordLength.class, LongWritable.class, Text.class, LongWritable.class, Text.class, reducerConf);
        ChainReducer.addMapper(job, Identity.class, LongWritable.class, Text.class, LongWritable.class, Text.class, identityConf);

        FileInputFormat.addInputPath(job, new Path("/Users/grifon/WORK/Samples/JavaSamples/hadoop/src/main/resources/input"));
        FileOutputFormat.setOutputPath(job, new Path("/Users/grifon/WORK/Samples/JavaSamples/hadoop/src/main/resources/output"));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }


    public static class MapperWordLength extends Mapper<Object, Text, LongWritable, Text> {
        private Text text = new Text();
        private LongWritable length = new LongWritable(0);

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer tokenizer = new StringTokenizer(value.toString(), " \",:}{/;\\][.)(><=+-*");

            while (tokenizer.hasMoreElements()) {
                String token = tokenizer.nextToken();

                text.set(token);
                length.set(token.length());

                context.write(length, text);
            }
        }
    }

    public static class ReducerWordLength extends Reducer<LongWritable, Text, LongWritable, Text> {
        private Text result = new Text();

        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            StringBuilder builder = new StringBuilder();

            for (Text value : values) {
                builder.append(value.toString()).append(" ");
            }

            result.set(builder.toString());
            context.write(key, result);
        }
    }

    public static class Identity extends Mapper<LongWritable, Text, LongWritable, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            context.write(key, value);
        }
    }

    public static class SortByLengthDesc extends WritableComparator {
        public SortByLengthDesc() {
            super(LongWritable.class, true);
        }

        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            LongWritable a1 = (LongWritable) a;
            LongWritable b1 = (LongWritable) b;

            return b1.compareTo(a1);
        }
    }



}