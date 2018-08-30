// TODO: Finish Commenting
#include <iostream>


// Declarations
void init();
int simulations[1000];
int processes[20];

int main()
{
    init();

    for (int& sim : simulations)
    {
        auto index = &sim - &simulations[0];        
        std::cout << "> Simulation " << index << " running..." << std::endl;

        simulations[index] = 421;
        std::cout << "  Value = " << simulations[index] << std::endl;
    }

	return 0;
}

// Prints Program Name and Author to the console
void init()
{
	std::cout << "--- Program 1 : Uniprocessor Scheduling Benchmarks" << std::endl;
    std::cout << "--- Author    : Edward Hong" << std::endl;
}