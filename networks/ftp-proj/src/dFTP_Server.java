import java.io.*;
import java.net.*;
import java.util.Scanner;

/*
 * Author: Edward Hong
 * Class:  CS 4390.002
 * Server-Side code for the UDP / Datagram implementation of a File Transfer Protocol (FTP)
 */

class dFTP_Server {
	
	public static final int RECEIVER_PORT_NUM = 34197;
	public static final int SENDER_PORT_NUM = 25565;

	public static void main(String args[])throws IOException,SocketException,Exception {
		System.out.println("UDP SERVER is Running");
		
		// Open our socket which we will use to send our datagram packet
		DatagramSocket dSock = new DatagramSocket(SENDER_PORT_NUM);
	
		Scanner scan = new Scanner(System.in);
		System.out.println("Type \"SEND\" to send a file.");
		System.out.println("Type \"FIN\" when finished.");
		
		boolean done = false;
		while(!done) {
			String cmd = scan.nextLine();
			
			if(cmd.equalsIgnoreCase("FIN")) {
				System.out.println("Finished - Closing Connection.");
				//ftpc.closeConnection(sock);
				done = true;
				scan.close();
				dSock.close();
			} else if(cmd.equalsIgnoreCase("SEND")) {
				System.out.print("Type file path to be sent: ");
				String filename = scan.nextLine();
				System.out.println("Reading File "+filename);
				
				try {
					
					// Create a file instance and an input stream which we use to write to our byte-array
					File file = new File(filename);
					FileInputStream fileInput = new FileInputStream(file);
					System.out.println("File Length = " + file.length());
					DatagramPacket packet;
					
					// Not handling file sizes greater than 64kB, which is the maximum amount of data a UDP packet may hold.
					if(file.length() > 64000) {
						System.out.println("File size is too large - please send using a different protocol.");
					} else {
						// Create our byte array and write to it the data bytes from our file
						byte[] b = new byte[(int)file.length()]; 
						fileInput.read(b,1,b.length - 1);
						fileInput.close();
						
						// Create our datagram packet and address it with the recipients IP address and port number
						packet = new DatagramPacket(b,b.length,InetAddress.getLocalHost(),RECEIVER_PORT_NUM);
						// Finally, send the packet
						dSock.send(packet);
					}
			
					System.out.println("File Sent");
					
					
				} catch(FileNotFoundException e) {
					System.out.println("Could not find the specified file.");
				}				
			} else {
				System.out.println("Please input a valid command.");
			}
		}
	}
}