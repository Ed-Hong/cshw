import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
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
				try {
                    //debug
                    System.out.println("  Sending: " + msg);
                    out.writeUTF(msg);
				} catch (IOException i) {
					i.printStackTrace();
				}
			} else {
				try {
					// Wait 5ms and check again
					Thread.sleep(5);	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// Cleanup
		// sock.close();
		// in.close();
		// out.close();
	}

	// protected void finalize() throws Throwable {
	// 	//cleanup
	// 	sock.close();
	// 	in.close();
	// 	out.close();
	// }
}