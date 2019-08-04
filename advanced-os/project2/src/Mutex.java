import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/*
 * Author: Edward Hong
 * Class:  CS 6378.0U1
 * Advanced Operating Systems - Project 2
 */

public static class Mutex {

	public static Queue<Integer> requests = new LinkedList<>();

    public static void enter() {
        System.out.println("* Entering critical section...");
    }
    public static void exit() {
        System.out.println("* Exiting critical section.");
    }
}