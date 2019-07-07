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
	public static Node[] nodes;

	// Topology Map for debug
	public static HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();

	public int id;
	public String hostName;
	public int listenPort;
	public HashMap<Integer, Node> neighbors;

	public Node(int id, String hostName, int listenPort) {
		this.id = id;
		this.hostName = hostName;
		this.listenPort = listenPort;
		this.neighbors = new HashMap<>();
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

		// Read config file and setup
		config(cfgScanner, hostname);

		//debugging network topology
		for (Integer key : map.keySet()) {
			ArrayList<Integer> neighbors = map.get(key);
			System.out.println("Node " + key + " neighbors: ");
			for (Integer neighbor : neighbors) {
				System.out.print(neighbor + " ");
			}
			System.out.println();
		}
	}

	private static void config(Scanner cfg, String currentHostname) {
		int[] params = new int[6];

		System.out.println("Configuring this node...");
		Scanner line = null;
		int lineNum = 0;

		// Read each line
		boolean isFirstPass = true;
		while (cfg.hasNextLine()) {
			String lineStr = cfg.nextLine();
			line = new Scanner(lineStr);
			boolean isValidLine = true;
			int tokenNum = 0;

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
						//debug --- todo populate index
						case 0:
							System.out.println("NodeId = " + token);
						break;

						case 1:
						System.out.println("hostName = " + token);
						break;

						case 2:
						System.out.println("listenPort = " + token);
						break;
					}
				}

				// Neighbors of N Nodes
				if (lineNum > NUM_NODES && lineNum <= 2*NUM_NODES) {
					int nodeId = lineNum - NUM_NODES;
					int neighborId = Integer.parseInt(token);

					//debug - populating topology map
					if(map.get(nodeId) == null) {
						map.put(nodeId, new ArrayList<Integer>());
					}
					map.get(nodeId).add(neighborId);

					//todo populate self's neighbors
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
