/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public class Reply extends Message {

    public Reply(int sourceId, int destinationId, int timestamp) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.message = "REPLY";
        this.timestamp = timestamp;

        this.addParam(sourceId);
        this.addParam(destinationId);
        this.addParam(timestamp);
    }
}