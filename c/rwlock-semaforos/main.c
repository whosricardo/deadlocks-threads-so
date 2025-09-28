#include <pthread.h>
#include <semaphore.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

sem_t mutex;        // protege contadores
sem_t roomEmpty;    // garante exclusão mútua entre leitores e escritores
sem_t turnstile;    // prioriza escritores

int readers = 0;    // número de leitores ativos

// --- Funções dos leitores ---
void* reader(void* arg) {
    int id = *(int*)arg;

    while (1) {
        sem_wait(&turnstile);   // passa pela "catraca"
        sem_post(&turnstile);

        sem_wait(&mutex);
        readers++;
        if (readers == 1)
            sem_wait(&roomEmpty); // primeiro leitor bloqueia escritores
        sem_post(&mutex);

        // seção crítica de leitura
        printf("Leitor %d está lendo...\n", id);
        usleep(100000); // simula leitura

        sem_wait(&mutex);
        readers--;
        if (readers == 0)
            sem_post(&roomEmpty); // último leitor libera escritores
        sem_post(&mutex);

        usleep(200000); // tempo fora da SC
    }
    return NULL;
}

// --- Funções dos escritores ---
void* writer(void* arg) {
    int id = *(int*)arg;

    while (1) {
        sem_wait(&turnstile);   // impede novos leitores de entrar
        sem_wait(&roomEmpty);   // garante exclusividade
        // seção crítica de escrita
        printf("Escritor %d está escrevendo...\n", id);
        usleep(150000); // simula escrita
        sem_post(&roomEmpty);
        sem_post(&turnstile);

        usleep(300000); // tempo fora da SC
    }
    return NULL;
}

// --- Programa principal ---
int main() {
    pthread_t rtid[5], wtid[2];
    int ids[5];

    sem_init(&mutex, 0, 1);
    sem_init(&roomEmpty, 0, 1);
    sem_init(&turnstile, 0, 1);

    for (int i = 0; i < 5; i++) {
        ids[i] = i + 1;
        pthread_create(&rtid[i], NULL, reader, &ids[i]);
    }
    for (int i = 0; i < 2; i++) {
        ids[i] = i + 1;
        pthread_create(&wtid[i], NULL, writer, &ids[i]);
    }

    for (int i = 0; i < 5; i++)
        pthread_join(rtid[i], NULL);
    for (int i = 0; i < 2; i++)
        pthread_join(wtid[i], NULL);

    sem_destroy(&mutex);
    sem_destroy(&roomEmpty);
    sem_destroy(&turnstile);

    return 0;
}

