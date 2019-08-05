import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public class Server extends Thread {
	private ServerSocket serverSock;
	final Node self;
	final ArrayList<ServerThread> threads = new ArrayList<>();
	
	int threadCount;
	private boolean _done = false;
	private volatile boolean alive;

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

	public synchronized boolean alive() {
		return alive;
	}

	public synchronized void kill() {
		alive = false;
	}

    @Override
    public void run() { 
		setup();

		alive = true;
		while(alive()) { }

		cleanup();
	}
	
	private void setup() {
		try {
			// Open a ServerSocket on self node's port number
			serverSock = new ServerSocket(self.listenPort);
			while(true) {
				if(threadCount == Node.NUM_NODES - 1) break;

				// Wait for a connection
				Socket sock = serverSock.accept();

				DataInputStream in = new DataInputStream(sock.getInputStream());	
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());	
				
				threads.add(new ServerThread(sock, in, out, self));

				threadCount++;
			}

			// Start all threads
			for(ServerThread thread : threads) {
				thread.start();
			}

			// Set Done flag
			Done();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void cleanup() {
		for(ServerThread thread : threads) {
			thread.kill();
		}
		try {
			serverSock.close();
		} catch (Exception e) {
			e.printStackTrace();
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