import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Random;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public class Mutex {
    private static Mutex _instance = null;

    private Node self = null;
    private Client client = null;
    private Server server = null;
    private Queue<String> requests = null;

    private Mutex(Node self) {
        this.self = self;
        this.requests = new LinkedList<>();
        this.client = new Client(self);
        this.server = new Server(self);
        
        // Start Server-side thread, which then spawns client handler threads
        this.server.start();

        // Start Client-side thread, which spawns client threads to neighbors
        this.client.start();

        // Wait block to allow all nodes to establish connections
        while(true) {
            if(client.isDone() && server.isDone()) break;
        }
    }

    public static void init(Node self) {
        _instance = new Mutex(self);
    }

    public static Mutex getInstance() {
        return _instance;
    }

    public void enter() {
        // Insert the request into priority queue
        requests.add("Request from Node " + self.id);

        // Broadcast request to all processes
        client.broadcast("Request from Node " + self.id);
    }

    public void exit() {
        System.out.println("* Exiting critical section.");
    }

    private void idle(long millis) {
		try {
			Thread.sleep(millis);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}