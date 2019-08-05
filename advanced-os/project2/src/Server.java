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
	private boolean _done = false;

	public Server(Node n) {
		this.self = n;
		this.threadCount = 0;
	}

	public synchronized boolean isDone() {
		return _done;
	}

	private synchronized void Done() {
		_done = true;
	}

    @Override
    public void run() { 
		try {
			// Open a ServerSocket on self node's port number
			ServerSocket serverSock = new ServerSocket(self.listenPort);
			while(true) {
				if(threadCount == Node.NUM_NODES - 1) break;

				// Wait for a connection
				Socket sock = serverSock.accept();

				DataInputStream in = new DataInputStream(sock.getInputStream());	
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());	
				
				new ServerThread(sock, in, out, self).start();

				threadCount++;
			}
			// Cleanup

			// Set Done flag
			Done();

		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}