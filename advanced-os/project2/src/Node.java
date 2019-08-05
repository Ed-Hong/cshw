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

	// The Node on the current host
	static Node self;

	// Node properties
	public int id;
	public String hostName;
	public int listenPort;
	public int clock;

	public Node(int id, String hostName, int listenPort) {
		this.id = id;
		this.hostName = hostName;
		this.listenPort = listenPort;
		this.clock = 0;
	}

	// public synchronized void incrementClock() {
    //     _clock[nodeId]++;
	// }
	
	// public synchronized int[] getClock() {
	// 	return _clock;
	// }

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

		//printConfig();
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
	
	private static void printConfig() {
		//debug config
		System.out.println("NUM_NODES = " + NUM_NODES);
		System.out.println("INTER_REQUEST_DELAY = " + INTER_REQUEST_DELAY);
		System.out.println("CS_EXECUTION_TIME = " + CS_EXECUTION_TIME);
		System.out.println("NUM_REQUESTS = " + NUM_REQUESTS);
		for (Integer id : nodes.keySet()) {
			Node n = nodes.get(id);
			System.out.println("NodeId: " + n.id + " HostName: " + n.hostName + " PortNum: " + n.listenPort);
		}
	}

	private static void runApplication() {
		// wait a random amount of time, d, then call Mutex.enter()
		// upon receiving permission from Mutex, wait for cs execution time, c
		// once finished, call Mutex.exit()
		// do this for NUM_REQUESTS

		// keep it simple - for now assume node 0 makes a single request
		if(self.id == 0) {
			Mutex.getInstance().enter();
		}
	}
}