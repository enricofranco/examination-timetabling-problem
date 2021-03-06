package it.polito.oma.solver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import it.polito.oma.solver.threads.Generator;

public class Handler {
	private static final int PENALTIES = 5;
	private static final int BASE_PENALTY = 2;
	private static final String GROUP = "_OMAAL_group09.sol";
	private static final int THREADS_NUMBER = 3;
	
	//Exams
	private Map<Integer, Exam> exams = new HashMap<>();
	private int E;
	
	//Students
	private Map<Integer, Student> students = new HashMap<>();
	private int S;
	
	//Timeslots
	private int T;
	
	//Obj function parameters
	private int[][] conflictWeight;
	private int[] p = new int[PENALTIES];
	private int[][][] conflict;
	private int[] solution;
	
	private final long timeStart;
	private final long timeout;
	
	/**
	 * This method creates a new Handler.
	 * @param timeStart - instant when the program started, expressed in nanosecond
	 * @param timeout - max time to execute the program, expressed in second
	 */
	public Handler(long timeStart, long timeout) {
		this.timeStart = timeStart;
		this.timeout = timeout;
	}

	/**
	 * This method loads all the instances into local variables.
	 * @param path The path of all the files to load
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void loadInstance(String path) throws FileNotFoundException, IOException {
		loadExams(path + ".exm");
		loadStudents(path + ".stu");
		loadTimeslot(path + ".slo");
	}
	
	/**
	 * This method loads all the exams into a local variable.
	 * @param path The path of .exm file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadExams(String path) throws FileNotFoundException, IOException {
		try(BufferedReader r = new BufferedReader(new FileReader(path))) {
			r.lines()
			.filter(s -> s.compareTo("") != 0)
			.map(l -> l.split(" "))
			.forEach(i -> {
				int id = Integer.valueOf(i[0]);
				Exam e = new Exam(id, Integer.valueOf(i[1]));
				exams.put(id, e);
			});
		}
		E = exams.size();
	}
	
	/**
	 * This method loads all the timeslots into a local variable.
	 * @param path The path of .slo file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadTimeslot(String path) throws FileNotFoundException, IOException {
		try(BufferedReader r = new BufferedReader(new FileReader(path))) {
			r.lines()
			.filter(s -> s.compareTo("") != 0)
			.map(l -> l.split(" "))
			.forEach(i -> {
				T = Integer.valueOf(i[0]);
			});
		}
	}

	/**
	 * This method loads all the students into a local variable.
	 * @param path The path of .stu file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadStudents(String path) throws FileNotFoundException, IOException {
		try(BufferedReader r = new BufferedReader(new FileReader(path))) {
			r.lines()
			.filter(s -> s.compareTo("") != 0)
			.map(l -> l.split(" "))
			.forEach(i -> {
				int sID = Integer.valueOf(i[0].replaceAll("s", ""));
				int eID = Integer.valueOf(i[1]);
				Exam e = exams.get(eID);
				Student s = students.get(sID);
				if(s == null) {
					s = new Student(sID);
					students.put(sID, s);
				}
				s.addExam(e);
			});
		}
		S = students.size();
	}
	
	/**
	 * This method writes a solution into a file.
	 * @param instanceName The name of the running instance
	 * @throws IOException
	 */
	public void writeSolution(String instanceName) throws IOException {
		String fileName = instanceName + GROUP;
		if(! isFeasible()) {
			try(BufferedWriter r = new BufferedWriter(new FileWriter(fileName))) {
				r.write("");
			}
			System.out.println("No feasible solution is found. File " + fileName + " was written empty.");
			return;
		} // If the solution is not feasible, do not write the file
		
		try(BufferedWriter r = new BufferedWriter(new FileWriter(fileName))) {
			for(int i = 0; i < E; ++i) {
				String s = String.format("%d %d%n", i+1, solution[i]);
				r.write(s);
			}
		}
		buildDistancies();
		System.out.format("Best solution found written on file " + fileName
				+ "%nObjective function: %.6f%n",
				objectiveFunction());
	}
	
	/**
	 * This method initializes the vectors used by the objective function.
	 */
	public void initialize() {
		int[] bestSolution = new int[E];
		double bestOF = Double.MAX_VALUE;
		conflictWeight = new int[E][E];
		conflict = new int[E][E][PENALTIES];
		solution = new int[E];
		setPenalties();
		buildConflictWeight();

		/*
		 * Threads creation
		 */
		boolean[] threadTaken = new boolean[THREADS_NUMBER];
		Generator[] generators = new Generator[THREADS_NUMBER];
		Thread t[] = new Thread[THREADS_NUMBER];

		for(int i = 0; i < THREADS_NUMBER; ++i) {
			generators[i] = new Generator(T, exams, conflictWeight, timeStart, S, p, timeout);
			t[i] = new Thread(generators[i]);
			t[i].start();
			threadTaken[i] = false;
		}

		for(int i = 0; i < THREADS_NUMBER; ++i) {
			/*
			 * For each thread, wait for a solution, then check feasibility
			 */
			int k = 0;
			do {
				k++;
				if(k >= THREADS_NUMBER) {
					k = 0;
				}
			}
			while(t[k].getState() != Thread.State.TERMINATED || threadTaken[k]);
			threadTaken[k] = true;

			try {
				t[k].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			solution = generators[k].getSolution();
			
			if(bestOF == Double.MAX_VALUE) { // First value returned
				bestSolution = solution;
				if(isFeasible()) {
					buildDistancies();
					bestOF = objectiveFunction();
				}
			}
			
			if(isFeasible()) {
				buildDistancies();
				double ofValue = objectiveFunction();
				System.out.format("Solution %d. Objective function value: %.6f. Time %.3f s%n",
						i+1, ofValue, (System.nanoTime()-timeStart)/1000000000.0);
				if(ofValue < bestOF) {
					bestOF = ofValue;
					bestSolution = solution;
				}
			} else {
				System.out.format("Solution %d. Unfeasible: %d conflicts. Time %.3f s%n",
						i+1, totalConflicts(), (System.nanoTime()-timeStart)/1000000000.0);
			}
		}
		
		solution = bestSolution;
	}
	
	/**
	 * This method returns the sum of all conflicts in the partial solution.
	 * @param solution the solution vector
	 * @return number of conflicts
	 */
	public int totalConflicts() {
		int conflicts=0;
		int i, j;
		for(i = 0; i < E; i++) {
			for(j = 0; j < E; j++) {
				if(solution[i] == solution[j] && i != j) {
					if(conflictWeight[i][j] > 0)
						conflicts++;
				}
			}
		}
		
		return conflicts;
	}
	
	/**
	 * This method set the penalties vector, due to the mutual distance
	 * between two exams.
	 */
	private void setPenalties() {
		for(int k = 0; k < PENALTIES; ++k)
			p[k] = (int) Math.pow(BASE_PENALTY, 5-(k+1));
	}
	
	/**
	 * This method builds the matrix of mutual
	 * exclusion due to the conflict between students.
	 */
	private void buildConflictWeight() {
		students.values().stream()
			.map(Student::getExams)
			.forEach(l -> {
					for(Exam e1 : l)
						for(Exam e2 : l) {
							int i = e1.getId(), j = e2.getId();
							if(i < j) {
								conflictWeight[i-1][j-1]++;
								conflictWeight[j-1][i-1]++;
								if(!e1.searchConflictWithExam(e2)) {
									e1.addExamConflict(e2);
								}
								if(!e2.searchConflictWithExam(e1)) {
									e2.addExamConflict(e1);
								}
							}
						}
			});
	}

	/**
	 * This method builds the mutual distances between two exams and check
	 * the possibility of penalties due to the little distance.
	 */
	private void buildDistancies() {
		/* 
		 * Reset the conflict matrix.
		 * Prevent the overlapping of conflicts during multiple threads execution
		 */
		for(int i = 0; i < E; ++i)
			for(int j = 0; j < E; ++j)
				for(int k = 0; k < PENALTIES; ++k)
					conflict[i][j][k] = 0;

		for(int i = 0; i < E; ++i) {
			for(int j = 0; j < E; ++j) {
				if(i < j) {	// Fill only the upper part of the matrix
					int k = Math.abs(solution[i] - solution[j]); // Distance
					if(k <= PENALTIES && k != 0) // Overlapping exams already checked
						conflict[i][j][k-1] = 1;
				}
			}
		}		
	}
	
	/**
	 * This method checks the feasibility of the founded solution.
	 * @return true if the solution is feasible, false otherwise.
	 */
	public boolean isFeasible() {
		for(int i = 0; i < E; ++i) {
			if(solution[i] < 1 || solution[i] > T) // Invalid timeslot
				return false;
			for(int j = 0; j < E; ++j) {
				if(solution[j] < 1 || solution[j] > T)  // Invalid timeslot
					return false;
				if(solution[i] == solution[j] && conflictWeight[i][j] > 0) // Exam in the same timeslot and students in conflict
					return false;
			}
		}
		return true;
	}
	
	/**
	 * This method describes the objective function and resolve it, for the current solution
	 * @return the objective function value.
	 */
	public double objectiveFunction() {
		double obj = 0.0;
		for(int i = 0; i < E; ++i) {
			for(int j = 0; j < E; ++j) {
				double partialSum = 0.0;
				for(int k = 0; k < PENALTIES; ++k) {
					partialSum += p[k] * conflict[i][j][k];
				}
				obj += conflictWeight[i][j] * partialSum;
			}
		}
		return obj/S;
	}
}
