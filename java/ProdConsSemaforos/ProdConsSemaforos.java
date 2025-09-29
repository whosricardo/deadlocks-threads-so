import java.util.concurrent.Semaphore;

public class ProdConsSemaforos {
    private static final int BUFFER_SIZE = 5;
    private static final int NUM_ITEMS = 10;

    private static int[] buffer = new int[BUFFER_SIZE];
    private static int in = 0;
    private static int out = 0;

    private static Semaphore empty = new Semaphore(BUFFER_SIZE);
    private static Semaphore full = new Semaphore(0);
    private static final Object mutex = new Object();

    static class Produtor extends Thread {
        public void run() {
            for (int i = 0; i < NUM_ITEMS; i++) {
                try {
                    int item = (int) (Math.random() * 100);
                    empty.acquire();
                    synchronized (mutex) {
                        buffer[in] = item;
                        System.out.println("Produtor produziu: " + item + " (pos=" + in + ")");
                        in = (in + 1) % BUFFER_SIZE;
                    }
                    full.release();
                    Thread.sleep((int) (Math.random() * 500));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Consumidor extends Thread {
        public void run() {
            for (int i = 0; i < NUM_ITEMS; i++) {
                try {
                    full.acquire();
                    int item;
                    synchronized (mutex) {
                        item = buffer[out];
                        System.out.println("Consumidor consumiu: " + item + " (pos=" + out + ")");
                        out = (out + 1) % BUFFER_SIZE;
                    }
                    empty.release();
                    Thread.sleep((int) (Math.random() * 800));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Thread produtor = new Produtor();
        Thread consumidor = new Consumidor();
        produtor.start();
        consumidor.start();
        try {
            produtor.join();
            consumidor.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
