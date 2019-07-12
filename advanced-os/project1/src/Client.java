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
					threads.get(destNode.id).addMessage(appMessage(destNode.id, self.id));
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
					threads.get(id).addMessage(markMessage(id, self.id));
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
			if(self.hasDoneMessage()) { 
				// Root node initiates
				if(self.isRoot) {
					while(self.hasDoneMessage()) {
						self.removeDoneMessage();
						for (Node neighbor : self.neighbors) {
							threads.get(neighbor.id).addMessage(doneMessage(neighbor.id, self.id));							
						}
					}
				} else {
					// Multicast DONE message to all neighbors
					while(self.hasDoneMessage()) {
						int id = self.removeDoneMessage();
						threads.get(id).addMessage(doneMessage(id, self.id));
					}
				}
				
				// todo root has to wait for all other processes before terminating
				if (!self.isRoot) {
					System.out.println("YOU HAVE BEEN TERMINATED.");
					self.setTerminated(true);
					break;
				}
			}

			if (self.isTerminated()) {
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
	
	private static String appMessage(int destId, int senderId) {
		return "APP to " + destId + " from " + senderId;
	}
	
	private static String markMessage(int destId, int senderId) {
		return "MARK to " + destId + " from " + senderId;
	}

	private static String doneMessage(int destId, int senderId) {
		return "DONE to " + destId + " from " + senderId;
	}
}