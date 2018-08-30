// TODO: Finish Commenting
#include <iostream>
#include <stdlib.h>


// * Variable Declarations
int simulations[1]; // not sure if this is actually necessary but TODO: REMEMBER TO CHANGE THIS BACK TO 1000
int processes[20];

// Represents time in clock cycles
// Array index represents the cycle at which a process finished
// Value in the array represents process id, [1-20], with -1 representing no-op
int cycles[200];

// * Function Declarations
void init();
void fcfs();
void showTimeline();

int main()
{
    init();

    for (int& sim : simulations)
    {
        auto index = &sim - &simulations[0];
        std::cout << "> Simulation " << index << " running..." << std::endl;

        for(int& proc : processes)
        {
            auto index = &proc - &processes[0];

            // Assigning a random Service Time between 1 and 10
            processes[index] = rand() % 10 + 1;
            std::cout << "  Process " << index << " ST = " << processes[index] << std::endl;
        }

        // FCFS Simulation
        fcfs();
        showTimeline();
    }

	return 0;
}

// TODO: ARRIVAL TIMES - right now this essentially assumes all processes arrive at time 0
void fcfs()
{
    std::cout << "- FCFS:" << std::endl;
    
    int currentTime = 0;

    // Executing each process in order
    for(int& proc : processes)
    {
        auto procId = &proc - &processes[0];

        // Executes a single process to completion
        for(unsigned int i = processes[procId]; i > 0; --i)
        {
            cycles[currentTime] = procId;
            ++currentTime;
        }
    }
}

void showTimeline() 
{
    for(int& cycle : cycles)
    {
        auto currentCycle = &cycle - &cycles[0];
        std::cout << cycles[currentCycle] << " ";
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