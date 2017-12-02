package it.polito.oma.solver.threads;

import java.util.Map;
import java.util.Random;

import it.polito.oma.solver.*;

public class Generator implements Runnable  {

	private int T;
	private Map<Integer, Exam> exams;
	private int E;
	private Map<Integer, Student> students;
	private int S;
	private int[] solution;
	private boolean finish = false;

	/*
	 * Set variables into the random generator
	 */
	public Generator(int T, Map<Integer, Exam> exams, Map <Integer, Student> students) {
		this.T = T;
		this.exams = exams;
		this.E = exams.size();
		this.students = students;
		this.S = students.size();
		solution = new int[E];
	}
	
	@Override
	public void run() {
		/*
		 * set random parameters
		 */
		Random rand = new Random();
		boolean feasible = false;
		rand.setSeed(System.nanoTime());
		
		/*
		 * the vector isn't filled linearly, but every jump position, chosen randomly 
		 */
		int jump = rand.nextInt(E/2)+1;
		
		for(int i = 0; i < E; ++i) {
			solution[(i*jump)%E] = rand.nextInt(T) + 1; //timeslot is choosen randomly
			jump = rand.nextInt(jump/4 + 1) + jump/4 + 1; //then reset the jump parameter
		}
		finish = true;
	}
	
	public int[] getSolution() {
		return solution;
	}
	
	public boolean isFinished() {
		return finish ;
	}

}
