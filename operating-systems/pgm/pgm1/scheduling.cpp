// TODO: Finish Commenting
#include <iostream>
#include <queue>
#include <vector>
#include <stdlib.h>
#include "Process.h"

// * Constants
static const unsigned int NUM_SIMULATIONS = 1;  // TODO: REMEMBER TO CHANGE THIS BACK TO 1000
static const unsigned int NUM_PROCESSES = 20;

// SPN Comparator
struct comp_spn {
    bool operator()(const Process& a, const Process& b){
        return a.serviceTime > b.serviceTime;
    }
};

// SPN Comparator
struct comp_srt {
    bool operator()(const Process& a, const Process& b){
        return a.remainingTime > b.remainingTime;
    }
};

// Queue Declarations for the four scheduling algorithms
std::queue<Process> q_fcfs;
std::queue<Process> q_rr;
std::priority_queue<Process, std::vector<Process>, comp_spn> pq_spn;
std::priority_queue<Process, std::vector<Process>, comp_srt> pq_srt;

template<typename T> void printQueue(T& q) {
    while(!q.empty()) {
        std::cout << q.top().pid << " ";
        q.pop();
    }
    std::cout << '\n';
}

// Prints Program Name and Author to the console
// and seeds the Random Number Generator
void init() {
	std::cout << "--- Program 1 : Uniprocessor Scheduling Benchmarks" << std::endl;
    std::cout << "--- Author    : Edward Hong" << std::endl;
    
    // Seed our RNG
    srand(time(NULL));
}

void fcfs() {
    std::cout << "- FCFS:" << std::endl;

    // Executing each process in order of FCFS
    while(!q_fcfs.empty()) {
        Process currentProcess = q_fcfs.front();

        // Check that the process has arrived
        if(currentProcess.arrivalTime <= Process::currentTime) {
            // Executing a single process to completion
            for(unsigned int t = currentProcess.remainingTime; t > 0; --t) {
                ++Process::currentTime;
                currentProcess.process(1);
            }
            
            std::cout << "PID:" << currentProcess.pid << " " << std::endl;
            std::cout << "ST:" << currentProcess.serviceTime << " " << std::endl;
            std::cout << "FT:" << currentProcess.finishTime << " " << std::endl;
            std::cout << "T:" << currentProcess.turnaround << " " << std::endl;
            std::cout << "RT:" << currentProcess.relativeTurnaround << " " << std::endl;
            std::cout << std::endl;

            q_fcfs.pop();
        } else {
            ++Process::currentTime;
        }
    }
}

void rr() {
    std::cout << "- RR:" << std::endl;

    // Executing each process in order of RR
    while(!q_rr.empty()) {
        Process currentProcess = q_rr.front();

        // Check that the process has arrived
        if(currentProcess.arrivalTime <= Process::currentTime) {
            // Executing a single process for one cycle (q = 1)
            ++Process::currentTime;
            currentProcess.process(1);
            q_rr.pop();

            // Place process back into the queue if it hasn't finished executing
            if(currentProcess.remainingTime > 0) {
                q_rr.push(currentProcess);
            }

        } else {
            ++Process::currentTime;
        }

        std::cout << "PID:" << currentProcess.pid << " " << std::endl;
        std::cout << "ST:" << currentProcess.serviceTime << " " << std::endl;
        std::cout << "FT:" << currentProcess.finishTime << " " << std::endl;
        std::cout << "T:" << currentProcess.turnaround << " " << std::endl;
        std::cout << "RT:" << currentProcess.relativeTurnaround << " " << std::endl;
        std::cout << std::endl;
    }
}

// For Round-Robin the currently executing process (which was interrupted) gets placed in the BACK of the queue
// Consider using a queue to hold the processes as they arrive, then iterate through time to perform scheduling

int main() {
    
    init();

    for (unsigned int i = 0; i < NUM_SIMULATIONS; ++i)
    {
        std::cout << "> Simulation " << i << " running..." << std::endl;


        // Populating the queues with processes of random service time
        for(unsigned int pid = 0; pid < NUM_PROCESSES; ++pid)
        {
            // Assigning a random Service Time between 1 and 10
            int serviceTime = rand() % 10 + 1;
            Process p = Process(pid, serviceTime);
            
            //debug
            //Process p = Process(pid, 1);


            q_fcfs.push(p);
            q_rr.push(p);
            pq_spn.push(p);
            pq_srt.push(p);
            
            std::cout << "  Process " << pid << " ST = " << serviceTime << std::endl;
        }

        fcfs();     // Simulate FCFS scheduling algorithm
        
        rr();     // Simulate RR scheduling algorithm

        std::cout << "SPN QUEUE" << std::endl;
        printQueue(pq_spn);

        std::cout << "SRT QUEUE" << std::endl;
        printQueue(pq_srt);


    }

	return 0;
}