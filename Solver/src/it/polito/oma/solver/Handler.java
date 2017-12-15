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
	private int PENALTIES = 5;
	private int BASE_PENALTY = 2;
	private String GROUP = "OMAAL_group09.sol";
	private int THREADS_NUMBER = 1;
	
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
	private int[] etSol;
	
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
	 * This method writes a solution into a file
	 * @param instanceName The name of the running instance
	 * @throws IOException
	 */
	public void writeSolution(String instanceName) throws IOException {
		String fileName = instanceName + GROUP;
		if(! checkFeasibility()) return; // If the solution is not feasible, do not write the file
		try(BufferedWriter r = new BufferedWriter(new FileWriter(fileName))) {
			for(int i = 0; i < E; ++i) {
				String s = String.format("%04d%d\n", i+1, etSol[i]);
				r.write(s);
			}
		}
	}
	
	/**
	 * This method initializes the vectors used by the objective function.
	 */
	public void initialize() {
		conflictWeight = new int[E][E];
		conflict = new int[E][E][PENALTIES];
		etSol = new int[E];
		setPenalties();
		buildConflictWeight();

		/*
		 * Threads creation
		 */
		Generator[] generators = new Generator[THREADS_NUMBER];
		Thread t[] = new Thread[THREADS_NUMBER];
		for(int i = 0; i < THREADS_NUMBER; ++i) {
			generators[i] = new Generator(T, exams, students);
			t[i] = new Thread(generators[i]);
			t[i].start();
		}

		for(int i = 0; i < THREADS_NUMBER; ++i) {
			/*
			 * For each thread, wait for a solution, then check feasibility
			 */
			while(t[i].getState() != Thread.State.TERMINATED);
			etSol = generators[i].getSolution();
			if(checkFeasibility()) {
				buildDistancies();
				System.out.println("Objective function value: " + objectiveFunction());
			} else {
				System.out.println("Unfeasible solution");
			}
		}
	}
	
	/**
	 * This method sets the penalties vector, due to the mutual distance
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
							if(i < j)
								conflictWeight[i-1][j-1]++;
						}
			});
	}

	/**
	 * This method builds the mutual distances between two exams and check
	 * the possibility of penalties due to the little distance.
	 */
	private void buildDistancies() {
		for(int i = 0; i < E; ++i) {
			for(int j = 0; j < E; ++j) {
				if(i < j) {	// Fill only the upper part of the matrix
					int k = Math.abs(etSol[i] - etSol[j]); // Distance
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
	public boolean checkFeasibility() {
		for(int i = 0; i < E; ++i) {
			if(etSol[i] < 1 || etSol[i] > T) // Invalid timeslot
				return false;
			for(int j = 0; j < E; ++j) {
				if(etSol[j] < 1 || etSol[j] > T)  // Invalid timeslot
					return false;
				if(etSol[i] == etSol[j] && conflictWeight[i][j] > 0) // Exam in the same timeslot and students in conflict
					return false;
			}
		}
		return true;
	}
	
	/**
	 * This method describes the objective function and resolve it, for the current solution
	 * @return result of the objective function.
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
