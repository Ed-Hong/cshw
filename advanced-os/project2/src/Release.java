/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public class Release extends Message {

    public Release(int sourceId, int destinationId, int timestamp) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.message = "RELEASE";
        this.timestamp = timestamp;

        this.addParam(sourceId);
        this.addParam(destinationId);
        this.addParam(timestamp);
    }
}