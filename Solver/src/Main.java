import it.polito.oma.solver.*;

public class Main {

	public static void main(String[] args) {
		Handler h = new Handler();
		h.loadInstance("./src/demo/test");
		h.initialize();
	}
}
