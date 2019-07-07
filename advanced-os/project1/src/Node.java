import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * 
 */

public class Node {

	// Global Parameters
	public static int NUM_NODES;
	public static int MIN_PER_ACTIVE;
	public static int MAX_PER_ACTIVE;
	public static int MIN_SEND_DELAY;
	public static int SNAPSHOT_DELAY;
	public static int MAX_NUMBER;

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

	public Node(int id, String hostName, int listenPort) {
		this.id = id;
		this.hostName = hostName;
		this.listenPort = listenPort;
		this.neighbors = new ArrayList<>();
	}

	public static void main(String[] args) throws Exception {
		System.out.println();

		// Create scanner for config file - filename of config is passed as args[0]
		Scanner cfgScanner = null;
		try {
			File configFile = new File(args[0]);
			cfgScanner = new Scanner(configFile);
		} catch (Exception e) {
			System.out.println("Error reading config file.");
			System.out.println("Pass name of config file as an argument, and ensure that it is correct.");
			return;
		}

		// Read config file
		config(cfgScanner);

		//debugging nodes index
		// for (Integer id : nodes.keySet()) {
		// 	Node n = nodes.get(id);
		// 	System.out.println("NodeId: " + n.id + " HostName: " + n.hostName + " listenPort: " + n.listenPort);
		// }

		//debugging network topology
		// for (Integer key : map.keySet()) {
		// 	ArrayList<Integer> neighbors = map.get(key);
		// 	System.out.println("Node " + key + " neighbors: ");
		// 	for (Integer neighbor : neighbors) {
		// 		System.out.print(neighbor + " ");
		// 	}
		// 	System.out.println();
		// }

		// Get the hostname
		String hostname = null;
		try {
			InetAddress ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();
			System.out.println("Current IP address: " + ip);
			System.out.println("Current Hostname: " + hostname);
			System.out.println();
		} catch (UnknownHostException e) {
			System.out.println("Error attempting to get hostname.");
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

		//debug self and self's neighbors
		// System.out.println(" Self-nodeId: " + self.id + " Self-hostName: " + self.hostName + " Self-NodeId: " + self.listenPort);
		// System.out.println("Self-neighbors: ");
		// for(Node n : self.neighbors) {
		// 	System.out.println(n.id + " ");
		// }

		self.listen();
	}

	public void listen() throws Exception {
		System.out.println("Listening on port " + self.listenPort);
				
		// Open a ServerSocket (TCP) using the specified port number.
		ServerSocket serverSock = new ServerSocket(self.listenPort);
		
		// Wait and listen for a connection.
		// Note that the below line is a blocking call until a client has connected.
		Socket sock = serverSock.accept();

		System.out.println("Client connected!");
		DataInputStream in = new DataInputStream(sock.getInputStream());		
		while(true) {
			try {
				String request = in.readUTF();
				System.out.println("Echo: " + request);
				if (request.equals("END")) {
					break;
				}
			} catch(EOFException e) {
				// End of File Exception means that there are no bytes in the buffer.
				// Therefore, if we check the buffer and nothing is there, wait 5ms and check again.
				Thread.sleep(5);
			}			
		}

		System.out.println("Closing connection.");
		serverSock.close();
	}

	private static void init(String currentHostname) {
		System.out.println("Initializing this node...");

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
		System.out.println("Reading config...");

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
