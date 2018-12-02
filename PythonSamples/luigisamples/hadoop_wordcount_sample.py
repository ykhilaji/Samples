import luigi
import luigi.contrib.hadoop


class InitialData(luigi.ExternalTask):
    def output(self):
        return luigi.LocalTarget(path="/Users/grifon/WORK/Samples/PythonSamples/luigisamples/input.txt")


class HadoopMapReduce(luigi.contrib.hadoop.JobTask):
    def requires(self):
        return [InitialData()]

    def output(self):
        return luigi.LocalTarget(path="/Users/grifon/WORK/Samples/PythonSamples/luigisamples/output.txt")

    def mapper(self, line):
        for w in line.strip().split():
            yield (w, 1)

    def reducer(self, key, values):
        yield (key, sum(values))


if __name__ == '__main__':
    luigi.build(tasks=[HadoopMapReduce()], local_scheduler=True)
