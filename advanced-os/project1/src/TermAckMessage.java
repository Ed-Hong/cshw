/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 1
 */

public class TermAckMessage extends Message {

    public TermAckMessage(int sourceId, int destinationId) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.message = "TACK";

        this.addParam(sourceId);
        this.addParam(destinationId);
    }

    public TermAckMessage(String sourceId, String destinationId) {
        this.sourceId = Integer.parseInt(sourceId);
        this.destinationId = Integer.parseInt(destinationId);
        this.message = "TACK";

        this.addParam(sourceId);
        this.addParam(destinationId);
    }
}