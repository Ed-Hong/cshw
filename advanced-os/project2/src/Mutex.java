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
    private Queue<Integer> requests = null;

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

        int messageCount = 0;

        // Begin
        while(true) {
            if (messageCount >= 10) break;

            //client.send((self.id + 1) % 3, "Hello from Node " + self.id);
            client.broadcast("Hello from Node " + self.id);
            messageCount++;

            Random rand = new Random();

            // Wait between 1 to 5 seconds
            idle(1000 + rand.nextInt(4000));
        }
    }

    public static void init(Node self) {
        _instance = new Mutex(self);
    }

    public static Mutex getInstance() {
        return _instance;
    }

    public static void enter() {
        System.out.println("* Entering critical section...");
    }
    public static void exit() {
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