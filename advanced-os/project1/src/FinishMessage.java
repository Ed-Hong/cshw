/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 1
 */

public class FinishMessage extends Message {
    public static int IS_ACTIVE_INDEX = 3;
    public static int NUM_CHANNEL_MSGS_INDEX = 4;
    public static int VECTOR_CLOCK_INDEX = 5;

    public boolean isActive;
    public int numChannelMsgs;
    public int[] clock;

    public FinishMessage(int sourceId, int destinationId, boolean isActive, int channelMsgs, int[] clk) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.message = "FIN";
        this.isActive = isActive;
        this.numChannelMsgs = channelMsgs;
        this.clock = clk;

        this.addParam(sourceId);
        this.addParam(destinationId);
        this.addParam(isActive);
        this.addParam(numChannelMsgs);
        this.addParam(clock);
    }

    public FinishMessage(String sourceId, String destinationId, String isActive, String channelMsgs, String clk) {
        this.sourceId = Integer.parseInt(sourceId);
        this.destinationId = Integer.parseInt(destinationId);
        this.message = "FIN";
        this.isActive = Boolean.parseBoolean(isActive);
        this.numChannelMsgs = Integer.parseInt(channelMsgs);

        String[] vals = clk.split(",");
        this.clock = new int[vals.length];
        for(int i = 0; i < vals.length; i++) {
            this.clock[i] = Integer.parseInt(vals[i]);
        }

        this.addParam(sourceId);
        this.addParam(destinationId);
        this.addParam(isActive);
        this.addParam(numChannelMsgs);
        this.addParam(clock);
    }
}