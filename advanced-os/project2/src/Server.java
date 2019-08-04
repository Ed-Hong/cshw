import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public class Server extends Thread {
	final Node self;
	int threadCount;

	public Server(Node n) {
		this.self = n;
		this.threadCount = 0;
	}

    @Override
    public void run() { 
		System.out.println("* Listening on port " + self.listenPort + "...");
		try {
			// Open a ServerSocket on self node's port number
			ServerSocket serverSock = new ServerSocket(self.listenPort);
			while(true) {
				if(threadCount == Node.NUM_NODES - 1) break;

				// Wait for a connection
				Socket sock = serverSock.accept();

				System.out.println("> Client connected.");
				DataInputStream in = new DataInputStream(sock.getInputStream());	
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());	
				
				new ServerThread(sock, in, out, self).start();

				threadCount++;
			}
			// Cleanup
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}