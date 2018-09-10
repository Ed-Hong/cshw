/***************************************************************************
* File: Process.cpp
* Author: Edward Hong
* Procedures:
* Process - constructor for a Process
* process - executes a process for a certain amount of time
* finish -  called when a Process is finished executing, and calculates
            Finish Time, Turnaround, and Relative Turnaround
***************************************************************************/
#include "Process.h"

// Initialize current time for processes
int Process::currentTime = 0;

/***************************************************************************
* Process Process(int pid, int serviceTime)
* Author: Edward Hong
* Date: 9 September 2018
* Description:  Creates a Process. Sets pid = pid and serviceTime = serviceTime,
                as well as initializing arrivalTime, remainingTime, finishTime,
                turnaround, and relativeTurnaround
* Parameters:
* pid I/P int The Process Id of the process to be created
* serviceTime I/P int The service time of the process to be created
**************************************************************************/
Process::Process(int pid, int serviceTime) : pid(pid), serviceTime(serviceTime) 
{
    arrivalTime = 2 * pid;
    remainingTime = serviceTime;
    
    // Initialize values that are initially unknown to -1
    finishTime = -1;
    turnaround = -1;
    relativeTurnaround = -1;
}

/***************************************************************************
* int process(int timeProcessed)
* Author: Edward Hong
* Date: 9 September 2018
* Description:  Executes a Process for the amount of time specified
* Parameters:
* timeProcessed I/P int The amount of time to execute the process
* remainingTime O/P int The amount of time remaining to finish the process
**************************************************************************/
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

/***************************************************************************
* void finish(int timeProcessed)
* Author: Edward Hong
* Date: 9 September 2018
* Description:  Calculates Finish Time, Turnaround Time, and Relative Turnaround
**************************************************************************/
void Process::finish() 
{
    finishTime = currentTime;
    turnaround = finishTime - arrivalTime;

    relativeTurnaround = (float)turnaround / (float)serviceTime;
}