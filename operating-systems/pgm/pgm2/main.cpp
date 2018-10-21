#include <stdlib.h>
#include <pthread.h>
#include <iostream>
#include <fcntl.h>
#include <sys/stat.h>
#include <semaphore.h>

static const unsigned int SIZE = 1000;
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
    if(head == tail) {
        std::cout << "C: on fetch: head == tail" << std::endl;
        return 0;
    }
    float value = buffer[tail];
    tail++;
    return value;
}

// void consume(float sum, float total) {
//     total += sum;
// }

void printTotals() {
    for(int i = 0; i < NUM_THREADS - 1; i++) {
        std::cout << "Consumer Thread " << i << " Total = " << total[i] << std::endl;   
    }
}

void *producer(void *tid) {
    for(int i = 0; i < SIZE; i++) {
        float sum = produce();
        std::cout << "P: waiting for buf" << std::endl;
        sem_wait(buf);          // wait for buffer to be released
        std::cout << "P: appending" << std::endl;
        append(sum);
        sem_post(buf);          // signal that the buffer is released
        sem_post(notEmpty);     // signal that the buffer has at least one value to be consumed
    }
    std::cout << "DONE !!!" << std::endl;
    done = true;
    printTotals();
    pthread_exit(NULL);
}

void *consumer(void *tid) {
    //float total = total[(long(tid))];
    while(!done) {
        std::cout << "C: waiting for notEmpty" << std::endl;
        sem_wait(notEmpty);     // wait for there to be a value in the buffer
        std::cout << "C: waiting for buf" << std::endl;
        sem_wait(buf);          // wait for buffer to be released
        std::cout << "C: fetching" << std::endl;
        float value = fetch();
        sem_post(buf);          // signal that the buffer is released
        std::cout << "C: consuming" << std::endl;
        total[(long(tid))] += value;
    }
    //std::cout << "Consumer " << (long)tid << " Total: " << total <<std::endl;
    pthread_exit(NULL);
}

// TESTING
// void *PrintHello(void *threadid) {
//     sem_wait(start);
//     long tid;
//     tid = (long)threadid;
//     std::cout << "Hello World! Thread ID, " << tid << std::endl;
//     pthread_exit(NULL);
// }


int main() {
	std::cout << "--- Program 2 : Producer Consumer" << std::endl;
    std::cout << "--- Author    : Edward Hong" << std::endl;


    // TESTING
    pthread_t threads[NUM_THREADS];
    int rc;
    
    // Create the consumer threads first
    for(int i = 0; i < NUM_THREADS - 1; i++ ) {
        std::cout << "main() : creating consumer thread, " << i << std::endl;
        rc = pthread_create(&threads[i], NULL, consumer, (void *)i);
    }

    // Create the producer thread
    std::cout << "main() : creating producer thread, " << (NUM_THREADS - 1) << std::endl;
    rc = pthread_create(&threads[NUM_THREADS - 1], NULL, producer, (void *)(NUM_THREADS - 1));

    // sem_post(start);
    // sem_post(start);
    // sem_post(start);
    // sem_post(start);
    // sem_post(start);

    pthread_exit(NULL);
}