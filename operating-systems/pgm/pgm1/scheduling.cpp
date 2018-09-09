// TODO: Finish Commenting
#include <iostream>
#include <queue>
#include <vector>
#include <stdlib.h>
#include "Process.h"

// * Constants
static const unsigned int NUM_SIMULATIONS = 1;
static const unsigned int NUM_PROCESSES = 20;
static const unsigned int NUM_CYCLES = 200;

// * Variable Declarations
//int simulations[NUM_SIMULATIONS]; // not sure if this is actually necessary but TODO: REMEMBER TO CHANGE THIS BACK TO 1000
int processes[NUM_PROCESSES];


std::queue<Process> processQueue;


// Represents time in clock cycles
// Array index represents the cycle at which a process finished
// Value in the array represents process id, [1-20], with -1 representing no-op
int cycles[NUM_CYCLES];

// SPN Comparator
struct CompSPN{
    bool operator()(const Process& a, const Process& b){
        return a.serviceTime > b.serviceTime;
    }
};

// SPN Comparator
struct CompSRT{
    bool operator()(const Process& a, const Process& b){
        return a.remainingTime > b.remainingTime;
    }
};

// * Function Declarations
void init();
void fcfs();
void showTimeline();

template<typename T> void printQueue(T& q) {
    while(!q.empty()) {
        std::cout << q.top().pid << " ";
        q.pop();
    }
    std::cout << '\n';
}

void printProcessQueue() {
    while(!processQueue.empty()) {
        std::cout << processQueue.front().pid << " ";
        processQueue.pop();
    }
    std::cout << '\n';
}

int main()
{
    init();

    for (unsigned int i = 0; i < NUM_SIMULATIONS; ++i)
    {
        std::cout << "> Simulation " << i << " running..." << std::endl;

        std::priority_queue<Process, std::vector<Process>, CompSPN> SPNQueue;
        std::priority_queue<Process, std::vector<Process>, CompSRT> SRTQueue;


        for(unsigned int pid = 0; pid < NUM_PROCESSES; ++pid)
        {
            // Assigning a random Service Time between 1 and 10
            int serviceTime = rand() % 10 + 1;
            Process p = Process(pid, serviceTime);
            
            processQueue.push(p);
            SPNQueue.push(p);
            SRTQueue.push(p);
            
            std::cout << "  Process " << pid << " ST = " << serviceTime << std::endl;
        }

        // FCFS Simulation
        //fcfs();
        //showTimeline();

        std::cout << "PROCESS QUEUE" << std::endl;
        printProcessQueue();
        
        std::cout << "SPN QUEUE" << std::endl;
        printQueue(SPNQueue);

        std::cout << "SRT QUEUE" << std::endl;
        printQueue(SRTQueue);


    }

	return 0;
}

// TODO: ARRIVAL TIMES - right now this essentially assumes all processes arrive at time 0
void fcfs()
{
    std::cout << "- FCFS:" << std::endl;
    
    int currentTime = 0;

    // Executing each process in order
    for(unsigned int p = 0; p < NUM_PROCESSES; ++p)
    {
        // Ensures the process has arrived by inserting no-op cycles
        // Arrival time is twice the process index, p
        int noops = (2*p) - currentTime; 
        for(int n = 0; n < noops; ++n)
        {
            cycles[currentTime] = -1;
            ++currentTime;
        }

        // Executes a single process to completion
        for(unsigned int i = processes[p]; i > 0; --i)
        {
            cycles[currentTime] = p;
            ++currentTime;
        }
    }
}

// For Round-Robin the currently executing process (which was interrupted) gets placed in the BACK of the queue
// Consider using a queue to hold the processes as they arrive, then iterate through time to perform scheduling

void showTimeline() 
{
    for(unsigned int c = 0; c < NUM_CYCLES; ++c)
    {
        std::cout << cycles[c] << " ";
    }
    std::cout << std::endl;
}

// Prints Program Name and Author to the console
// and seeds the Random Number Generator
void init()
{
	std::cout << "--- Program 1 : Uniprocessor Scheduling Benchmarks" << std::endl;
    std::cout << "--- Author    : Edward Hong" << std::endl;
    
    // Seed our RNG
    srand(time(NULL));
}