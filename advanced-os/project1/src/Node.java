import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 1
 */

public class Node {

	// Global Parameters
	public static int NUM_NODES;
	public static int MIN_PER_ACTIVE;
	public static int MAX_PER_ACTIVE;
	public static int MIN_SEND_DELAY;
	public static int SNAPSHOT_DELAY;
	public static int MAX_NUMBER;

	// Root Node Id
	public static int startingNodeId = Integer.MAX_VALUE;

	// Index of All Nodes
	public static HashMap<Integer, Node> nodes = new HashMap<>();

	// Adjacency List of All Nodes
	public static HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();

	// The Node on the current host
	static Node self;

	// Node properties
	public int id;
	public String hostName;
	public int listenPort;
	public ArrayList<Node> neighbors;
	public final boolean isRoot;
	public int[] clock;	//todo update the clock
	public int parentId;

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
	private Queue<FinishMessage> _finMessages = new LinkedList<>();
	private HashSet<Integer> _passiveNodesSet = new HashSet<>();
	private HashSet<Integer> _nodesWithEmptyChannelsSet = new HashSet<>();

	private boolean _isTerminated;
	private Queue<TerminateMessage> _terminateMessages = new LinkedList<>();
	private Queue<TermAckMessage> _termAckMessages = new LinkedList<>();
	private HashSet<Integer> _termAckSet = new HashSet<>();
	private boolean _hasReceivedParentTerm = false;

	public Node(int id, String hostName, int listenPort) {
		this.id = id;
		this.hostName = hostName;
		this.listenPort = listenPort;
		this.neighbors = new ArrayList<>();
		this.isRoot = id == startingNodeId;
		this.clock = new int[NUM_NODES];

		this._isActive = isRoot;
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

	public int countMessagesInChannels() {
		int numChannelMsgs = 0;
		for (int key : self.channels.keySet()) {
			numChannelMsgs += self.channels.get(key).size();
		}
		return numChannelMsgs;
	}

	public synchronized void setFinishedLocal(boolean fin) {
        _isFinishedLocal = fin;
    }

    public synchronized boolean isFinishedLocal() {
        return _isFinishedLocal;
	}

	public synchronized void addFinMessage(FinishMessage msg) {
		_finMessages.add(msg);
	}
	
	public synchronized boolean hasFinMessage() {
		return !_finMessages.isEmpty();
	}

	public synchronized FinishMessage removeFinMessage() {
		return _finMessages.poll();
	}

	public synchronized void receiveFinMessage(FinishMessage msg) {
		int nodeId = msg.sourceId;

		if(!msg.isActive) {
			_passiveNodesSet.add(nodeId);
		} else if(_passiveNodesSet.contains(nodeId)) {
			_passiveNodesSet.remove(nodeId);
		}

		if(msg.numChannelMsgs == 0) {
			_nodesWithEmptyChannelsSet.add(nodeId);
		} else if(_nodesWithEmptyChannelsSet.contains(nodeId)) {
			_nodesWithEmptyChannelsSet.remove(nodeId);
		}
	}

	public void checkMAPTermination() {
		System.out.println("Checking MAP Termination...");

		if(self.isRoot && !_globalFinishDebounce) {
			if(_passiveNodesSet.size() == NUM_NODES - 1 && _nodesWithEmptyChannelsSet.size() == NUM_NODES - 1) {
				System.out.println("MAP PROTOCOL HAS FINISHED - DECLARE TERMINATION!");
				_globalFinishDebounce = true;

				// Root node begins termination protocol
				self.addTerminateMessage(new TerminateMessage(self.id, self.id));
				self.hasReceivedParentTerm();	
			}
		}
	}

	public synchronized boolean hasTerminateMessage() {
		return !_terminateMessages.isEmpty();
	}

	public synchronized void addTerminateMessage(TerminateMessage termMsg) {
		_terminateMessages.add(termMsg);
	}

	public synchronized TerminateMessage removeTerminateMessage() {
		return _terminateMessages.poll();
	}

	public synchronized void addTermAckMessage(TermAckMessage tackMsg) {
		_termAckMessages.add(tackMsg);
	}

	public synchronized boolean hasTermAckMessage() {
		return !_termAckMessages.isEmpty();
	}

	public synchronized TermAckMessage removeTermAckMessage() {
		return _termAckMessages.poll();
	}

	public synchronized void addTermAckToSet(int sourceId) {
		_termAckSet.add(sourceId);
	}

	public synchronized boolean receivedAllTermAcks() {
		// Subtract 1 if not root since one of the neighbors is parent
		return self.isRoot ? _termAckSet.size() == self.neighbors.size() : 
							 _termAckSet.size() == self.neighbors.size() - 1;
	}

	public synchronized void receivedParentTerm() {
        if(!_hasReceivedParentTerm) {
			_hasReceivedParentTerm = true;
		}
    }

    public synchronized boolean hasReceivedParentTerm() {
        return _hasReceivedParentTerm;
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
			System.out.println("> Pass name of config file as an argument, and ensure that it is correct.\n");
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
			System.out.println("DEBUG: Please input a mock hostname for development purposes.\n");
			return;
		}

		init(hostname);

		System.out.println("> Configuration Finished.\n");

		// Spawn a Server-side thread, which then spawns client handler threads
		new Server(self).start();

		// Spawn a Client-side thread, which spawns client threads to neighbors
		new Client(self).start();

		//naive solution: start task to start snapshot every 5 seconds
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
			//todo make this repeat
			// Delay then attempt a snapshot
			Thread.sleep(delay);	

			// If I am the root, send self MARK to begin snapshot
			if(self.isRoot) {
				Socket sock = new Socket("localhost", self.listenPort);
				DataOutputStream out = new DataOutputStream(sock.getOutputStream());
				out.writeUTF(new MarkerMessage(self.id, self.id).message);

				//debug - test single snapshot
				//break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
