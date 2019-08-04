import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.io.EOFException;
import java.io.IOException;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public class ServerThread extends Thread { 
	final Node self;
	final Socket sock; 
	final DataInputStream in; 
    final DataOutputStream out; 
        
    public ServerThread (Socket s, DataInputStream is, DataOutputStream os, Node n) { 
        this.sock = s; 
        this.in = is; 
		this.out = os; 
		this.self = n;
    } 
  
    @Override
    public void run() { 
		while(true) {
			try {
				if (in.available() > 0) {
					String message = in.readUTF();
					System.out.println("  Received: " + message);
				} else {
					idle(10);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void idle(long millis) {
		try {
			Thread.sleep(millis);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}