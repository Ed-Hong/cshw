/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 1
 */

public class FinishMessage extends Message {
    public static int NUM_CHANNEL_MSGS_INDEX = 3;
    public static int VECTOR_CLOCK_INDEX = 4;

    public FinishMessage(int sourceId, int destinationId, int numChannelMsgs, int[] clock) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.message = "FIN";

        this.addParam(sourceId);
        this.addParam(destinationId);
        this.addParam(numChannelMsgs);
        this.addParam(clock);
    }
}