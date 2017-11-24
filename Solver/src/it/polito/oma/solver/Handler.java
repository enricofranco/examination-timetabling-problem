package it.polito.oma.solver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Handler {
	private int PENALTIES = 5;
	private int BASE_PENALTY = 2;
	
	private Map<Integer, Exam> exams = new HashMap<>();
	private int examsNumber;
	private Map<Integer, Student> students = new HashMap<>();
	private int studentsNumber;
	private int numberTimeslot;
	
	private int mutualExclusion[][];
	private int penalty[] = new int[PENALTIES];
	private int y[][][];
	private int solution[];	
	
	public void loadInstance(String path) {
		try {
			loadExams(path + ".exm");
			loadStudents(path + ".stu");
			loadTimeslot(path + ".slo");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadExams(String path) throws FileNotFoundException, IOException {
		try(BufferedReader r = new BufferedReader(new FileReader(path))) {
			r.lines()
			.map(l -> l.split(" "))
			.forEach(i -> {
				int id = Integer.valueOf(i[0]);
				Exam e = new Exam(id, Integer.valueOf(i[1]));
				exams.put(id, e);
			});
		}
		examsNumber = exams.size();
	}
	
	private void loadTimeslot(String path) throws FileNotFoundException, IOException {
		try(BufferedReader r = new BufferedReader(new FileReader(path))) {
			r.lines()
			.map(l -> l.split(" "))
			.forEach(i -> {
				numberTimeslot = Integer.valueOf(i[0]);
			});
		}
	}

	private void loadStudents(String path) throws FileNotFoundException, IOException {
		try(BufferedReader r = new BufferedReader(new FileReader(path))) {
			r.lines()
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
		studentsNumber = students.size();
	}
	
	public void initialize() {
		mutualExclusion = new int[examsNumber][examsNumber];
		y = new int[examsNumber][examsNumber][PENALTIES];
		solution = new int[examsNumber];
		setPenalties();
		
//		for(int i = 0; i < PENALTIES; ++i)
//			System.out.println(penalty[i]);
//		System.out.println();
		
		buildMutualExclusionMatrix();
		
//		for(int i = 0; i < examsNumber; ++i) {
//			for(int j = 0; j < examsNumber; ++j)
//				System.out.print(mutualExclusion[i][j] + " ");
//			System.out.println();
//		}
		
		/** Test instance solution */
		solution[0] = 1;
		solution[1] = 3;
		solution[2] = 6;
		solution[3] = 1;
		
		if(checkFeasibility()) {
			buildDistancies();
			System.out.println("Objective function value: " + objectiveFunction());
		} else
			System.out.println("Unfeasible solution");
	}
	
	private void setPenalties() {
		for(int k = 0; k < PENALTIES; ++k)
			penalty[k] = (int) Math.pow(BASE_PENALTY, 5-(k+1));
	}
	
	private void buildMutualExclusionMatrix() {
		students.values().stream()
			.map(Student::getExams)
			.forEach(l -> {
					for(Exam e1 : l)
						for(Exam e2 : l) {
							int i = e1.getId(), j = e2.getId();
							if(i < j)
								mutualExclusion[i-1][j-1]++;
						}
			});
	}

	
	private void buildDistancies() {
		for(int i = 0; i < examsNumber; ++i) {
			for(int j = 0; j < examsNumber; ++j) {
				if(i < j) {	// Fill only the upper part of the matrix
					int k = Math.abs(solution[i] - solution[j]); // Distance
					if(k <= PENALTIES && k != 0) // Overlapping exams already checked
						y[i][j][k-1] = 1;
				}
			}
		}		
	}
	
	public boolean checkFeasibility() {
		for(int i = 0; i < examsNumber; ++i)
			for(int j = 0; j < examsNumber; ++j)
				if(solution[i] == solution[j] && mutualExclusion[i][j] > 0) // Exam in the same timeslot and students in conflict
					return false;
		return true;
	}
	
	public double objectiveFunction() {
		double obj = 0.0;
		for(int i = 0; i < examsNumber; ++i) {
			for(int j = 0; j < examsNumber; ++j) {
				double partialSum = 0.0;
				for(int k = 0; k < PENALTIES; ++k) {
					partialSum += penalty[k] * y[i][j][k];
				}
				obj += mutualExclusion[i][j] * partialSum;
			}
		}
		return obj/studentsNumber;
	}
}
