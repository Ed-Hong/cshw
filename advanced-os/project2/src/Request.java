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