/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public class Request extends Message {

    public Request(int sourceId, int destinationId, int timestamp) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.message = "REQUEST";
        this.timestamp = timestamp;

        this.addParam(sourceId);
        this.addParam(destinationId);
        this.addParam(timestamp);
    }
}