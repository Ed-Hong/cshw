/***************************************************************************
* File: Process.h
* Author: Edward Hong
* Procedures:
* Process - constructor for a Process
* process - executes a process for a certain amount of time
* finish -  called when a Process is finished executing, and calculates
            Finish Time, Turnaround, and Relative Turnaround
* Public Members:
* currentTime static int The current time for all processes in CPU cycles
* pid int The Process ID
* arrivalTime int The arrival time of the process (equal to twice the Process ID)
* serviceTime int The service time of the process (randomly assigned between [1-10])
* finishTime int The time the proess finishes executing
* turnaround int The Turnaround Time for the process (equal to Finish Time - Arrival Time)
* relativeTurnaround float The Relative Turnaround Time for the process (equal to Turnaround Time / Service Time)
***************************************************************************/

class Process
{
    private:
    void finish();

    public:
    static int currentTime;
    int pid;
    int arrivalTime;
    int serviceTime;
    int remainingTime;
    int finishTime;
    int turnaround;
    float relativeTurnaround;

    Process(int pid, int serviceTime);
    int process(int timeProcessed);
};