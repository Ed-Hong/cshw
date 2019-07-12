import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 1
 */

public class ClientThread extends Thread { 
	final Node self;
	final Socket sock; 
	final DataInputStream in; 
	final DataOutputStream out; 
	private final Queue<String> _messages = new LinkedList<>();

	public ClientThread (Socket s, DataInputStream is, DataOutputStream os, Node n) { 
        this.sock = s; 
        this.in = is;
		this.out = os;
		this.self = n;
	}
	
	public synchronized boolean hasMessage() {
		return !_messages.isEmpty();
	}

	public synchronized void addMessage(String msg) {
		_messages.add(msg);
	}

	public synchronized String getNextMessage() {
		return _messages.poll();
	}

    @Override
    public void run() {
		while(true) {
			if (hasMessage()) {
                String msg = getNextMessage();
                String[] params = msg.split("_");
                String msgType = params[Message.MESSAGE_TYPE_INDEX];
                int destinationId = Integer.parseInt(params[Message.DESTINATION_INDEX]);

                // If sending TERM-ACK to parent, then terminate after sending all remaining ACKs
                boolean shouldTerminate = false;
                if(msgType.equals("TACK") && destinationId == self.parentId && !hasMessage()) {
                    shouldTerminate = true;
                }

				try {
                    //debug
                    System.out.println("  Sending: " + msg);
                    out.writeUTF(msg);
                    
                    if(shouldTerminate) {
                        // TERMINATE NODE
                        System.out.println("YOU HAVE BEEN TERMINATED.");
                        self.setTerminated(true);
                    }
				} catch (IOException i) {
					i.printStackTrace();
				}
				self.sentMessageCount++;
			} else {
				try {
					// Wait 5ms and check again
					Thread.sleep(5);	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (self.isTerminated()) {
				break;
			}
		}
	}

	// protected void finalize() throws Throwable {
	// 	//cleanup
	// 	sock.close();
	// 	in.close();
	// 	out.close();
	// }
}