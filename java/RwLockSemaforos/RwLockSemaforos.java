import java.util.concurrent.Semaphore;

class RWLock {
    private int readers = 0;                // nº de leitores ativos
    private final Semaphore mutex = new Semaphore(1);       // protege o contador de leitores
    private final Semaphore roomEmpty = new Semaphore(1);   // garante exclusão mútua na área
    private final Semaphore turnstile = new Semaphore(1);   // dá prioridade a escritores

    public void readerEnter(int id) throws InterruptedException {
        turnstile.acquire();   // passa pela catraca
        turnstile.release();

        mutex.acquire();
        readers++;
        if (readers == 1) {
            roomEmpty.acquire(); // primeiro leitor bloqueia escritores
        }
        mutex.release();

        System.out.println("Leitor " + id + " está lendo...");
        Thread.sleep(100); // simula leitura
    }

    public void readerExit(int id) throws InterruptedException {
        mutex.acquire();
        readers--;
        if (readers == 0) {
            roomEmpty.release(); // último leitor libera escritores
        }
        mutex.release();
    }

    public void writerEnter(int id) throws InterruptedException {
        turnstile.acquire();   // bloqueia novos leitores
        roomEmpty.acquire();   // acesso exclusivo
        System.out.println("Escritor " + id + " está escrevendo...");
        Thread.sleep(150); // simula escrita
    }

    public void writerExit(int id) {
        roomEmpty.release();
        turnstile.release();
    }
}

class Reader implements Runnable {
    private final RWLock lock;
    private final int id;

    public Reader(RWLock lock, int id) {
        this.lock = lock;
        this.id = id;
    }

    public void run() {
        try {
            while (true) {
                lock.readerEnter(id);
                lock.readerExit(id);
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Writer implements Runnable {
    private final RWLock lock;
    private final int id;

    public Writer(RWLock lock, int id) {
        this.lock = lock;
        this.id = id;
    }

    public void run() {
        try {
            while (true) {
                lock.writerEnter(id);
                lock.writerExit(id);
                Thread.sleep(300);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class RwLockSemaforos {
    public static void main(String[] args) {
        RWLock lock = new RWLock();

        // cria leitores
        for (int i = 1; i <= 5; i++) {
            new Thread(new Reader(lock, i)).start();
        }

        // cria escritores
        for (int i = 1; i <= 2; i++) {
            new Thread(new Writer(lock, i)).start();
        }
    }
}

