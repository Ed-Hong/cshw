class Process
{
    private:
    int finish();

    public:
    static int currentTime;
    int pid;
    int arrivalTime;
    int serviceTime;
    int remainingTime;
    int finishTime;
    int turnaround;
    int relativeTurnaround;

    Process(int pid, int serviceTime);
    int process(int timeProcessed);
};