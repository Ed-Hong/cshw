#include "Process.h"

// Initialize current time for processes
int Process::currentTime = 0;

Process::Process(int pid, int serviceTime) : pid(pid), serviceTime(serviceTime) 
{
    arrivalTime = 2 * pid;
    remainingTime = serviceTime;
    finishTime = -1;
    turnaround = -1;
    relativeTurnaround = -1;
}

int Process::process(int timeProcessed)
{
    if(timeProcessed > remainingTime) 
    {
        timeProcessed = remainingTime;
    }

    remainingTime -= timeProcessed;
    
    if(remainingTime == 0) 
    {
        finish();
    }
    
    return remainingTime;
}

int Process::finish() 
{
    finishTime = currentTime;
    turnaround = finishTime - arrivalTime;

    // make this a floating point?
    relativeTurnaround = turnaround / serviceTime;
}