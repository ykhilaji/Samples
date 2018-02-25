import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.StringTokenizer;

public class ChainJobs {
    private static String INPUT_DIR = "/Users/grifon/WORK/Samples/JavaSamples/hadoop/src/main/resources/input";
    private static String OUTPUT_DIR = "/Users/grifon/WORK/Samples/JavaSamples/hadoop/src/main/resources/output";

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration firstConfiguration = new Configuration();
        Configuration secondConfiguration = new Configuration();

        secondConfiguration.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", "");

        Job firstJob = Job.getInstance(firstConfiguration, "firstStep");
        Job secondJob = Job.getInstance(secondConfiguration, "secondStep");

        firstJob.setJarByClass(ChainJobs.class);
        firstJob.setMapperClass(FirstMapper.class);
        firstJob.setReducerClass(FirstReducer.class);
        firstJob.setOutputKeyClass(Text.class);
        firstJob.setOutputValueClass(LongWritable.class);

        FileInputFormat.addInputPath(firstJob, new Path(INPUT_DIR, "input.txt"));
        FileOutputFormat.setOutputPath(firstJob, new Path(OUTPUT_DIR, "firstJobOut"));

        if (!firstJob.waitForCompletion(true)) {
            System.exit(1);
        }

        secondJob.setJarByClass(ChainJobs.class);
        secondJob.setMapperClass(SecondMapper.class);
        secondJob.setReducerClass(SecondReducer.class);
        secondJob.setOutputKeyClass(Text.class);
        secondJob.setOutputValueClass(Text.class);
        secondJob.setInputFormatClass(KeyValueTextInputFormat.class);

        KeyValueTextInputFormat.addInputPath(secondJob, new Path(OUTPUT_DIR, "firstJobOut"));
        FileOutputFormat.setOutputPath(secondJob, new Path(OUTPUT_DIR, "secondJobOut"));

        if (!secondJob.waitForCompletion(true)) {
            System.exit(1);
        }

    }

    public static class FirstMapper extends Mapper<Object, Text, Text, LongWritable> {
        private Text text = new Text();
        private LongWritable one = new LongWritable(1);

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer stringTokenizer = new StringTokenizer(value.toString(), " \",:}{/;\\][.)(><=+-*");

            while (stringTokenizer.hasMoreElements()) {
                text.set(stringTokenizer.nextToken());
                context.write(text, one);
            }
        }
    }

    public static class FirstReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        private LongWritable sum = new LongWritable(0);

        @Override
        protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            long a = 0;

            for (LongWritable longWritable : values) {
                a += longWritable.get();
            }

            sum.set(a);
            context.write(key, sum);
        }
    }

    public static class SecondMapper extends Mapper<Text, Text, Text, Text> {
        @Override
        protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
            System.out.println("KEY: " + key.toString() + " VALUE: " + value.toString());
            context.write(value, key);
        }
    }

    public static class SecondReducer extends Reducer<Text, Text, Text, Text> {
        private Text text = new Text();

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            StringBuilder stringBuilder = new StringBuilder();

            for (Text str : values) {
                stringBuilder.append(str.toString()).append(", ");
            }

            text.set(stringBuilder.substring(0, stringBuilder.toString().length() - 1));
            context.write(key, text);
        }
    }
}
