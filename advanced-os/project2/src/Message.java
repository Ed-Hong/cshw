/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public class Message { 
    public static int MESSAGE_TYPE_INDEX = 0;
    public static int SOURCE_INDEX = 1;
    public static int DESTINATION_INDEX = 2;
    public static int TIMESTAMP_INDEX = 3;

    public Type type;
    public int sourceId;
    public int destinationId;
    public int timestamp;

    public Message(Type type, int sourceId, int destId, int timestamp) {
        this.type = type;
        this.sourceId = sourceId;
        this.destinationId = destId;
        this.timestamp = timestamp;
    }

    public Message(String encoded) {
        String[] params = encoded.split(" ");

        switch (params[0]) {
            case "REQUEST":
                this.type = Type.REQUEST;
                break;

            case "REPLY":
                this.type = Type.REPLY;
                break;
            
            case "RELEASE":
                this.type = Type.RELEASE;
                break;
        }

        this.sourceId       = Integer.parseInt(params[2]);
        this.destinationId  = Integer.parseInt(params[4]);
        this.timestamp      = Integer.parseInt(params[6]);
    }

    // 0            2    4    6
    // REQUEST from 0 to 1 at ts
    public String encode() {
        String encoding = "";
        
        // Encode message type
        switch (type) {
            case REQUEST:
                encoding += "REQUEST ";
                break;

            case REPLY:
                encoding += "REPLY ";
                break;
            
            case RELEASE:
                encoding += "RELEASE ";
                break;
        }

        // Encode Source and Destination IDs
        encoding += "from " + sourceId + " to " + destinationId;

        // Encode timestamp
        encoding += " at " + timestamp;

        return encoding;
    }
}

enum Type {
    REQUEST, REPLY, RELEASE
}