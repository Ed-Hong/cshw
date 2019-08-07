import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        
		boolean checkNext = true;
		boolean success = true;
		int prevNode = -1;
		long prevTS = 0;

		try {
			File outputFile = new File("output.out");
			Scanner scan = new Scanner(outputFile);
	
			while (scan.hasNextLine()) {
				String line = scan.nextLine();

				if(line.length() == 0) {
					checkNext = true;
					continue;
				}
				System.out.println("line: " + line);
				
				int node = Integer.parseInt(line.split(" ")[0]);
				long timestamp = Long.parseLong(line.split(" ")[4]);

				System.out.println("prevTS: " + prevTS);
				System.out.println("currTS: " + timestamp);

				if (checkNext) {
					prevNode = node;
					checkNext = false;
				} else {
					if(prevNode != node) {
						success = false;
						break;
					}
				}

				if(prevTS < timestamp) {
					prevTS = timestamp;
				} else {
					success = false;
					break;
				}
			}
	
			if(success) {
				System.out.println("MUTEX SUCCESS");
			} else {
				System.out.println("MUTEX FAIL");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

<<<<<<< HEAD
}
=======
}
>>>>>>> origin/master
