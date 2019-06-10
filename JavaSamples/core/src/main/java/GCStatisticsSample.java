import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GCStatisticsSample {
    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService executor =  Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            ManagementFactory.getGarbageCollectorMXBeans().forEach(gcMetric -> {
                System.out.println(String.format("%s Count: %d Time: %d", gcMetric.getName(), gcMetric.getCollectionCount(), gcMetric.getCollectionTime()));
            });

            ManagementFactory.getMemoryPoolMXBeans().forEach(poolMetric -> {
                System.out.println(String.format("%s Max: %d Used: %d", poolMetric.getName(), poolMetric.getUsage().getMax(), poolMetric.getUsage().getUsed()));
            });

        }, 0, 5, TimeUnit.SECONDS);

        Thread.sleep(50000);
    }
}
