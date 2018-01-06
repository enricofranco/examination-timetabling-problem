import java.io.FileNotFoundException;
import java.io.IOException;

import it.polito.oma.solver.Handler;

public class Main {

	static final int PARAMETERS_ERROR = 1001;
	static final int INPUT_FILE_ERROR = 1002;
	static final int OUTPUT_FILE_ERROR = 1003;
	
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
			System.exit(PARAMETERS_ERROR);
		}
		
		instanceName = args[0];
		
		if(! args[1].equalsIgnoreCase("-t")) {
			displayErrorMessage("Unvalid option \"" + args[1] + "\".");
			System.exit(PARAMETERS_ERROR);
		}
		
		try {
			timeout = Long.valueOf(args[2]);
		} catch(NumberFormatException e) {
			displayErrorMessage("Unvalid timeout. It must be an integer number.");
			System.exit(PARAMETERS_ERROR);
		}
		
		Handler h = new Handler(timeStart, timeout);
		
		// Read files
		try {
			h.loadInstance(instanceName);
		} catch (FileNotFoundException e) {
			System.err.println("Unvalid instance name. Files not found.");
			System.exit(INPUT_FILE_ERROR);
		} catch (IOException e) {
			System.err.println("Unvalid instance name. Files not found.");
			System.exit(INPUT_FILE_ERROR);
		}
		
		h.initialize();
		
		// Write the solution on file
		try {
			h.writeSolution(instanceName);
		} catch (IOException e) {
			System.err.println("Error occured during writing solution file. Execution aborted.");
			System.exit(OUTPUT_FILE_ERROR);
		}
		
		System.out.format("Execution time: %.3f s",
				(System.nanoTime() - timeStart) / 1000000000.0);
	}
	
	private static void displayErrorMessage(String description) {
		System.err.println("Parameters error. " + description +
				"\nThe correct usage is ETPsolver_OMAAL_group09.exe instancename -t timelimit.");
	}
	
}
