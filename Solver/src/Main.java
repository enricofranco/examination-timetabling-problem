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
		long timeout = 0;
		String instanceName;
		
		if(args.length < 3) {
			displayErrorMessage("");
			System.exit(1);
		}
		
		instanceName = args[0];
		
		if(! args[1].equalsIgnoreCase("-t")) {
			displayErrorMessage("Unvalid option \"" + args[1] + "\".");
			System.exit(1);
		}
		
		try {
			timeout = Long.valueOf(args[2]);
		} catch(NumberFormatException e) {
			displayErrorMessage("Unvalid timeout. It must be an integer number.");
			System.exit(1);
		}
		
		Handler h = new Handler(timeStart, timeout);
		
		// Read files
		try {
			h.loadInstance(instanceName);
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
		
		System.out.println("Tempo Esecuzione " + (System.nanoTime() - timeStart) / 1000000000.0);
	}
	
	private static void displayErrorMessage(String description) {
		System.err.println("Parameters error. " + description +
				"\nThe correct usage is ETPsolver_OMAAL_group09.exe instancename -t timelimit.");
	}
	
}
