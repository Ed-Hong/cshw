// TODO: Finish Commenting
#include <iostream>
#include <stdlib.h>


// * Constants
static const unsigned int NUM_SIMULATIONS = 10;
static const unsigned int NUM_PROCESSES = 20;
static const unsigned int NUM_CYCLES = 200;

// * Variable Declarations
int simulations[NUM_SIMULATIONS]; // not sure if this is actually necessary but TODO: REMEMBER TO CHANGE THIS BACK TO 1000
int processes[NUM_PROCESSES];

// Represents time in clock cycles
// Array index represents the cycle at which a process finished
// Value in the array represents process id, [1-20], with -1 representing no-op
int cycles[NUM_CYCLES];

// * Function Declarations
void init();
void fcfs();
void showTimeline();

int main()
{
    init();

    for (unsigned int i = 0; i < NUM_SIMULATIONS; ++i)
    {
        std::cout << "> Simulation " << i << " running..." << std::endl;

        for(unsigned int p = 0; p < NUM_PROCESSES; ++p)
        {
            // Assigning a random Service Time between 1 and 10
            processes[p] = rand() % 10 + 1;
            std::cout << "  Process " << p << " ST = " << processes[p] << std::endl;
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
    for(unsigned int p = 0; p  < NUM_PROCESSES; ++p)
    {
        // Executes a single process to completion
        for(unsigned int i = processes[p]; i > 0; --i)
        {
            cycles[currentTime] = p;
            ++currentTime;
        }
    }
}

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