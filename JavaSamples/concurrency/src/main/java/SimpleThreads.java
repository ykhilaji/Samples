public class SimpleThreads {
    public static void main(String[] args) throws InterruptedException {
        A a1 = new A(1);
        A a2 = new A(2);

        Thread thread1 = new Thread(a1);
        Thread thread2 = new Thread(a2);

        thread1.start();
        thread2.start();

        // Wait until thread1 and thread2 finished
        thread1.join();
        thread2.join();

        System.out.println("Main thread finished");
    }

    public static class A implements Runnable {
        private int id;

        public A(int id) {
            this.id = id;
        }

        public void run() {
            try {
                System.out.println(String.format("Wait 1 sec in task id: %d", this.id));
                Thread.sleep(1000);
                System.out.println(String.format("Run task id: %d", this.id));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
