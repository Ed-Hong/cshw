// TODO: Finish Commenting
#include <iostream>
#include <queue>
#include <deque>
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

// Queue for storing processes in order of arrival
std::queue<Process> q_procs;

// Queue for the FCFS Algorithm
std::queue<Process> q_fcfs;

// Deque for the RR Algorithm, so that a process can be placed back into the front of the queue
// in the event that the next process hasn't arrived, and the current process can continue executing
std::deque<Process> q_rr;

// Priority Queues for the SPN and SRT algorithms using the above comparators
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
        std::cout << "TIME:" << Process::currentTime << " " << std::endl;

        Process currentProcess = q_rr.front();

        // Check that the process has arrived
        if(currentProcess.arrivalTime <= Process::currentTime) {
            // Executing a single process for one cycle (q = 1)
            currentProcess.process(1);
            q_rr.pop_front();

            Process nextProcess = q_rr.front();

            std::cout << "PID:" << currentProcess.pid << " " << std::endl;
            std::cout << "remaining:" << currentProcess.remainingTime << " " << std::endl;
            std::cout << std::endl;

            // Place process back into the queue if it hasn't finished executing
            if(currentProcess.remainingTime > 0) {
                // If the next process after current process hasn't arrived yet, then continue executing current process
                if(nextProcess.arrivalTime > Process::currentTime) {
                    q_rr.push_front(currentProcess);
                } else {
                    q_rr.push_back(currentProcess);
                }
            } else {
                std::cout << "FINISHED PID:" << currentProcess.pid << " " << std::endl;
                std::cout << "FT:" << currentProcess.finishTime << " " << std::endl;
                std::cout << "T:" << currentProcess.turnaround << " " << std::endl;
                std::cout << "RT:" << currentProcess.relativeTurnaround << " " << std::endl;
                std::cout << std::endl;
            }
        }
        ++Process::currentTime;
    }
}

void spn() {
    std::cout << "- SPN:" << std::endl;

    // Executing each process in order of SPN
    while(!q_procs.empty()) {
        std::cout << "TIME:" << Process::currentTime << " " << std::endl;

        Process currentProcess = q_procs.front();

        // Check that the process has arrived
        if(currentProcess.arrivalTime <= Process::currentTime) {
            q_procs.pop();
            pq_spn.push(currentProcess);
        }

        if(!pq_spn.empty()) {

            Process shortestProc = pq_spn.top();
            pq_spn.pop();

            // Executing process with highest priority (in this case the shortest process) for one cycle
            shortestProc.process(1);
            
            std::cout << "PID:" << shortestProc.pid << " " << std::endl;
            std::cout << "remaining:" << shortestProc.remainingTime << " " << std::endl;
            std::cout << std::endl;

            if(shortestProc.remainingTime == 0) {
                std::cout << "FINISHED PID:" << shortestProc.pid << " " << std::endl;
                std::cout << "ST:" << shortestProc.serviceTime << " " << std::endl;
                std::cout << "FT:" << shortestProc.finishTime << " " << std::endl;
                std::cout << "T:" << shortestProc.turnaround << " " << std::endl;
                std::cout << "RT:" << shortestProc.relativeTurnaround << " " << std::endl;
                std::cout << std::endl;
            } else {
                pq_spn.push(shortestProc);
            }
        }
        ++Process::currentTime;
    }
}

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

            q_procs.push(p);
            q_fcfs.push(p);
            q_rr.push_back(p);
            //pq_spn.push(p);
            //pq_srt.push(p);
            
            std::cout << "  Process " << pid << " ST = " << serviceTime << std::endl;
        }

        //fcfs();     // Simulate FCFS scheduling algorithm
        
        //rr();     // Simulate RR scheduling algorithm

        spn();

        // std::cout << "SPN QUEUE" << std::endl;
        // printQueue(pq_spn);

        // std::cout << "SRT QUEUE" << std::endl;
        // printQueue(pq_srt);


    }

	return 0;
}