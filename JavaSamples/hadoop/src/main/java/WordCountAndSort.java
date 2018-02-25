import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.StringTokenizer;

// sort by key
public class WordCountAndSort {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "wordCountAndSort");

        job.setJarByClass(WordCountAndSort.class);
        job.setMapperClass(WordMapper.class);
        job.setReducerClass(WordReducer.class);
        job.setCombinerClass(WordReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setSortComparatorClass(WordComparatorFirst.class);

        FileInputFormat.addInputPath(job, new Path("/Users/grifon/WORK/Samples/JavaSamples/hadoop/src/main/resources/input"));
        FileOutputFormat.setOutputPath(job, new Path("/Users/grifon/WORK/Samples/JavaSamples/hadoop/src/main/resources/output"));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static class WordComparatorFirst extends WritableComparator {
        public WordComparatorFirst() {
            super(Text.class, true);
        }

        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            Text a1 = (Text) a;
            Text b1 = (Text) b;

            return a1.toString().toLowerCase().compareTo(b1.toString().toLowerCase());
        }
    }

    public static class WordMapper extends Mapper<Object, Text, Text, IntWritable> {
        private IntWritable one = new IntWritable(1);
        private Text text = new Text();

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer tokenizer = new StringTokenizer(value.toString(), " \",:}{/;\\][.)(><=+-*");

            while (tokenizer.hasMoreElements()) {
                text.set(tokenizer.nextToken());
                context.write(text, one);
            }
        }
    }

    public static class WordReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum = 0;

            for (IntWritable value : values) {
                sum += value.get();
            }

            result.set(sum);
            context.write(key, result);
        }
    }
}