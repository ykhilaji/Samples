import java.util.concurrent.*;

public class CallableSample {
    public static void main(String[] args) {
        ExecutorService callable = Executors.newFixedThreadPool(4);
        ExecutorService runnable = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 50; ++i) {
            Future<Integer> future = callable.submit(new A(i));
            runnable.submit(() -> {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 2000));
                    System.out.println(String.format("Result: %d", future.get()));
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }

        callable.shutdown();
        runnable.shutdown();
    }

    public static class A implements Callable<Integer> {
        private int id;

        public A(int id) {
            this.id = id;
        }

        public Integer call() throws Exception {
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 2000));
            System.out.println(String.format("Run task id: %d", id));
            return id;
        }
    }
}
