import java.io.IOException;

import it.polito.oma.solver.Handler;

public class Main {

	/**
	 * args format: instancename -t timelimit
	 * @param args Command line arguments
	 */	
	public static void main(String[] args) {
		Handler h = new Handler();
		h.loadInstance("./src/demo/instance01");
		h.initialize();
		
		try {
			h.writeSolution("test");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
