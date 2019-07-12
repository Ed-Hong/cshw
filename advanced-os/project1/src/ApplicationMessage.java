/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 1
 */

public class ApplicationMessage extends Message {
    public static int VECTOR_CLOCK_INDEX = 3;

    public int[] clock;

    public ApplicationMessage(int sourceId, int destinationId, int[] clk) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.message = "APP";
        this.clock = clk;

        this.addParam(sourceId);
        this.addParam(destinationId);
        this.addParam(clk);
    }

    public ApplicationMessage(String sourceId, String destinationId, String clk) {
        this.sourceId = Integer.parseInt(sourceId);
        this.destinationId = Integer.parseInt(destinationId);
        this.message = "APP";

        String[] vals = clk.split(",");
        this.clock = new int[vals.length];
        for(int i = 0; i < vals.length; i++) {
            this.clock[i] = Integer.parseInt(vals[i]);
        }

        this.addParam(sourceId);
        this.addParam(destinationId);
        this.addParam(clk);
    }
}