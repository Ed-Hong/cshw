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

// Prints Program Name and Author to the console
// and seeds the Random Number Generator
void init() {
	std::cout << "--- Program 1 : Uniprocessor Scheduling Benchmarks" << std::endl;
    std::cout << "--- Author    : Edward Hong" << std::endl;
    
    // Seed our RNG
    srand(time(NULL));
}

void showProcessDetails(const Process& proc) {
    std::cout << "> PROCESS FINISHED" << std::endl;
    std::cout << "PID:" << proc.pid << " " << std::endl;
    std::cout << "AT:" << proc.arrivalTime << " " << std::endl;
    std::cout << "ST:" << proc.serviceTime << " " << std::endl;
    std::cout << "FT:" << proc.finishTime << " " << std::endl;
    std::cout << "TT:" << proc.turnaround << " " << std::endl;
    std::cout << "RTT:" << proc.relativeTurnaround << " " << std::endl;
    std::cout << std::endl;
}

void fcfs() {
    while(!q_fcfs.empty()) {
        Process currentProcess = q_fcfs.front();

        // Check that the process has arrived
        if(currentProcess.arrivalTime <= Process::currentTime) {

            // Executing a single process to completion
            for(unsigned int t = currentProcess.remainingTime; t > 0; --t) {
                ++Process::currentTime;
                currentProcess.process(1);
            }
            showProcessDetails(currentProcess);
            q_fcfs.pop();
        } else {
            ++Process::currentTime;
        }
    }
}

void rr() {
    while(!q_rr.empty()) {
        Process currentProcess = q_rr.front();

        // Check that the process has arrived
        if(currentProcess.arrivalTime <= Process::currentTime) {

            // Executing a single process for one cycle (q = 1)
            currentProcess.process(1);
            q_rr.pop_front();

            Process nextProcess = q_rr.front();

            // Place process back into the queue if it hasn't finished executing
            if(currentProcess.remainingTime > 0) {
                // If the next process after current process hasn't arrived yet, then continue executing current process
                if(nextProcess.arrivalTime > Process::currentTime) {
                    q_rr.push_front(currentProcess);
                } else {
                    q_rr.push_back(currentProcess);
                }
            } else {
                showProcessDetails(currentProcess);
            }
        }
        ++Process::currentTime;
    }
}

void spn_srt() {
    while(!q_procs.empty() || !pq_spn.empty() || !pq_srt.empty()) {
        Process currentProcess = q_procs.front();

        // Check that the process has arrived
        if(!q_procs.empty() && currentProcess.arrivalTime <= Process::currentTime) {
            q_procs.pop();
            pq_spn.push(currentProcess);
            pq_srt.push(currentProcess);
        }

        // Processing Shortest Process Next 
        if(!pq_spn.empty()) {
            Process shortestProc = pq_spn.top();
            pq_spn.pop();

            // Executing process with highest priority (in this case the shortest process) for one cycle
            shortestProc.process(1);
            
            if(shortestProc.remainingTime == 0) {
                std::cout << "--- SPN ---" << std::endl;
                showProcessDetails(shortestProc);
            } else {
                pq_spn.push(shortestProc);
            }
        }

        // Processing Shortest Remaining Time Next 
        if(!pq_srt.empty()) {
            Process shortestRemainingProc = pq_srt.top();
            pq_srt.pop();

            // Executing process with highest priority (in this case the shortest process) for one cycle
            shortestRemainingProc.process(1);

            if(shortestRemainingProc.remainingTime == 0) {
                std::cout << "--- SRT ---" << std::endl;
                showProcessDetails(shortestRemainingProc);
            } else {
                pq_srt.push(shortestRemainingProc);
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

            q_procs.push(p);
            q_fcfs.push(p);
            q_rr.push_back(p);
            
            std::cout << "  Process " << pid << " ST = " << serviceTime << std::endl;
        }

        std::cout << "--- FCFS ---" << std::endl;
        fcfs();                     // Simulate FCFS scheduling algorithm
        Process::currentTime = 0;   // Reset Processes' current time
        
        std::cout << "--- RR ---" << std::endl;
        rr();                       // Simulate RR scheduling algorithm
        Process::currentTime = 0;   // Reset Processes' current time

        spn_srt();                  // Simulate SPN and SRT scheduling algorithms
    }

	return 0;
}