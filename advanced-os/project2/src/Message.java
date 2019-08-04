/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public abstract class Message { 
    public static int MESSAGE_TYPE_INDEX = 0;
    public static int SOURCE_INDEX = 1;
    public static int DESTINATION_INDEX = 2;
    public static int TIMESTAMP_INDEX = 3;

    public int sourceId;
    public int destinationId;
    public String message;
    public int timestamp;

    public void addParam(int paramInt) {
        this.message += "_" + paramInt;
    }

    public void addParam(boolean paramBool) {
        this.message += "_" + paramBool;
    }

    public void addParam(String paramStr) {
        this.message += "_" + paramStr;
    }

    public void addParam(int[] intArr) {
        String arrStr = "";
        for(int i = 0; i < intArr.length; i++) {
            arrStr += intArr[i];
            if (i < intArr.length - 1) {
                arrStr += ",";
            }
        }
        this.message += "_" + arrStr;
    }
}