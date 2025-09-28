import java.util.LinkedList;
import java.util.Queue;

public class ProdConsCondVars {

    // --- Ponto de Entrada da Simulação ---
    public static void main(String[] args) {
        final int BUFFER_SIZE = 5;
        final int NUM_ITEMS = 15;

        System.out.println("Iniciando simulação Produtor/Consumidor em um único arquivo Java.");

        SharedBuffer sharedBuffer = new SharedBuffer(BUFFER_SIZE);

        Producer producerTask = new Producer(sharedBuffer, NUM_ITEMS);
        Consumer consumerTask = new Consumer(sharedBuffer, NUM_ITEMS);

        Thread producerThread = new Thread(producerTask);
        Thread consumerThread = new Thread(consumerTask);

        producerThread.start();
        consumerThread.start();

        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Simulação finalizada.");
    }

    static class SharedBuffer {
        private final Queue<Integer> buffer;
        private final int capacity;

        public SharedBuffer(int capacity) {
            this.buffer = new LinkedList<>();
            this.capacity = capacity;
        }

        public synchronized void produce(int item) throws InterruptedException {
            while (buffer.size() == capacity) {
                System.out.println("Produtor: Buffer CHEIO. Esperando...");
                wait(); 
            }
            buffer.add(item);
            System.out.printf("Produtor: Produziu o item %d. Itens no buffer: %d\n", item, buffer.size());
            notifyAll(); 
        }

        public synchronized int consume() throws InterruptedException {
            while (buffer.isEmpty()) {
                System.out.println("Consumidor: Buffer VAZIO. Esperando...");
                wait();
            }
            int item = buffer.remove();
            System.out.printf("Consumidor: Consumiu o item %d. Itens no buffer: %d\n", item, buffer.size());
            notifyAll(); 
            return item;
        }
    }

    static class Producer implements Runnable {
        private final SharedBuffer buffer;
        private final int numItems;

        public Producer(SharedBuffer buffer, int numItems) {
            this.buffer = buffer;
            this.numItems = numItems;
        }

        @Override
        public void run() {
            for (int i = 0; i < numItems; i++) {
                try {
                    buffer.produce(i);
                    Thread.sleep(100); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Produtor foi interrompido.");
                }
            }
        }
    }

    static class Consumer implements Runnable {
        private final SharedBuffer buffer;
        private final int numItems;

        public Consumer(SharedBuffer buffer, int numItems) {
            this.buffer = buffer;
            this.numItems = numItems;
        }

        @Override
        public void run() {
            for (int i = 0; i < numItems; i++) {
                try {
                    buffer.consume();
                    Thread.sleep(200); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Consumidor foi interrompido.");
                }
            }
        }
    }
}