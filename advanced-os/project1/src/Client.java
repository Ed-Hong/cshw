import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.util.Random;
import java.util.HashMap;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 1
 */

public class Client extends Thread {
	final Node self;
	final HashMap<Integer, ClientThread> threads = new HashMap<>();

	public Client(Node n) {
		this.self = n;
	}

    @Override
    public void run() {

        setupChannels();

		// Start all threads
		for(Integer id : threads.keySet()) {
			threads.get(id).start();
		}

		// Once all channels established, begin MAP 
		while(true) {
			if(self.isActive()) {
				System.out.println("  ACTIVE");
				int randomMsgCount = new Random().ints(1, Node.MIN_PER_ACTIVE, Node.MAX_PER_ACTIVE + 1).findFirst().getAsInt();
				while(randomMsgCount > 0) {
					if(self.sentMessageCount >= Node.MAX_NUMBER) {
						self.setActive(false);
						break;
					}
					int randomIndex = new Random().nextInt(threads.keySet().size());
                    Node destNode = self.neighbors.get(randomIndex);
                    
                    // Application Message send - increment my clock
                    self.incrementClock(self.id);

					threads.get(destNode.id).addMessage(new ApplicationMessage(self.id, destNode.id, self.getClock()).message);
					randomMsgCount--;
                    
					try {
						// Wait MIN_SEND_DELAY ms before sending again
						Thread.sleep(Node.MIN_SEND_DELAY);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				self.setActive(false);
			}
			
			// On Color Change from Blue to Red if we have marker messages to send, send them
			if(self.hasMarker()) {
				self.recordLocalState();

				// Multicast MARK message to all neighbors
				while(self.hasMarker()) {
					int id = self.removeMarker();
					threads.get(id).addMessage(new MarkerMessage(self.id, id).message);
				}
			}

            // Finished recording local state and channel state - sending FIN message to parent
            // And Forward any FIN messages received to parent
			if(self.hasFinMessage()) {
				while(self.hasFinMessage()) {
                    FinishMessage finMsg = self.removeFinMessage();
                    threads.get(finMsg.destinationId).addMessage(finMsg.message);
				}
			}

			// Node termination
			if(self.hasTerminateMessage()) {
				// Root node initiates
				if(self.isRoot) {
					while(self.hasTerminateMessage()) {
                        System.out.println("ROOT NODE initiating TERMINATION");
						self.removeTerminateMessage();
						for (Node neighbor : self.neighbors) {
							threads.get(neighbor.id).addMessage(new TerminateMessage(self.id, neighbor.id).message);							
						}
					}
				} else {
                    // Multicast TERM message to all neighbors
					while(self.hasTerminateMessage()) {
                        System.out.println("Sending TERM");
						TerminateMessage termMsg = self.removeTerminateMessage();
						threads.get(termMsg.destinationId).addMessage(termMsg.message);
					}
				}
            }
            
            // Sending Term-Ack messages once Node has received TERM from parent
            if(self.hasTermAckMessage() && self.hasReceivedParentTerm()) {
                while(self.hasTermAckMessage()) {
                    System.out.println("Sending TERM-ACK");
                    TermAckMessage tackMsg = self.removeTermAckMessage();
                    threads.get(tackMsg.destinationId).addMessage(tackMsg.message);
                }
            }

            // Terminate self once all remaining ACKs have been sent
            if (self.isTerminated() && !self.hasTermAckMessage()) {
				break;
			}
		}
		//cleanup
		//threads.clear();
    }
    
    private void setupChannels() {
        while(true) {
			if (threads.keySet().size() == self.neighbors.size()) break;	// Break once all channels established

			// Spawn a new thread for each channel to each neighbor
			for (Node neighbor : self.neighbors) {
				if (threads.keySet().contains(neighbor.id)) continue;
				try {
					//todo set hostname

					// Connect to neighbors
					Socket sock = new Socket("localhost", neighbor.listenPort);
					DataInputStream in = new DataInputStream(sock.getInputStream());	
					DataOutputStream out = new DataOutputStream(sock.getOutputStream());
					
					// Build index of channels to neighbors
					threads.put(neighbor.id, new ClientThread(sock, in, out, self));
				} catch (ConnectException c) {
					try {
						//debug
						//System.out.print(".");
						// Wait 50ms and try again
						Thread.sleep(50);	
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
    }
}