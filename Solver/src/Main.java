import java.io.FileNotFoundException;
import java.io.IOException;

import it.polito.oma.solver.Handler;

public class Main {

	/**
	 * args format: instancename -t timelimit
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		long timeStart = System.nanoTime(); // Start timer
		String instanceName = "instance02";
		Handler h = new Handler();
		
		// Read files
		try {
			h.loadInstance("./src/demo/" + instanceName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		h.initialize();
		
		// Write the solution on file
		try {
			h.writeSolution(instanceName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println((System.nanoTime() - timeStart) / 1000000000.0);
	}
}
