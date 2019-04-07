import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.logger.log4j.Log4JLogger;

public class ComputeSample {
    public static void main(String[] args) throws IgniteCheckedException {
        IgniteConfiguration configuration = new IgniteConfiguration();
        IgniteLogger logger = new Log4JLogger("D:\\Program Files\\WORK\\Samples\\JavaSamples\\ignite\\src\\main\\resources\\log4j.xml").getLogger("IGNITE_SAMPLE");
        configuration.setGridLogger(logger);

        try(Ignite ignite = Ignition.start(configuration)) {
            ignite.active(true);

            IgniteCompute compute = ignite.compute(ignite.cluster().forLocal());
            compute.broadcast(() -> ignite.log().info("LOCAL COMPUTE"));

            compute = ignite.compute(ignite.cluster().forPredicate(node -> node.metrics().getTotalCpus() > 2));
            compute.broadcast(() -> ignite.log().info("TOTAL CPUs > 2"));

            compute = ignite.compute(ignite.cluster().forServers()).withAsync();
            compute.broadcast(() -> ignite.log().info("Async compute"));
            compute.future().listen(f -> ignite.log().info("Finished async compute"));

            ignite.executorService(ignite.cluster().forServers()).submit(() -> ignite.log().info("INSIDE EXECUTOR SERVICE"));
        }
    }
}
