import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.util.Random;
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

	public Client(Node n) {
		this.self = n;
	}

	public synchronized boolean isDone() {
		return _done;
	}

	private synchronized void Done() {
		_done = true;
	}

    @Override
    public void run() {

        setupChannels();

		// Once all channels established, begin MAP 
		// while(true) {
		// 		
		// 	break;
		// }

		// Cleanup
		//threads.clear();
    }
	
	public void send(int destId, String message) {
		threads.get(destId).addMessage(message + " to Node " + destId);
	}

	public void broadcast(String message) {
		for(Integer id : threads.keySet()) {
			send(id, message);
		}
	}

    private void setupChannels() {
        while(true) {
			// Stop once all channels have been established to all neighbors
			if (threads.keySet().size() == Node.NUM_NODES - 1) break;

			// Spawn a new thread for each channel to each neighbor
			for (Integer nodeId : Node.nodes.keySet()) {
				if (nodeId == self.id) continue;
				Node neighbor = Node.nodes.get(nodeId);
				try {
					// Connect to neighbors
					Socket sock = new Socket("localhost", neighbor.listenPort);		//todo set hostname

					DataInputStream in = new DataInputStream(sock.getInputStream());	
					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
					
					// Build index of channels to neighbors
					threads.put(nodeId, new ClientThread(sock, in, out, self));

				} catch (ConnectException c) {
					idle(500);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// Start all threads
		for(Integer id : threads.keySet()) {
			threads.get(id).start();
		}

		// Set Done flag
		Done();
	}
	
	private void idle(long millis) {
		try {
			Thread.sleep(millis);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}