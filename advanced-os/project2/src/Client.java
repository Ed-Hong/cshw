import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.util.HashMap;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public class Client extends Thread {
	final Node self;
	final HashMap<Integer, ClientThread> threads = new HashMap<>();
	private boolean _done = false;
	private volatile boolean alive;

	public Client(Node n) {
		this.self = n;
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

        setupChannels();

		alive = true;
		while(alive()) { }

		cleanup();
    }
	
	public void send(Message msg) {
		threads.get(msg.destinationId).addMessage(msg.encode());
	}

	public void broadcast(Message msg) {
		for(Integer destId : threads.keySet()) {
			msg.destinationId = destId;
			send(msg);
		}
	}

	public void broadcast(Request req) {
		for(Integer destId : threads.keySet()) {
			send(new Message(Type.REQUEST, req.sourceId, destId, req.timestamp));
		}
	}

    private void setupChannels() {
		// Spawn a new thread for each channel to each neighbor
		for (Integer nodeId : Node.nodes.keySet()) {
			if (nodeId == self.id) continue;
			Node neighbor = Node.nodes.get(nodeId);

			// Blocking call, retries until connection successfull
			connectTo(neighbor);
		}

		// Start all threads
		for(Integer id : threads.keySet()) {
			threads.get(id).start();
		}

		// Set Done flag
		Done();
	}

	private void connectTo(Node neighbor) {
		while(true) {
			try {
				// Connect to neighbors
				Socket sock = new Socket(neighbor.hostName, neighbor.listenPort);
				DataInputStream in = new DataInputStream(sock.getInputStream());	
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());
				
				// Build index of channels to neighbors
				threads.put(neighbor.id, new ClientThread(sock, in, out, self));
				break;
			} catch (ConnectException c) {
				idle(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void cleanup() {
		for(Integer destId : threads.keySet()) {
			threads.get(destId).kill();
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
