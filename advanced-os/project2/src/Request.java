import java.util.Comparator; 

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public class Request {
    public int sourceId;
    public int timestamp;

    public Request(int sourceId, int timestamp) {
        this.sourceId = sourceId;
        this.timestamp = timestamp;
    }
}

class RequestComparator implements Comparator<Request>{ 
    public int compare(Request r1, Request r2) { 
        if (r1.timestamp < r2.timestamp)
            return -1;
        else if (r1.timestamp > r2.timestamp) 
            return 1;
        return 0; 
    } 
} 