import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * Author: Edward Hong
 * Class:  CS 6378.001
 * 
 */


public class Server {
	
	public static final int PORT_NUM = 34197;
	
	public static void main(String[] args) throws Exception {
		System.out.println("Server is running");
		
		// Open a ServerSocket (TCP) using the specified port number.
		ServerSocket serverSock = new ServerSocket(PORT_NUM);
		
		// Wait and listen for a connection.
		// Note that the below line is a blocking call until a client has connected.
		Socket sock = serverSock.accept();
		System.out.println("Client connected!");
		DataInputStream in = new DataInputStream(sock.getInputStream());
		
		Server ftps = new Server();
		
		while(true) {
			// The first thing which should've been written to the buffer is either
			// the message "SEND" or "RECEIVE" - This is how the server knows the type of request
			try {
				String request = in.readUTF();
				if(request.equals("SEND")) {
					System.out.println("Server got SEND request");
					ftps.sendFile(sock);
				} else if (request.equals("RECEIVE")) {
					System.out.println("Server got RECEIVE request");
					ftps.receiveFile(sock);
				} else if (request.equals("FIN")) {
					System.out.println("Server got FIN request -- Closing Connection!");
					break;
				}
			} catch(EOFException e) {
				// End of File Exception means that there are no bytes in the buffer.
				// Therefore, if we check the buffer and nothing is there, wait 5ms and check again.
				Thread.sleep(5);
			}			
		}
		serverSock.close();
	}
	
	public void sendFile(Socket soc) throws Exception {
		DataInputStream in = new DataInputStream(soc.getInputStream());
		DataOutputStream out = new DataOutputStream(soc.getOutputStream());
		
		// Read from the buffer the name and path of the requested file
		String filename = in.readUTF();
		System.out.println("Reading File "+filename);
		
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
	
	public void receiveFile(Socket soc) throws Exception {	
		DataInputStream in = new DataInputStream(soc.getInputStream());
		
		// Read from the buffer the file name and file path which is being sent
		String filename = in.readUTF();
		System.out.println("Receiving File "+filename);
		
		// For testing, received file will be stored in default parent directory.
		// This way, our sent and received files will not be stored in the same location,
		// so we can verify that our implementation works.
		filename = filename.substring(filename.indexOf('/') + 1);
		
		// This block handles reading from the input stream / buffer and writing to a new file
		// with the given name at the specified path
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
}
