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
				if (self.isRed() && request.startsWith("APP")) {
                    String[] params = request.split("_");

					int channel = Integer.parseInt(params[Message.SOURCE_INDEX]);
					self.channels.get(channel).add(request);
				}

				if(request.startsWith("APP") && !self.isActive() && self.sentMessageCount < Node.MAX_NUMBER) {
					self.setActive(true);
				}

				// Snapshot Protocol messages are system messages and begin with "MARK"
				if(request.startsWith("MARK")) {
                    String[] params = request.split("_");

					int nodeId = Integer.parseInt(params[Message.SOURCE_INDEX]);
					self.receiveMarkerMessage(nodeId);
					
                    // Change color from blue to red
					if(!self.isRed()) {
                        self.setRed(true);
                        
                        //todo this needs to NOT happen more than once
                        System.out.println("  COLOR CHANGE TO RED");
                        System.out.println("  SETTING PARENT ID = " + nodeId);
                        self.parentId = nodeId;
                        
						for (Node neighbor : self.neighbors) {
							self.addMarker(neighbor.id);	
						}

						// Init channel states
						for (Node neighbor : self.neighbors) {
							self.channels.put(neighbor.id, new ArrayList<String>());
						}
					} else if (!self.isFinishedLocal() && 
							  (!self.isRoot && self.getReceivedMarkerCount() >= self.channels.keySet().size() ||
							  (self.isRoot && self.getReceivedMarkerCount() > self.channels.keySet().size()))) {
                            // Node is finished recording local state and channel states
                            // On receiving all marker messages from all neighbors (the root receiving one from itself)
							self.setFinishedLocal(true);
							System.out.println("  LOCALLY FINISHED");

                            // Send FIN message to my parent, who will forward to Root
                            if(!self.isRoot) {
                                self.addFinMessage(
                                    new FinishMessage(
                                        self.id, 
                                        self.parentId, 
                                        self.isActive(), 
                                        self.countMessagesInChannels(), 
                                        self.clock)
                                    );
                            }
                            
					}
				}

				// Receiving FIN: Non-root Nodes forward FIN messages to the root
				if (request.startsWith("FIN")) {
                    String[] params = request.split("_");

					// Root Node accounts for FIN messages to determine termination
					if(self.isRoot) {
                        self.receiveFinMessage(
                                new FinishMessage(
                                    params[Message.SOURCE_INDEX], 
                                    params[Message.DESTINATION_INDEX], 
                                    params[FinishMessage.IS_ACTIVE_INDEX],
                                    params[FinishMessage.NUM_CHANNEL_MSGS_INDEX], 
                                    params[FinishMessage.VECTOR_CLOCK_INDEX]
                                )
                        );

                        self.checkMAPTermination();

					} else {
                        // Receiving FIN message from child node, forward to my parent
                        self.addFinMessage(
                            new FinishMessage(
                                params[Message.SOURCE_INDEX], 
                                String.valueOf(self.parentId), 
                                params[FinishMessage.IS_ACTIVE_INDEX],
                                params[FinishMessage.NUM_CHANNEL_MSGS_INDEX], 
                                params[FinishMessage.VECTOR_CLOCK_INDEX]
                                )
                            );
					}
				}

                //onReceiveDone - if from parent, send all neighbors DONE and wait for all neighbors' DONE-ACK 
				//				  if not from parent, then send DONE-ACK

                
				// Receiving TERM message begins termination
				if (request.startsWith("TERM")) {
                    String[] params = request.split("_");
                    int sourceId = Integer.parseInt(params[Message.SOURCE_INDEX]);
                    
                    // If TERM message is from parent
                    if(sourceId == self.parentId) {
                        System.out.println("Got TERM from parent");
                        self.receivedParentTerm();
                        // Add TERM messages for all neighbors to buffer
                        for(Node neighbor : self.neighbors) {
                            if(neighbor.id != self.parentId) {
                                System.out.println("Add TERMs for all non-parent neighbors to buffer");
                                self.addTerminateMessage(
                                    new TerminateMessage(self.id, neighbor.id)
                                );
                            }
                        }
                    } else {
                        System.out.println("Got TERM from neighbor (NOT PARENT)");
                        System.out.println("Adding TERM-ACK for neighbor");
                        // TERM message is not from parent, send a TERM-ACK (TACK)
                        // if already received TERM from parent, otherwise buffer it.
                        self.addTermAckMessage(
                            new TermAckMessage(self.id, sourceId)
                        );
                    }

					//break;
                }
                
                //onReceiveDoneAck - 
                //if all neighbors have sent a DONE-ACK then send parent DONE-ACK, and terminate


                // Receiving TACK message
				if (request.startsWith("TACK")) {
                    String[] params = request.split("_");

                    System.out.println("Got TERM-ACK, adding to set");
                    self.addTermAckToSet(Integer.parseInt(params[Message.SOURCE_INDEX]));

                    // Once all TERM-ACKs have been received, add TERM-ACK to buffer to send to parent
                    if(self.receivedAllTermAcks()) {
                        if(self.isRoot) {
                            System.out.println("ROOT RECEIVED ALL TERM-ACKS, ALL NODES HALTED");
                            //todo once all nodes halted, make sure the nodes actually stop
                            //and output from root node. (part 4)
                        } else {
                            System.out.println("ALL TERM-ACKs RECEIVED, sending ACK to parent");
                            self.addTermAckMessage(
                                new TermAckMessage(self.id, self.parentId)
                            );
                        }
                    }
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