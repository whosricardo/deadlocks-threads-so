#include <stdio.h>
#include <pthread.h>
#include <unistd.h>

#define N_READERS 5
#define N_WRITERS 2

pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t cond = PTHREAD_COND_INITIALIZER;

int readers = 0;         
int writers = 0;         
int waiting_writers = 0; 
int shared_data = 0;    

void *reader(void *arg) {
    int id = *(int *)arg;
    while (1) {
        pthread_mutex_lock(&mutex);
        while (writers > 0 || waiting_writers > 0) {
            pthread_cond_wait(&cond, &mutex);
        }
        readers++;
        pthread_mutex_unlock(&mutex);


        printf("Leitor %d leu valor: %d\n", id, shared_data);


        pthread_mutex_lock(&mutex);
        readers--;
        if (readers == 0) pthread_cond_broadcast(&cond); 
        pthread_mutex_unlock(&mutex);

        usleep(200000);
    }
    return NULL;
}

void *writer(void *arg) {
    int id = *(int *)arg;
    while (1) {
        pthread_mutex_lock(&mutex);
        waiting_writers++;
        while (readers > 0 || writers > 0) {
            pthread_cond_wait(&cond, &mutex);
        }
        waiting_writers--;
        writers = 1;
        pthread_mutex_unlock(&mutex);

        shared_data++;
        printf(">>> Escritor %d escreveu valor: %d\n", id, shared_data);


        pthread_mutex_lock(&mutex);
        writers = 0;
        pthread_cond_broadcast(&cond);
        pthread_mutex_unlock(&mutex);

        usleep(400000);
    }
    return NULL;
}

int main() {
    pthread_t r[N_READERS], w[N_WRITERS];
    int id_r[N_READERS], id_w[N_WRITERS];

    for (int i = 0; i < N_READERS; i++) {
        id_r[i] = i + 1;
        pthread_create(&r[i], NULL, reader, &id_r[i]);
    }
    for (int i = 0; i < N_WRITERS; i++) {
        id_w[i] = i + 1;
        pthread_create(&w[i], NULL, writer, &id_w[i]);
    }

    for (int i = 0; i < N_READERS; i++) pthread_join(r[i], NULL);
    for (int i = 0; i < N_WRITERS; i++) pthread_join(w[i], NULL);

    return 0;
}
