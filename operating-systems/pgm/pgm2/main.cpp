/*********************************************************************************************************************************
* File: main.cpp
* Author: Edward Hong
* Procedures:
* main - creates the 10 consumer threads and the 1 producer thread
* produce - returns the sum of a thousand numbers uniformly distributed between 0 and 1, divided by 500
* append - inserts a value into the buffer
* fetch - removes a value from the buffer if the buffer isn't empty
* printTotals - prints the total amount consumed by each of the 10 consumers
* producer - producer thread which will produce values and append them to the buffer using semaphores to ensure mutual exclusion
* consumer - consumer thread which will consume values placed into the buffer using semaphores to ensure mutual exclusion
*********************************************************************************************************************************/
#include <stdlib.h>
#include <pthread.h>
#include <iostream>
#include <fcntl.h>
#include <sys/stat.h>
#include <semaphore.h>

// Constants
static const unsigned int SIZE = 1000000;
static const unsigned int NUM_THREADS = 11;

float buffer[SIZE];
float total[NUM_THREADS];
int head = 0, tail = 0;
bool done = false;

// Semaphore acting as a "lock" for accessing the buffer
sem_t *buf = sem_open("bufSem", O_CREAT | O_EXCL, 0600, 1);

// Semaphore signaling when the buffer contains a value, allowing a consumer to consume
sem_t *notEmpty = sem_open("notEmptySem", O_CREAT | O_EXCL, 0600, 0);

/**************************************************************************************************************
* float produce()
* Author: Edward Hong
* Date: 20 October 2018
* Description:  computes the sum of a thousand floating point numbers uniformly distributed between 0 and 1
                then returns the sum divided by 500
**************************************************************************************************************/
float produce() {
    float sum = 0;
    for(int i = 0; i < 1000; i++) {
        float random = (rand() / (RAND_MAX+1.0)); 
        sum += random;
    }
    return sum / 500;
}

/**************************************************************************************************************
* float produce()
* Author: Edward Hong
* Date: 20 October 2018
* Description:  inserts an item into the buffer and updates the HEAD of the buffer
* Parameters:
* value I/P float The value to be appended to the buffer
**************************************************************************************************************/
void append(float value) {
    buffer[head] = value;
    head++;
}

/**************************************************************************************************************
* float fetch()
* Author: Edward Hong
* Date: 20 October 2018
* Description:  takes an item out of the buffer and updates the TAIL of the buffer
**************************************************************************************************************/
float fetch() {
    if(tail >= head) {
        return 0;
    }
    float value = buffer[tail];
    tail++;
    return value;
}

/**************************************************************************************************************
* float printTotals()
* Author: Edward Hong
* Date: 20 October 2018
* Description:  prints the total amount consumed by each of the consumer threads
**************************************************************************************************************/
void printTotals() {
    std::cout << std::endl;
    std::cout << "--- Totals ---" << std::endl;
    for(int i = 0; i < NUM_THREADS - 1; i++) {
        std::cout << "Consumer Thread " << i << " Total = " << total[i] << std::endl;   
    }
}

/**************************************************************************************************************
* void *producer(void *tid)
* Author: Edward Hong
* Date: 20 October 2018
* Description:  thread for a producer
* Parameters:
* *tid I/P void The thread id
**************************************************************************************************************/
void *producer(void *tid) {
    for(int i = 0; i < SIZE; i++) {
        float sum = produce();
        sem_wait(buf);          // wait for buffer to be released
        append(sum);
        sem_post(buf);          // signal that the buffer is released
        sem_post(notEmpty);     // signal that the buffer has at least one value to be consumed
    }
    done = true;
    printTotals();              // print the totals of each of the consumers
    sem_close(buf);             // close our semaphores
    sem_close(notEmpty);
    pthread_exit(NULL);
}

/**************************************************************************************************************
* void *consumer(void *tid)
* Author: Edward Hong
* Date: 20 October 2018
* Description:  thread for a consumer
* Parameters:
* *tid I/P void The thread id
**************************************************************************************************************/
void *consumer(void *tid) {
    while(!done) {
        sem_wait(notEmpty);             // wait for there to be a value in the buffer
        sem_wait(buf);                  // wait for buffer to be released
        float value = fetch();
        sem_post(buf);                  // signal that the buffer is released
        total[(long(tid))] += value;    // update the total of this consumer
    }
    pthread_exit(NULL);
}

/**************************************************************************************************************
* void main()
* Author: Edward Hong
* Date: 20 October 2018
* Description:  Initializes all 10 consumer threads and the 1 producer thread
**************************************************************************************************************/
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