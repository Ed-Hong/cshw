/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 1
 */

public class FinishMessage extends Message {
    public static int NUM_CHANNEL_MSGS_INDEX = 3;
    public static int VECTOR_CLOCK_INDEX = 4;

    public int numChannelMsgs;
    public int[] clock;

    public FinishMessage(int sourceId, int destinationId, int channelMsgs, int[] clk) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.message = "FIN";
        this.numChannelMsgs = channelMsgs;
        this.clock = clk;

        this.addParam(sourceId);
        this.addParam(destinationId);
        this.addParam(numChannelMsgs);
        this.addParam(clock);
    }

    public FinishMessage(String sourceId, String destinationId, String channelMsgs, String clk) {
        this.sourceId = Integer.parseInt(sourceId);
        this.destinationId = Integer.parseInt(destinationId);
        this.message = "FIN";
        this.numChannelMsgs = Integer.parseInt(channelMsgs);

        String[] vals = clk.split(",");
        this.clock = new int[vals.length];
        for(int i = 0; i < vals.length; i++) {
            this.clock[i] = Integer.parseInt(vals[i]);
        }

        this.addParam(sourceId);
        this.addParam(destinationId);
        this.addParam(numChannelMsgs);
        this.addParam(clock);
    }
}