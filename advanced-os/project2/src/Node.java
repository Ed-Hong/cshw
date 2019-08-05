import java.io.DataOutputStream;
import java.io.FileOutputStream;
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
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public class Node {

	// Global Parameters
	public static int NUM_NODES;
	public static int INTER_REQUEST_DELAY;
	public static int CS_EXECUTION_TIME;
	public static int NUM_REQUESTS;
	public static String CONFIG_NAME;

	// Index of All Nodes
	public static HashMap<Integer, Node> nodes = new HashMap<>();
	public static int startingNodeId = Integer.MAX_VALUE;

	// The Node on the current host
	static Node self;

	// Node properties
	public int id;
	public String hostName;
	public int listenPort;
	private int clock;

	public Node(int id, String hostName, int listenPort) {
		this.id = id;
		this.hostName = hostName;
		this.listenPort = listenPort;
		this.clock = 0;
	}

	public synchronized int incrementClock() {
		clock++;
		return clock;
	}

	public synchronized int getClock() {
		return clock;
	}

	public synchronized int setClock(int clk) {
		clock = clk;
		return clock;
	}

	public static void main(String[] args) throws Exception {
		// Create scanner for config file - filename of config is passed as args[0]
		Scanner cfgScanner = null;
		try {
			String fileName = args[0];
			CONFIG_NAME = fileName.substring(0, fileName.indexOf(".txt"));
			File configFile = new File(fileName);
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
		} catch (UnknownHostException e) {
			System.out.println("! Error attempting to get hostname.");
			e.printStackTrace();
			cfgScanner.close();
			return;
		}

		//DEBUG - todo remove this
		try {
			hostname = args[1];
		} catch (Exception e) {
			System.out.println("DEBUG: Please input a mock hostname for development purposes.");
			return;
		}

		// Initialize this node with this machine's hostname
		init(hostname);
		
		// Initialize Mutex singleton instance
		Mutex.init(self);

		// Begin application
		runApplication();

		return;
	}

	private static void init(String currentHostname) {
		for (Integer id : nodes.keySet()) {
			Node n = nodes.get(id);
			if (n.hostName.equals(currentHostname)) {
				self = new Node(n.id, n.hostName, n.listenPort);
				break;
			}
		}
	}

	private static void config(Scanner cfg) {
		int[] params = new int[4];
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
		NUM_NODES 				= params[0];
		INTER_REQUEST_DELAY 	= params[1];
		CS_EXECUTION_TIME 		= params[2];
		NUM_REQUESTS 			= params[3];
	}

	private static void idle(long millis) {
		try {
			Thread.sleep(millis);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void runApplication() {
		long waitTime;
		for(int i = 0; i < NUM_REQUESTS; i++) {
			Mutex.getInstance().enter();	// Blocking until request granted
			logStart();

			// Executing critical section
			self.incrementClock();	// Internal Event
			idle(getRandomWaitTime(CS_EXECUTION_TIME));

			Mutex.getInstance().exit();		// Exit critical section and release
			logEnd();

			// Wait a random amount of time before requesting again
			idle(getRandomWaitTime(INTER_REQUEST_DELAY));
		}

		done();
	}

	private static void done() {
		System.out.println("\n");
		System.out.println(self.id + ": DONE");
		testMutex();

		if (self.id == startingNodeId) {
			Mutex.getInstance().addDone();
		} else {
			Mutex.getInstance().done();
		}
	}

	private static long getRandomWaitTime(int lambda) {
		Random rng = new Random();
		return (long) ((Math.log(1 - rng.nextDouble()) / (-lambda)) * 1000);
	}

	private static void log(String log) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("output.out", true)); 
			writer.newLine();
			writer.write(log);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void logStart() {
		String log = self.id + " : enters at " + System.currentTimeMillis();
		log(log);
	}

	private static void logEnd() {
		String log = self.id + " : exits at  " + System.currentTimeMillis() + "\n";
		log(log);
	}

	private static void testMutex() {
		boolean checkNext = true;
		boolean success = true;
		int prevNode = -1;
		long prevTS = 0;

		try {
			File outputFile = new File("output.out");
			Scanner scan = new Scanner(outputFile);
	
			while (scan.hasNextLine()) {
				String line = scan.nextLine();

				if(line.length() == 0) {
					checkNext = true;
					break;
				}

				int node = Integer.parseInt(line.split(" ")[0]);
				long timestamp = Long.parseLong(line.split(" ")[3]);

				if (checkNext) {
					prevNode = node;
					checkNext = false;
				} else {
					if(prevNode != node) {
						success = false;
						break;
					}
				}

				if(prevTS < timestamp) {
					prevTS = timestamp;
				} else {
					success = false;
					break;
				}
			}
	
			if(success) {
				System.out.println(self.id + ": MUTEX SUCCESS");
			} else {
				System.out.println(self.id + ": MUTEX FAIL");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}