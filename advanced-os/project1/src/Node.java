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
import java.util.HashSet;
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
	public boolean isActive;

	public Node(int id, String hostName, int listenPort) {
		this.id = id;
		this.hostName = hostName;
		this.listenPort = listenPort;
		this.neighbors = new ArrayList<>();
		this.isActive = id == startingNodeId ? true : false;
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

		//debug self and self's neighbors
		// System.out.println(" Self-nodeId: " + self.id + " Self-hostName: " + self.hostName + " Self-NodeId: " + self.listenPort);
		// System.out.println("Self-neighbors: ");
		// for(Node n : self.neighbors) {
		// 	System.out.println(n.id + " ");
		// }

		// Spawn a Server-side thread, which then spawns client handler threads
		new Server(self).start();

		// Spawn a Client-side thread, which spawns client threads to neighbors
		new Client(self).start();
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
				
				new ServerThread(sock, in, out).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}

class ServerThread extends Thread { 
	final Socket sock; 
	final DataInputStream in; 
    final DataOutputStream out; 
        
    public ServerThread (Socket s, DataInputStream is, DataOutputStream os) { 
        this.sock = s; 
        this.in = is; 
        this.out = os; 
    } 
  
    @Override
    public void run() { 
		while(true) {
			try {
				String request = in.readUTF();
				System.out.println("  Received: " + request);
				if (request.equals("END")) {
					break;
				}
			} catch(EOFException eof) {
				try {
					// No bytes in the buffer; wait 5ms and check again
					Thread.sleep(5);	
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e ) {
				e.printStackTrace();
			}
		}

		System.out.println("> Closing client connection.");
		try {
			sock.close();
			in.close();
			out.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}

class Client extends Thread {
	final Node self;

	public Client(Node n) {
		this.self = n;
	}

    @Override
    public void run() {
		HashSet<Integer> neighborsConnected = new HashSet<>();
		while(true) {
			if (neighborsConnected.size() == self.neighbors.size()) break;

			// Spawn a new thread for each channel to each neighbor
			for (Node neighbor : self.neighbors) {
				if (neighborsConnected.contains(neighbor.id)) continue;
				try {
					//todo set hostname
					Socket sock = new Socket("localhost", neighbor.listenPort);
					DataInputStream in = new DataInputStream(sock.getInputStream());	
					DataOutputStream out = new DataOutputStream(sock.getOutputStream());	
					neighborsConnected.add(neighbor.id);
					new ClientThread(sock, in, out, self).start();
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
    }
}

class ClientThread extends Thread { 
	final Node client;
	final Socket sock; 
	final DataInputStream in; 
    final DataOutputStream out; 

	public ClientThread (Socket s, DataInputStream is, DataOutputStream os, Node cl) { 
        this.sock = s; 
        this.in = is; 
		this.out = os; 
		this.client = cl;
    } 

    @Override
    public void run() {
		while(true) {
			String str = "Node " + client.id + " connected!";
			try {
				out.writeUTF(str);
			} catch (IOException i) {
				i.printStackTrace();
			}

			//debug jesus lets avoid an implosion here
			break;
		}
	}
}
