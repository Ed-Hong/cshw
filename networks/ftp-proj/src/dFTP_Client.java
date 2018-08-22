import java.io.*;
import java.net.*;

/*
 * Author: Edward Hong
 * Class:  CS 4390.002
 * Server-Side code for the UDP / Datagram implementation of a File Transfer Protocol (FTP)
 */

class dFTP_Client {
	
	public static final int RECEIVER_PORT_NUM = 34197;

	public static void main(String args[])throws IOException,SocketException {
		System.out.println("UDP CLIENT is Running");
		
		// Open a Datagram Socket on the Receiver Port
		DatagramSocket dSock = new DatagramSocket(RECEIVER_PORT_NUM);
		
		File file = new File("test.txt");
		FileOutputStream fileOutput = new FileOutputStream(file);
		DatagramPacket packet;
				
		while(true) {
			System.out.println("Waiting for file...");
			// Create our byte array
			byte[] buf = new byte[64000];
			// Create a packet to receive data from the buffer and wait until one is received
			// Note that the second line below is a blocking call until the socket receives a packet
			packet = new DatagramPacket(buf,buf.length);
			dSock.receive(packet);
			
			// Write the data from the buffer to the newly created file
			fileOutput.write(buf);
			
			System.out.println("File Received.");
		}
	}
}