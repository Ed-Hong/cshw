/*********************************************************************************************************************************
* File: main.cpp
* Author: Edward Hong
* Procedures:
* main - creates the 10 consumer threads and the 1 producer thread
*********************************************************************************************************************************/
#include <stdlib.h>
#include <iostream>
#include <unordered_map>
#include <map>
#include <algorithm>

// Constants
static const unsigned int NUM_EXPERIMENTS = 3;
static const unsigned int TRACE_SIZE = 1000;
static const unsigned int MAX_WORKING_SET_SIZE = 20;

// Encoding the four page replacement algorithms
static const unsigned int LRU = 0;
static const unsigned int FIFO = 1;
static const unsigned int CLOCK = 2;
static const unsigned int RANDOM = 3;

int trace[TRACE_SIZE];
int faults[MAX_WORKING_SET_SIZE][4];

/**************************************************************************************************************
* int uniform()
* Author: Edward Hong
* Date: 21 November 2018
* Description:  Generates a uniform random integer number within the range [lo, hi]
* Parameters:
* lo I/P int The lower bound of the uniform random number distribution, inclusive
* hi I/P int The upper bound of the uniform random number distribution, inclusive
**************************************************************************************************************/
int uniform(int lo, int hi) {
    int x;
    while((x = random()/(hi-lo+1)) > (hi-lo));
    return x + lo;
}

/**************************************************************************************************************
* int lru(int wss)
* Author: Edward Hong
* Date: 21 November 2018
* Description:  Runs through a Least-Recently-Used (LRU) page replacement algorithm for the given
                working set size
* Parameters:
* wss I/P int The working set size
**************************************************************************************************************/
int lru(int wss) {
        
    // Count of page faults
    int faults = 0;

    // Current trace index
    int ti = 0;

    // Hashmap acting as our working set
    // Key being the page stored in the working set
    // Value being the last time the page was used
    std::unordered_map<int, int> ws;

    // Populate working set with initial addresses
    for(int i = 0; i < wss; i++) {
        ws[trace[ti]] = ti;
        ti++;
        std::cout << "Working set at " << i << " = " << trace[ti] << std::endl;
    }

    // Simulating the rest of the page address stream
    for(; ti < TRACE_SIZE; ti++) {
        std::cout << "Attempting to find page " << trace[ti] << std::endl;
 
        if (ws.find(trace[ti]) == ws.end()) {
            std::cout << "Page NOT found in working set; PAGE FAULT "<< std::endl;

            // Find the Least Recently Used item
            int leastVal = INT_MAX;
            int leastKey = 0;
            for (auto itr = ws.begin(); itr != ws.end(); itr++) 
            {
                if(itr->second < leastVal) {
                    leastVal = itr->second;
                    leastKey = itr->first;
                }
            }

            // Remove the Least Recently Used item by key
            ws.erase(leastKey);

            // Replace the removed item with new item
            ws[trace[ti]] = ti;
        }      
        else{
            std::cout << "Page found in working set: " << trace[ti] << std::endl;
            
            // Update the last time the page was accessed
            ws[trace[ti]] = ti;
        }
    }

    return faults;
}

/**************************************************************************************************************
* int fifo(int wss)
* Author: Edward Hong
* Date: 21 November 2018
* Description:  Runs through a First-In-First-Out (FIFO) page replacement algorithm for the given
                working set size
* Parameters:
* wss I/P int The working set size
**************************************************************************************************************/
int fifo(int wss) {
    
    // Count of page faults
    int faults = 0;

    // Current trace index
    int ti = 0;

    // Replacement index
    int replace = 0;

    // Array acting as our working set
    int ws[wss];

    // Populate working set with initial addresses
    for(int i = 0; i < wss; i++) {
        ws[i] = trace[ti];
        ti++;
        std::cout << "Working set at " << i << " = " << ws[i] << std::endl;
    }

    // Simulating the rest of the page address stream
    for(; ti < TRACE_SIZE; ti++) {
        std::cout << "Attempting to find page " << trace[ti] << std::endl;

        // Attempt to find the next page within the working set
        int * p;
        p = std::find (ws, ws+wss, trace[ti]);
        if (p != ws+wss) {
            std::cout << "Page found in working set: " << *p << std::endl;
        }
        else {
            std::cout << "Page NOT found in working set; PAGE FAULT "<< std::endl;
            faults++;

            // Random replacement
            ws[replace] = trace[ti];
            replace++;
            replace = replace % wss;
        }
    }

    return faults;
}

/**************************************************************************************************************
* int clk(int wss)
* Author: Edward Hong
* Date: 21 November 2018
* Description:  Runs through a Clock Policy page replacement algorithm for the given working set size
* Parameters:
* wss I/P int The working set size
**************************************************************************************************************/
int clk(int wss) {
        
    // Count of page faults
    int faults = 0;

    // Current trace index
    int ti = 0;

    // Ordered map acting as our circular buffer for the working set
    // Key being the page stored in the working set
    // Value being a bool representing the use bit
    std::map<int, bool> ws;

    // Populate working set with initial addresses
    for(int i = 0; i < wss; i++) {
        ws[trace[ti]] = true;
        ti++;
        std::cout << "Working set at " << i << " = " << trace[ti] << std::endl;
    }

    // Simulating the rest of the page address stream
    for(; ti < TRACE_SIZE; ti++) {
        std::cout << "Attempting to find page " << trace[ti] << std::endl;
 
        if (ws.find(trace[ti]) == ws.end()) {
            std::cout << "Page NOT found in working set; PAGE FAULT "<< std::endl;

            // Find the first item with use bit being false
            int replace = ws.begin()->first;
            for (auto itr = ws.begin(); itr != ws.end(); itr++) 
            {
                if(itr->second == false) {
                    replace = itr->first;
                    break;
                } else {
                    ws[trace[ti]] = false;
                }
            }

            // Remove the Least Recently Used item by key
            ws.erase(replace);

            // Replace the removed item with new item
            ws[trace[ti]] = true;
        }      
        else{
            std::cout << "Page found in working set: " << trace[ti] << std::endl;
            
            // Update the use bit
            ws[trace[ti]] = true;
        }
    }

    return faults;
}

/**************************************************************************************************************
* int rando(int wss)
* Author: Edward Hong
* Date: 21 November 2018
* Description:  Runs through a random page replacement algorithm for the given working set size
* Parameters:
* wss I/P int The working set size
**************************************************************************************************************/
int rando(int wss) {

    // Count of page faults
    int faults = 0;

    // Current trace index
    int ti = 0;

    // Array acting as our working set
    int ws[wss];

    // Populate working set with initial addresses
    for(int i = 0; i < wss; i++) {
        ws[i] = trace[ti];
        ti++;
        std::cout << "Working set at " << i << " = " << ws[i] << std::endl;
    }

    // Simulating the rest of the page address stream
    for(; ti < TRACE_SIZE; ti++) {
        std::cout << "Attempting to find page " << trace[ti] << std::endl;

        // Attempt to find the next page within the working set
        int * p;
        p = std::find (ws, ws+wss, trace[ti]);
        if (p != ws+wss) {
            std::cout << "Page found in working set: " << *p << std::endl;
        }
        else {
            std::cout << "Page NOT found in working set; PAGE FAULT "<< std::endl;
            faults++;

            // Random replacement
            int replace = uniform(0, wss-1);
            ws[replace] = trace[ti];
        }
    }

    return faults;
}

/**************************************************************************************************************
* int uniform()
* Author: Edward Hong
* Date: 21 November 2018
* Description:  Generates a memory trace
**************************************************************************************************************/
int generateMemoryTrace() {
    for(int part = 0; part < 10; part++) {
        int base = 25 * uniform(0,99);
        for(int index = 0; index < 100; index++)
            trace[100*part+index] = base + uniform(0,99);
    }
}

/**************************************************************************************************************
* void main()
* Author: Edward Hong
* Date: 21 November 2018
* Description:  
**************************************************************************************************************/
int main() {
	std::cout << "--- Program 3 : Virtual Memory Page Replacement Algorithms" << std::endl;
    std::cout << "--- Author    : Edward Hong" << std::endl;

    // Running experiments
    for(int expr = 0; expr < NUM_EXPERIMENTS; expr++) {
        std::cout << "--- Running Experiment " << expr << " ---"<< std::endl;
        
        // Reset the trace from the previous experiment
        int trace[TRACE_SIZE];
        
        std::cout << "Generating memory trace..." << std::endl;
        generateMemoryTrace();
        std::cout << "Memory trace generated." << std::endl;

        for(int wss = 2; wss <= MAX_WORKING_SET_SIZE; wss++) {
            // std::cout << "Simulating LRU with working set size " << wss << "..." << std::endl;
            // faults[wss][LRU] += lru(wss);

            // std::cout << "Simulating FIFO with working set size " << wss << "..." << std::endl;
            // faults[wss][FIFO] += fifo(wss);

            std::cout << "Simulating CLK with working set size " << wss << "..." << std::endl;
            faults[wss][CLOCK] += clk(wss);

            // std::cout << "Simulating RANDOM with working set size " << wss << "..." << std::endl;
            // faults[wss][RANDOM] += rando(wss);
        }
    }

    std::cout << "--- Experiments finished running ---" << std::endl;


    // Testing traces
    for(int wss = 2; wss <= MAX_WORKING_SET_SIZE; wss++) {
        std::cout << "- Working Set Size: " << wss << std::endl;

        faults[wss][LRU] += lru(wss);
        faults[wss][FIFO] += fifo(wss);
        faults[wss][CLOCK] += clk(wss);
        faults[wss][RANDOM] += rando(wss);

        std::cout << "LRU =  " << faults[wss][LRU] << std::endl;
        std::cout << "FIFO =  " << faults[wss][FIFO] << std::endl;
        std::cout << "CLOCK =  " << faults[wss][CLOCK] << std::endl;
        std::cout << "RANDOM =  " << faults[wss][RANDOM] << std::endl;
    }
}