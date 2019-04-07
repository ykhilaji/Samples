import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledThreadExecutorSample {
    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

        executorService.scheduleAtFixedRate(() -> System.out.println("Inside task"), 0, 2, TimeUnit.SECONDS);

        Thread.sleep(10000);
        executorService.shutdown();
    }
}
