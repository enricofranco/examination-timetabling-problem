import it.polito.oma.solver.Handler;

public class Main {

	public static void main(String[] args) {
		Handler h = new Handler();
		h.loadInstance("./src/demo/test");
		h.initialize();
	}
}
