import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.PriorityQueue;

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
    private PriorityQueue<Request> requests = null;
    private int replyCount = 0;

    private Mutex(Node self) {
        this.self = self;
        this.requests = new PriorityQueue<>(new RequestComparator()); 
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

    public static synchronized void init(Node self) {
        if (_instance == null) _instance = new Mutex(self);
    }

    public static synchronized Mutex getInstance() {
        return _instance;
    }

    public synchronized void addRequest(Request request) {
        requests.add(request);
        //System.out.println(self.id + ": added request from " + request.sourceId);
    }

    public synchronized void removeRequest(Request request) {
        requests.remove(request);
    }

    public synchronized Request getNextRequest() {
        return requests.poll();
    }

    public synchronized Request peekNextRequest() {
        return requests.peek();
    }

    public synchronized void addReply() {
        replyCount++;
    }

    public synchronized int getReplies() {
        return replyCount;
    }

    public synchronized void resetReplies() {
        replyCount = 0;
    }

    public synchronized void onReceiveRequest(Request req) {
        // Insert request into queue
        addRequest(req);

        // Send a reply
        client.send(new Message(Type.REPLY, self.id, req.sourceId, self.incrementClock()));
    }

    public synchronized void onReceiveReply(int sourceId) {
        //System.out.println(self.id + "got REPLY from " + sourceId);
        addReply();
    }

    public synchronized void onReceiveRelease(int sourceId) {
        removeRequest(new Request(sourceId));
        //System.out.println(self.id + ": Released - queue size = " + requests.size());
    }

    public void enter() {
        Request req = new Request(self.id, self.incrementClock());

        // Insert the request into priority queue
        addRequest(req);

        // Broadcast request message to all processes
        client.broadcast(req);

        // Block until request is granted
        while(true) {
            if(peekNextRequest().sourceId == self.id && getReplies() == Node.NUM_NODES - 1) {
                break;
            }
        }
    }

    public void exit() {        
        // Remove request from the queue
        getNextRequest();
        resetReplies();
        
        // Broadcast release
        client.broadcast(new Message(Type.RELEASE, self.id, self.incrementClock()));
    }

    private void idle(long millis) {
		try {
			Thread.sleep(millis);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}