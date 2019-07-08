import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.stream.IntStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * 
 */

public class Node {
	// Constants
	public static final String APP_MESSAGE = "APP from Node ";
	public static final String MARK_MESSAGE = "MARK from Node ";
	public static final String FIN_MESSAGE = "FIN from Node ";
	public static final String DONE_MESSAGE = "DONE from Node ";

	// Global Parameters
	public static int NUM_NODES;
	public static int MIN_PER_ACTIVE;
	public static int MAX_PER_ACTIVE;
	public static int MIN_SEND_DELAY;
	public static int SNAPSHOT_DELAY;
	public static int MAX_NUMBER;

	public static int startingNodeId = Integer.MAX_VALUE;

	// Index of All Nodes
	public static HashMap<Integer, Node> nodes = new HashMap<>();

	// Adjacency List of All Nodes
	public static HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();

	// The Node on the current host
	static Node self;

	public int id;
	public String hostName;
	public int listenPort;
	public ArrayList<Node> neighbors;

	// MAP Protocol Variables
	private boolean _isActive;
	public int sentMessageCount;

	// CL Protocol Variables
	private boolean _isRed;
	private Queue<Integer> _markerMessages = new LinkedList<>();
	private HashSet<Integer> _markerMessagesReceived = new HashSet<>();
	public HashMap<Integer, ArrayList<String>> channels = new HashMap<>();

	private boolean _isFinishedLocal;
	private boolean _globalFinishDebounce = false;
	private Queue<Integer> _finMessages = new LinkedList<>();
	private HashSet<Integer> _finMessagesSet = new HashSet<>();

	private boolean _isTerminated;
	private Queue<Integer> _doneMessages = new LinkedList<>();

	public Node(int id, String hostName, int listenPort) {
		this.id = id;
		this.hostName = hostName;
		this.listenPort = listenPort;
		this.neighbors = new ArrayList<>();

		this._isActive = id == startingNodeId ? true : false;
		this.sentMessageCount = 0;

		this._isRed = false;
		this._isFinishedLocal = false;
		this._isTerminated = false;
	}

	public synchronized void setActive(boolean active) {
        _isActive = active;
    }

    public synchronized boolean isActive() {
        return _isActive;
	}

	public synchronized void setRed(boolean red) {
        _isRed = red;
    }

    public synchronized boolean isRed() {
        return _isRed;
	}

	public synchronized void addMarker(int id) {
		_markerMessages.add(id);
    }

	public synchronized boolean hasMarker() {
		return !_markerMessages.isEmpty();
	}

    public synchronized int removeMarker() {
        return _markerMessages.poll();
	}

	public synchronized void receiveMarkerMessage(int id) {
        _markerMessagesReceived.add(id);
    }

    public synchronized int getReceivedMarkerCount() {
        return _markerMessagesReceived.size();
	}

	public synchronized void setFinishedLocal(boolean fin) {
        _isFinishedLocal = fin;
    }

    public synchronized boolean isFinishedLocal() {
        return _isFinishedLocal;
	}

	public synchronized void addFinMessage(int id) {
		_finMessages.add(id);
	}
	
	public synchronized boolean hasFinMessage() {
		return !_finMessages.isEmpty();
	}

	public synchronized int removeFinMessage() {
		return _finMessages.poll();
	}

	public synchronized void addFinMessageToSet(int id) {
		if(self.id == Node.startingNodeId) {
			_finMessagesSet.add(id);

			if(!_globalFinishDebounce && _finMessagesSet.size() == NUM_NODES - 1) {
				System.out.println("--- GLOBAL FINISH ---");
				addDoneMessage(Node.startingNodeId);
				_globalFinishDebounce = true;
			}
		}
	}

	public synchronized boolean hasDoneMessage() {
		return !_doneMessages.isEmpty();
	}

	public synchronized void addDoneMessage(int id) {
		_doneMessages.add(id);
	}

	public synchronized int removeDoneMessage() {
		return _doneMessages.poll();
	}

	public synchronized void setTerminated(boolean t) {
        _isTerminated = t;
    }

    public synchronized boolean isTerminated() {
        return _isTerminated;
	}
	
	//debug
	private boolean recorded = false;

	//todo elaborate this method?
	public void recordLocalState() {
		if (!recorded) {
			String state = "NodeId=" + self.id + " isActive=" + self.isActive() + " sentMessageCount=" + self.sentMessageCount;
			System.out.println(state);
			recorded = true;
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("\n* Configuring node...");

		// Create scanner for config file - filename of config is passed as args[0]
		Scanner cfgScanner = null;
		try {
			File configFile = new File(args[0]);
			cfgScanner = new Scanner(configFile);
		} catch (Exception e) {
			System.out.println("! Error reading config file.");
			System.out.println("> Pass name of config file as an argument, and ensure that it is correct.");
			return;
		}

		// Read config file
		config(cfgScanner);

		// Get the hostname
		String hostname = null;
		try {
			InetAddress ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();
			System.out.println("  Current Hostname: " + hostname);
		} catch (UnknownHostException e) {
			System.out.println("! Error attempting to get hostname.");
			e.printStackTrace();
			cfgScanner.close();
			return;
		}

		//DEBUG
		try {
			hostname = args[1];
		} catch (Exception e) {
			System.out.println("DEBUG: Please input a mock hostname for development purposes.");
			return;
		}

		init(hostname);

		System.out.println("> Configuration Finished.\n");

		// Spawn a Server-side thread, which then spawns client handler threads
		new Server(self).start();

		// Spawn a Client-side thread, which spawns client threads to neighbors
		new Client(self).start();

		//naive solution: start task to start snapshot (todo)
		new Snapshot(5000, self).start();
	}

	private static void init(String currentHostname) {
		// Init self node definition
		for (Integer id : nodes.keySet()) {
			Node n = nodes.get(id);
			if (n.hostName.equals(currentHostname)) {
				self = new Node(n.id, n.hostName, n.listenPort);
				break;
			}
		}

		// Populate list of neighbors for self
		for (Integer neighborId : map.get(self.id)) {
			self.neighbors.add(nodes.get(neighborId));
		}
	}

	private static void config(Scanner cfg) {
		int[] params = new int[6];
		Scanner line = null;
		int lineNum = 0;
		boolean isFirstPass = true;

		// Read each line
		while (cfg.hasNextLine()) {
			String lineStr = cfg.nextLine();
			line = new Scanner(lineStr);
			boolean isValidLine = true;
			int tokenNum = 0;

			// Temp variables for Node definitions
			Integer nodeId = null;
			String hostName = null;
			Integer listenPort = null;

			// Read each token within a line
			while(line.hasNext()) {
				String token = line.next();

				// Skip over non-valid lines. Valid lines begin with unsigned int
				if (tokenNum == 0 && !isUnsignedInt(token)) {
					isValidLine = false;
					break;
				}

				// Skip over comments
				if (token.equals("#")) break;

				// Assign Global Parameters
				if (lineNum == 0) {
					params[tokenNum] = Integer.parseInt(token);
				}

				// Definitions of N Nodes
				if (lineNum > 0 && lineNum <= NUM_NODES) {
					switch(tokenNum) {
						case 0:
							nodeId = new Integer(Integer.parseInt(token));
						break;
						case 1:
							hostName = token;
						break;
						case 2:
							listenPort = new Integer(Integer.parseInt(token));
						break;
					}

					if (nodeId != null && hostName != null && listenPort != null) {
						Node newNode = new Node(nodeId, hostName, listenPort);
						nodes.put(nodeId, newNode);

						// Starting node is the node with lowest id
						if (nodeId < startingNodeId) {
							startingNodeId = nodeId;
						}

						// Reset for next node definition
						nodeId = null;
						hostName = null;
						listenPort = null;
					}
				}

				// Neighbors of N Nodes
				if (lineNum > NUM_NODES && lineNum <= 2*NUM_NODES) {
					int id = lineNum - NUM_NODES - 1;
					int neighborId = Integer.parseInt(token);

					// Populating topology map
					if(map.get(id) == null) {
						map.put(id, new ArrayList<Integer>());
					}
					map.get(id).add(neighborId);
				}

				tokenNum++;
			}

			// Make sure the line is valid and has at least 1 token
			if (isValidLine && tokenNum > 0) {
				lineNum++;
			}

			if (isFirstPass && lineNum == 1) {
				isFirstPass = false;

				// Re-assigning Global Parameters to named constants
				aliasGlobalParams(params);
			}
		}

		if(cfg != null) cfg.close();
		if(line != null) line.close();
	}

	private static boolean isUnsignedInt(String str) {
		try {
			int i = Integer.parseInt(str);
			return i >= 0;
		} catch (NumberFormatException | NullPointerException e) {
			return false;
		}
	}

	private static void aliasGlobalParams(int[] params) {
		NUM_NODES 		= params[0];
		MIN_PER_ACTIVE 	= params[1];
		MAX_PER_ACTIVE 	= params[2];
		MIN_SEND_DELAY 	= params[3];
		SNAPSHOT_DELAY 	= params[4];
		MAX_NUMBER 		= params[5];
	}
}

class Server extends Thread {
	final Node self;

	public Server(Node n) {
		this.self = n;
	}

    @Override
    public void run() { 
		System.out.println("* Listening on port " + self.listenPort + "...");
		
		try {
			// Open a ServerSocket on self node's port number
			ServerSocket serverSock = new ServerSocket(self.listenPort);
			while(true) {
				// Wait for a connection
				Socket sock = serverSock.accept();

				System.out.println("> Client connected.");
				DataInputStream in = new DataInputStream(sock.getInputStream());	
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());	
				
				new ServerThread(sock, in, out, self).start();
				//todo the serverSocket can be cleaned up since we know the number of threads to be spawned
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}

class ServerThread extends Thread { 
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
				String request = in.readUTF();
				System.out.println("  Received: " + request);

				// MAP Protocol messages are application messages and begin with "APP"

				// Record app messages received while red as channel state
				//todo once i've recorded all my channel states then i'm done
				if (self.isRed() && request.startsWith("APP")) {

					// Application messages are stamped by sender at the end
					int channel = Integer.parseInt(request.substring(request.lastIndexOf(" ") + 1));
					self.channels.get(channel).add(request);
				}

				if(request.startsWith("APP") && !self.isActive() && self.sentMessageCount < Node.MAX_NUMBER) {
					self.setActive(true);
				}

				// Snapshot Protocol messages are system messages and begin with "MARK"
				if(request.startsWith("MARK")) {
					int nodeId = Integer.parseInt(request.substring(request.lastIndexOf(" ") + 1));
					self.receiveMarkerMessage(nodeId);
					
					// Change color from blue to red
					if(!self.isRed()) {
						System.out.println("  COLOR CHANGE TO RED");
						for (Node neighbor : self.neighbors) {
							self.addMarker(neighbor.id);	
						}
						self.setRed(true);

						// Init channel states
						for (Node neighbor : self.neighbors) {
							self.channels.put(neighbor.id, new ArrayList<String>());
						}
					} else if (!self.isFinishedLocal() && 
							  (self.id != Node.startingNodeId && self.getReceivedMarkerCount() >= self.channels.keySet().size() ||
							  (self.id == Node.startingNodeId && self.getReceivedMarkerCount() > self.channels.keySet().size()))) {
							// Once this node has received all marker messages from all neighbors (the root receiving one from itself)
							self.setFinishedLocal(true);
							System.out.println("  LOCALLY FINISHED");
							
							// Multicast FIN message to all neighbors (if not root node)
							if (self.id != Node.startingNodeId) {
								for(Node n : self.neighbors) {
									//todo attach extra data to fin message like clock, etc
									self.addFinMessage(n.id);
								}
						}
					}
				}

				// Non-root Nodes forward FIN messages to the root
				if (request.startsWith("FIN") && !self.hasDoneMessage()) {
					int nodeId = Integer.parseInt(request.substring(request.lastIndexOf(" ") + 1));

					// Root Node receives accounts for FIN messages
					if(self.id == Node.startingNodeId) {
						self.addFinMessageToSet(nodeId);
					} else {
						System.out.println("Received FIN from " + nodeId + ". Forwarding.");
						self.addFinMessage(nodeId);
					}
				}

				// Receiving DONE message begins termination
				if (request.startsWith("DONE")) {
					int nodeId = Integer.parseInt(request.substring(request.lastIndexOf(" ") + 1));

					self.addDoneMessage(nodeId);
					//break;
				}

				if (self.isTerminated()) {
					break;
				}

			} catch(EOFException eof) {
				try {
					// No bytes in the buffer; wait 5ms and check again
					Thread.sleep(5);	
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			//cleanup
			// try {
			// 	sock.close();
			// 	in.close();
			// 	out.close();
			// } catch (IOException e) {
			// 	e.printStackTrace();
			// }
		}
    }
}

class Client extends Thread {
	final Node self;
	final HashMap<Integer, ClientThread> threads = new HashMap<>();

	public Client(Node n) {
		this.self = n;
	}

    @Override
    public void run() {
		while(true) {
			if (threads.keySet().size() == self.neighbors.size()) break;	// Break once all channels established

			// Spawn a new thread for each channel to each neighbor
			for (Node neighbor : self.neighbors) {
				if (threads.keySet().contains(neighbor.id)) continue;
				try {
					//todo set hostname

					// Connect to neighbors
					Socket sock = new Socket("localhost", neighbor.listenPort);
					DataInputStream in = new DataInputStream(sock.getInputStream());	
					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
					
					// Build index of channels to neighbors
					threads.put(neighbor.id, new ClientThread(sock, in, out, self));
				} catch (ConnectException c) {
					try {
						//debug
						//System.out.print(".");
						// Wait 50ms and try again
						Thread.sleep(50);	
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// Start all threads
		for(Integer id : threads.keySet()) {
			threads.get(id).start();
		}

		// Once all channels established, begin MAP 
		while(true) {
			if(self.isActive()) {
				System.out.println("  ACTIVE");
				int randomMsgCount = new Random().ints(1, Node.MIN_PER_ACTIVE, Node.MAX_PER_ACTIVE + 1).findFirst().getAsInt();
				while(randomMsgCount > 0) {
					if(self.sentMessageCount >= Node.MAX_NUMBER) {
						self.setActive(false);
						break;
					}
					int randomIndex = new Random().nextInt(threads.keySet().size());
					Node destNode = self.neighbors.get(randomIndex);
					threads.get(destNode.id).addMessage(appMessage(self.id));
					randomMsgCount--;
	
					try {
						// Wait MIN_SEND_DELAY ms before sending again
						Thread.sleep(Node.MIN_SEND_DELAY);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				self.setActive(false);
			}
			
			// On Color Change from Blue to Red if we have marker messages to send, send them
			if(self.hasMarker()) {
				self.recordLocalState();

				// Multicast MARK message to all neighbors
				while(self.hasMarker()) {
					int id = self.removeMarker();
					threads.get(id).addMessage(markMessage(self.id));
				}
			}

			// Finished recording local state and channel state - sending FIN message to neighbors
			if(self.hasFinMessage()) {
				while(self.hasFinMessage()) {
					int id = self.removeFinMessage();
					threads.get(id).addMessage(finMessage(self.id));
				}
			}

			// Node termination
			if(self.hasDoneMessage()) { 
				// Root node initiates
				if(self.id == Node.startingNodeId) {
					while(self.hasDoneMessage()) {
						self.removeDoneMessage();
						for (Node neighbor : self.neighbors) {
							threads.get(neighbor.id).addMessage(doneMessage(self.id));							
						}
					}
				} else {
					// Multicast DONE message to all neighbors
					while(self.hasDoneMessage()) {
						int id = self.removeDoneMessage();
						threads.get(id).addMessage(doneMessage(self.id));
					}
				}
				System.out.println("YOU HAVE BEEN TERMINATED");
				self.setTerminated(true);
				break;
			}

			if (self.isTerminated()) {
				break;
			}
		}
		//cleanup
		//threads.clear();
	}
	
	private static String appMessage(int senderId) {
		return Node.APP_MESSAGE + senderId;
	}
	
	private static String markMessage(int senderId) {
		return Node.MARK_MESSAGE + senderId;
	}

	private static String finMessage(int senderId) {
		return Node.FIN_MESSAGE + senderId;
	}

	private static String doneMessage(int senderId) {
		return Node.DONE_MESSAGE + senderId;
	}
}

class ClientThread extends Thread { 
	final Node self;
	final Socket sock; 
	final DataInputStream in; 
	final DataOutputStream out; 
	private final Queue<String> _messages = new LinkedList<>();

	public ClientThread (Socket s, DataInputStream is, DataOutputStream os, Node n) { 
        this.sock = s; 
        this.in = is;
		this.out = os;
		this.self = n;
	}
	
	public synchronized boolean hasMessage() {
		return !_messages.isEmpty();
	}

	public synchronized void addMessage(String msg) {
		_messages.add(msg);
	}

	public synchronized String getNextMessage() {
		return _messages.poll();
	}

    @Override
    public void run() {
		while(true) {
			if (hasMessage()) {
				String msg = getNextMessage();
				try {
					out.writeUTF(msg);
				} catch (IOException i) {
					i.printStackTrace();
				}
				self.sentMessageCount++;
			} else {
				try {
					// Wait 5ms and check again
					Thread.sleep(5);	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// Begin termination with root node
			if(!self.isTerminated() && self.id == Node.startingNodeId && self.hasDoneMessage()) {
				while(self.hasDoneMessage()) {
					self.removeDoneMessage();
					try {
						Socket sock = new Socket("localhost", self.listenPort);
						DataOutputStream out = new DataOutputStream(sock.getOutputStream());
						out.writeUTF("DONE " + Node.startingNodeId);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (self.isTerminated()) {
				break;
			}
		}
	}

	// protected void finalize() throws Throwable {
	// 	//cleanup
	// 	sock.close();
	// 	in.close();
	// 	out.close();
	// }
}

class Snapshot extends Thread { 
	final long delay;
	final Node self;

	public Snapshot (long millis, Node n) { 
		this.delay = millis;
		this.self = n;
    }

    @Override
    public void run() {
		try {
			// Delay then attempt a snapshot
			Thread.sleep(delay);	

			// If I am the root, send self MARK to begin snapshot
			if(self.id == Node.startingNodeId) {
				Socket sock = new Socket("localhost", self.listenPort);
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());
				out.writeUTF("MARK " + Node.startingNodeId);

				//debug - test single snapshot
				//break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
