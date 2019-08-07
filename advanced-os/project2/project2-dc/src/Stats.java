import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Scanner;

public class Stats {
    public static void main(String[] args) {
        int responseTimeTotal = 0;
        int responseTimeN = 0;

        int messagesSentTotal = 0;
        int messagesSentN = 0;

		try {
			File outputFile = new File("stats.out");
			Scanner scan = new Scanner(outputFile);
	
			while (scan.hasNextLine()) {
                String line = scan.nextLine();

                if(line.length() <= 0) continue;

                String[] params = line.split(" = ");

                String stat = params[0];
                String value = params[1];

                if(stat.contains("response time")) {
                    responseTimeTotal += Integer.parseInt(value);
                    responseTimeN++;
                }

                if(stat.contains("messages sent")) {
                    messagesSentTotal += Integer.parseInt(value);
                    messagesSentN++;
                }
            }
            
            float avgResponseTime = responseTimeTotal / responseTimeN;
            float avgMessagesSent = messagesSentTotal / messagesSentN;

            System.out.println("Avg Response Time = " + avgResponseTime);
            System.out.println("Avg Messages Sent = " + avgMessagesSent);

		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
