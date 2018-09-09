class Process
{
    private:
    int pid;
    int arrivalTime;
    int serviceTime;
    int finishTime;
    int turnaround;
    int relativeTurnaround;
    int finish();

    public:
    static int currentTime;

    Process(int pid, int serviceTime);
    
    int getPid();
    int getArrivalTime();
    int getServiceTime();
    int getFinishTime();
    int getTurnaround();
    int getRelativeTurnaround();
    int process(int timeProcessed);
    
}; 