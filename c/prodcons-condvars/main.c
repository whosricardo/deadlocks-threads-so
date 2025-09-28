#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>

#define BUFFER_SIZE 5   
#define NUM_ITEMS 15    


typedef struct {
    int buffer[BUFFER_SIZE];
    int in;               
    int out;              
    int count;            
    pthread_mutex_t mutex; 
    pthread_cond_t cond_cheio; 
    pthread_cond_t cond_vazio; 
} shared_buffer;

shared_buffer shb;


void initialize_buffer(shared_buffer *sb) {
    sb->in = 0;
    sb->out = 0;
    sb->count = 0;
    pthread_mutex_init(&sb->mutex, NULL);
    pthread_cond_init(&sb->cond_cheio, NULL);
    pthread_cond_init(&sb->cond_vazio, NULL);
}


void* produtor(void* arg) {
    for (int i = 0; i < NUM_ITEMS; ++i) {
        int item = i * 10; 

        pthread_mutex_lock(&shb.mutex);

        while (shb.count == BUFFER_SIZE) {
            printf("Produtor: Buffer CHEIO. Esperando...\n");
            pthread_cond_wait(&shb.cond_vazio, &shb.mutex);
        }

  
        shb.buffer[shb.in] = item;
        shb.in = (shb.in + 1) % BUFFER_SIZE;
        shb.count++;
        printf("Produtor: Produziu o item %d. Itens no buffer: %d\n", item, shb.count);

     
        pthread_cond_signal(&shb.cond_cheio);

        pthread_mutex_unlock(&shb.mutex);

        sleep(1);
    }
    return NULL;
}

void* consumidor(void* arg) {
    for (int i = 0; i < NUM_ITEMS; ++i) {
      
        pthread_mutex_lock(&shb.mutex);

        while (shb.count == 0) {
            printf("Consumidor: Buffer VAZIO. Esperando...\n");
            pthread_cond_wait(&shb.cond_cheio, &shb.mutex);
        }

        int item = shb.buffer[shb.out];
        shb.out = (shb.out + 1) % BUFFER_SIZE;
        shb.count--;
        printf("Consumidor: Consumiu o item %d. Itens no buffer: %d\n", item, shb.count);

        pthread_cond_signal(&shb.cond_vazio);

        pthread_mutex_unlock(&shb.mutex);

        sleep(2);
    }
    return NULL;
}

int main() {
    pthread_t produtor_tid, consumidor_tid;

    printf("Iniciando simulação Produtor/Consumidor com Variáveis de Condição.\n");

    initialize_buffer(&shb);

    pthread_create(&produtor_tid, NULL, produtor, NULL);
    pthread_create(&consumidor_tid, NULL, consumidor, NULL);

    pthread_join(produtor_tid, NULL);
    pthread_join(consumidor_tid, NULL);

    pthread_mutex_destroy(&shb.mutex);
    pthread_cond_destroy(&shb.cond_cheio);
    pthread_cond_destroy(&shb.cond_vazio);

    printf("Simulação finalizada.\n");

    return 0;
}