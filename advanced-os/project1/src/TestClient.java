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
 * Class:  CS 6378.0U1
 * 
 */


public class TestClient {
	
	public static final int PORT_NUM = 34197;
	
	public static void main(String[] args) throws Exception {
		//Open our socket on the local machine with the specified port number
		Socket sock = new Socket("localhost", PORT_NUM);
				
		//Code for handling command-line interface
		Scanner scan = new Scanner(System.in);
		System.out.println("Client started.");
		System.out.println("Type \"END\" to close the connection.");
		
		boolean done = false;
		while(!done) {
			String cmd = scan.nextLine();
			DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			out.writeUTF(cmd);

			if(cmd.equals("END")) break;
		}

		scan.close();
		sock.close();
		return;
	}
}
