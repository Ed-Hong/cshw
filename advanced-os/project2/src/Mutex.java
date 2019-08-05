import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.PriorityQueue;
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
    private PriorityQueue<String> requests = null;

    private Mutex(Node self) {
        this.self = self;
        this.requests = new PriorityQueue<>(); 
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

    public synchronized void addRequest(String request) {
        requests.add(request);
        System.out.println(self.id + ": added " + request);
    }

    public synchronized void removeRequest(String request) {
        requests.remove(request);
    }

    public void enter() {
        // Insert the request into priority queue
        addRequest("REQUEST from Node " + self.id);

        // Broadcast request to all processes
        client.broadcast("REQUEST from Node " + self.id);
    }

    public void exit() {
        System.out.println("* Exiting critical section.");
    }

    public void onReceiveRequest(String message) {
        // Insert request into queue
        addRequest(message);

        // Send a reply
        client.send(0, "REPLY from Node " + self.id);
    }

    public void onReceiveReply(String message) {
        System.out.println(self.id + "got " + message);
    }

    private void idle(long millis) {
		try {
			Thread.sleep(millis);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}