#include "Process.h"

Process::Process(int pid, int serviceTime) : pid(pid), serviceTime(serviceTime) 
{ 
    arrivalTime = 2 * pid;
    finishTime = -1;
    turnaround = -1;
    relativeTurnaround = -1;
}

int Process::process(int timeProcessed)
{
    if(timeProcessed > serviceTime) 
    {
        timeProcessed = serviceTime;
    }

    serviceTime -= timeProcessed;
    
    if(serviceTime == 0) 
    {
        finish();
    }
    
    return serviceTime;
}

int Process::finish() 
{
    finishTime = Process::currentTime;
    turnaround = finishTime - arrivalTime;

    // make this a floating point?
    relativeTurnaround = turnaround / serviceTime;
}

int Process::getPid()
{
    return pid;
}

int Process::getArrivalTime()
{
    return arrivalTime;
}

int Process::getServiceTime()
{
    return serviceTime;
}

int Process::getFinishTime()
{
    return finishTime;
}

int Process::getTurnaround()
{
    return turnaround;
}

int Process::getRelativeTurnaround()
{
    return relativeTurnaround;
}