import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.Scanner;

/*
 * Author: Edward Hong
 * Class:  CS 4390.002
 * Client-Side code for the File Transfer Protocol (FTP) implementation
 */


public class FTP_Client {
	
	public static final int PORT_NUM = 34197;
	
	public static void main(String[] args) throws Exception {
		//First open our socket on the local machine with the specified port number
		Socket sock = new Socket("localhost", PORT_NUM);
		
		FTP_Client ftpc = new FTP_Client();
		
		//Code for handling command-line interface
		Scanner scan = new Scanner(System.in);
		System.out.println("Client started.");
		System.out.println("Type \"SEND\" or \"REC\" to initiate File Transfer.");
		System.out.println("Type \"FIN\" when finished.");
		
		boolean done = false;
		while(!done) {
			String cmd = scan.nextLine();
			
			if(cmd.equalsIgnoreCase("FIN")) {
				System.out.println("Finished - Closing Connection.");
				
				ftpc.closeConnection(sock);
				done = true;
				scan.close();
				sock.close();
			} else if(cmd.equalsIgnoreCase("SEND")) {
				System.out.print("Type file path to be sent: ");
				String filename = scan.nextLine();
				System.out.println("Reading File "+filename);
				
				try {
					ftpc.sendFile(sock, filename);
				} catch(FileNotFoundException e) {
					System.out.println("Could not find the specified file.");
				}				
			} else if(cmd.equalsIgnoreCase("REC")) {
				System.out.print("What file do you want to receive?");
				String filename = scan.nextLine();
				
				ftpc.receiveFile(sock, filename);				
			} else {
				System.out.println("Please input a valid command.");
			}
		}
	}
	
	public void sendFile(Socket soc, String filename) throws Exception {
		DataOutputStream out = new DataOutputStream(soc.getOutputStream());
		// We first write in the buffer a specific message to tell the server what request
		// is to be handled. In this case, we are requesting that the server RECEIVE our file.
		out.writeUTF("RECEIVE");
		
		// Here we write to the buffer the file name itself. This is how the receiver learns of
		// the file name, type, and its path.
		out.writeUTF(filename);
		
		// This block handles reading the individual bytes from the file and writing them to the buffer.
		File file = new File(filename);
		FileInputStream fin = new FileInputStream(file);
		int ch;
		do {
			ch = fin.read();
			out.writeUTF(String.valueOf(ch));
		} while(ch != -1);
		fin.close();
		
		System.out.println("File Sent");
	}
	
	public void receiveFile(Socket soc, String filename) throws Exception {	
		DataInputStream in = new DataInputStream(soc.getInputStream());
		DataOutputStream out = new DataOutputStream(soc.getOutputStream());
		
		// Again, we write a specific control message to the I/O stream so that the Server knows
		// how to handle the incoming request.
		out.writeUTF("SEND");
		// Write to the buffer the desired file name and path - the server will use this path
		// to find the file to be sent.
		out.writeUTF(filename);
		
		// For testing, received file will be stored in default parent directory.
		// This way, our sent and received files will not be stored in the same location,
		// so we can verify that our implementation works.
		filename = filename.substring(filename.indexOf('/') + 1);
	
		System.out.println("Receiving File "+filename);
		
		// This block handles reading the individual bytes received from the server (through the 
		// input stream) and writing them to a file located at the specified path.
		File file = new File(filename);
		FileOutputStream fileOut = new FileOutputStream(file);
		int ch;
		do {
			ch = Integer.parseInt(in.readUTF());
			if(ch != -1) {
				fileOut.write(ch);
			}
		} while(ch != -1);

		System.out.println("File Received");
		fileOut.close();
		}
	
	public void closeConnection(Socket soc) throws Exception {
		// To close our connection from the client-side, we send to the server a FIN request
		DataOutputStream out = new DataOutputStream(soc.getOutputStream());
		out.writeUTF("FIN");
	}
}
