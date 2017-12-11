package it.polito.oma.solver.threads;

import java.util.*;

import it.polito.oma.solver.Exam;

public class TimeSlot {
	
	private int id;
	private int numberOfConflicts[];
	private Map<Integer, Exam> exams = new HashMap<>();
	
	public TimeSlot(int id, int nExams) {
		this.id = id;
		numberOfConflicts = new int[nExams];
	}
	
	public void addConflict(int position) {
		numberOfConflicts[position-1]++;
	}
	
	public void subConflict(int position) {
		numberOfConflicts[position-1]--;
	}
	
	public int getNumberOfConflicts(int position) {
		return numberOfConflicts[position-1];
	}
	
	public int isInConflict(int position) {
		if(numberOfConflicts[position-1]==0) {
			return 0;
		}
		return 1;
	}
	
	public void addExams(Exam exam) {
		exams.put(exam.getId(), exam);
		exam.setTake();
		for(Exam examInConflict:exam.getExamConflict()) {
			this.addConflict(examInConflict.getId());
		}
	}

	public void subExams(Exam exam) {
		exams.remove(exam.getId());
		exam.setNoTake();
		for(Exam examInConflict:exam.getExamConflict()) {
			this.subConflict(examInConflict.getId());
		}
	}
	
	public int getId() {
		return id;
	}
	
	public Map<Integer, Exam> getExams() {
		return exams;
	}
	
}
