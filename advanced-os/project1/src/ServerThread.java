import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.io.EOFException;
import java.io.IOException;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 1
 */

public class ServerThread extends Thread { 
	final Node self;
	final Socket sock; 
	final DataInputStream in; 
    final DataOutputStream out; 
        
    public ServerThread (Socket s, DataInputStream is, DataOutputStream os, Node n) { 
        this.sock = s; 
        this.in = is; 
		this.out = os; 
		this.self = n;
    } 
  
    @Override
    public void run() { 
		while(true) {
			try {
				String request = in.readUTF();
				System.out.println("  Received: " + request);

				// MAP Protocol messages are application messages and begin with "APP"

				// Record app messages received while red as channel state
				//todo once i've recorded all my channel states then i'm done
				if (self.isRed() && request.startsWith("APP")) {

					// Application messages are stamped by sender at the end
					int channel = Integer.parseInt(request.substring(request.lastIndexOf(" ") + 1));
					self.channels.get(channel).add(request);
				}

				if(request.startsWith("APP") && !self.isActive() && self.sentMessageCount < Node.MAX_NUMBER) {
					self.setActive(true);
				}

				// Snapshot Protocol messages are system messages and begin with "MARK"
				if(request.startsWith("MARK")) {
					int nodeId = Integer.parseInt(request.substring(request.lastIndexOf(" ") + 1));
					self.receiveMarkerMessage(nodeId);
					
					// Change color from blue to red
					if(!self.isRed()) {
						System.out.println("  COLOR CHANGE TO RED");
						for (Node neighbor : self.neighbors) {
							self.addMarker(neighbor.id);	
						}
						self.setRed(true);

						// Init channel states
						for (Node neighbor : self.neighbors) {
							self.channels.put(neighbor.id, new ArrayList<String>());
						}
					} else if (!self.isFinishedLocal() && 
							  (!self.isRoot && self.getReceivedMarkerCount() >= self.channels.keySet().size() ||
							  (self.isRoot && self.getReceivedMarkerCount() > self.channels.keySet().size()))) {
							// Once this node has received all marker messages from all neighbors (the root receiving one from itself)
							self.setFinishedLocal(true);
							System.out.println("  LOCALLY FINISHED");
                            
                    
                            // Get total number of messages in channels
                            int numChannelMsgs = 0;
                            for (int key : self.channels.keySet()) {
                                numChannelMsgs += self.channels.get(key).size();
                            }

                            //debug
                            System.out.println("Relaying FIN to Neighbors.");
                            System.out.println("Messages in channels = " + numChannelMsgs);

							// Multicast FIN message to all neighbors (if not root node)
							if (!self.isRoot) {
								for(Node n : self.neighbors) {
                                    //todo attach extra data to fin message like clock, etc
                                    //todo send FIN message only to my parent which is the neighbor that I first receive a marker from
                                    //todo flesh out FIN messages with actual channel states
                                    self.addFinMessage(n.id);
								}
						}
					}
				}

				// Non-root Nodes forward FIN messages to the root
				if (request.startsWith("FIN") && !self.hasDoneMessage()) {
					int nodeId = Integer.parseInt(request.substring(request.lastIndexOf(" ") + 1));

					// Root Node receives accounts for FIN messages
					if(self.id == Node.startingNodeId) {
						self.addFinMessageToSet(nodeId);
					} else {
						self.addFinMessage(nodeId);
					}
				}

				// Receiving DONE message begins termination
				if (request.startsWith("DONE")) {
					int nodeId = Integer.parseInt(request.substring(request.lastIndexOf(" ") + 1));

					self.addDoneMessage(nodeId);
					//break;
				}

				if (self.isTerminated()) {
					break;
				}

			} catch(EOFException eof) {
				try {
					// No bytes in the buffer; wait 5ms and check again
					Thread.sleep(5);	
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			//cleanup
			// try {
			// 	sock.close();
			// 	in.close();
			// 	out.close();
			// } catch (IOException e) {
			// 	e.printStackTrace();
			// }
		}
    }
}