import java.util.concurrent.locks.*;
import java.util.concurrent.*;

class RwLockRwLocks {
    private static final int N_READERS = 5;
    private static final int N_WRITERS = 2;

    private int readers = 0;
    private int writers = 0;
    private int waitingWriters = 0;
    private int sharedData = 0;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    class Reader implements Runnable {
        private int id;

        Reader(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    lock.lock();
                    while (writers > 0 || waitingWriters > 0) {
                        condition.await();
                    }
                    readers++;
                    lock.unlock();

                    // Seção crítica de leitura
                    System.out.println("Leitor " + id + " leu valor: " + sharedData);
                    lock.lock();
                    readers--;
                    if (readers == 0) {
                        condition.signalAll();
                    }
                    lock.unlock();

                    Thread.sleep(200); // 200 ms
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    class Writer implements Runnable {
        private int id;

        Writer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    lock.lock();
                    waitingWriters++;
                    while (readers > 0 || writers > 0) {
                        condition.await();
                    }
                    waitingWriters--;
                    writers = 1;
                    lock.unlock();

                    // Seção crítica de escrita
                    sharedData++;
                    System.out.println(">>> Escritor " + id + " escreveu valor: " + sharedData);

                    lock.lock();
                    writers = 0;
                    condition.signalAll();
                    lock.unlock();

                    Thread.sleep(400); // 400 ms
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void startSimulation() {
        ExecutorService executor = Executors.newFixedThreadPool(N_READERS + N_WRITERS);

        for (int i = 1; i <= N_READERS; i++) {
            executor.execute(new Reader(i));
        }
        for (int i = 1; i <= N_WRITERS; i++) {
            executor.execute(new Writer(i));
        }
    }

    public static void main(String[] args) {
        new RwLockRwLocks().startSimulation();
    }
}
