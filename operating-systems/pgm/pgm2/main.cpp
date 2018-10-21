#include <stdlib.h>
#include <pthread.h>
#include <iostream>
#include <fcntl.h>
#include <sys/stat.h>
#include <semaphore.h>

static const unsigned int SIZE = 1000000;
static const unsigned int NUM_THREADS = 11;
float buffer[SIZE];
float total[NUM_THREADS];
int head = 0, tail = 0;
bool done = false;

sem_t *buf = sem_open("bufSem", O_CREAT | O_EXCL, 0600, 1);
sem_t *notEmpty = sem_open("notEmptySem", O_CREAT | O_EXCL, 0600, 0);

float produce() {
    float sum = 0;
    for(int i = 0; i < 1000; i++) {
        float random = (rand() / (RAND_MAX+1.0)); 
        sum += random;
    }
    return sum / 500;
}

void append(float value) {
    buffer[head] = value;
    head++;
}

float fetch() {
    if(tail >= head) {
        return 0;
    }
    float value = buffer[tail];
    tail++;
    return value;
}

void printTotals() {
    std::cout << std::endl;
    std::cout << "--- Totals ---" << std::endl;
    for(int i = 0; i < NUM_THREADS - 1; i++) {
        std::cout << "Consumer Thread " << i << " Total = " << total[i] << std::endl;   
    }
}

void *producer(void *tid) {
    for(int i = 0; i < SIZE; i++) {
        float sum = produce();
        sem_wait(buf);          // wait for buffer to be released
        append(sum);
        sem_post(buf);          // signal that the buffer is released
        sem_post(notEmpty);     // signal that the buffer has at least one value to be consumed
    }
    done = true;
    printTotals();
    sem_close(buf);
    sem_close(notEmpty);
    pthread_exit(NULL);
}

void *consumer(void *tid) {
    while(!done) {
        sem_wait(notEmpty);     // wait for there to be a value in the buffer
        sem_wait(buf);          // wait for buffer to be released
        float value = fetch();
        sem_post(buf);          // signal that the buffer is released
        //std::cout << ".";
        total[(long(tid))] += value;
    }
    pthread_exit(NULL);
}

int main() {
	std::cout << "--- Program 2 : Producer Consumer" << std::endl;
    std::cout << "--- Author    : Edward Hong" << std::endl;

    pthread_t threads[NUM_THREADS];
    
    // Create the consumer threads first
    for(int i = 0; i < NUM_THREADS - 1; i++ ) {
        pthread_create(&threads[i], NULL, consumer, (void *)i);
    }

    // Create the producer thread
    pthread_create(&threads[NUM_THREADS - 1], NULL, producer, (void *)(NUM_THREADS - 1));

    pthread_exit(NULL);
}