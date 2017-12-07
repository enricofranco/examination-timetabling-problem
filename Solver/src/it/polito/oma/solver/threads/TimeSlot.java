package it.polito.oma.solver.threads;

import java.util.*;

import it.polito.oma.solver.Exam;

public class TimeSlot {
	private int id;
	private int nConf[];
	private Map<Integer, Exam> exams=new HashMap<>();
	
	public TimeSlot(int id, int nExams) {
		this.id=id;
		nConf=new int[nExams];
	}
	
	public void addConf(int i) {
		nConf[i-1]++;
	}
	
	public void subConf(int i) {
		nConf[i-1]--;
	}
	
	public int getNConf(int i) {
		return nConf[i-1];
	}
	
	public int isConf(int i) {
		if(nConf[i-1]==0) {
			return 0;
		}
		else {
			return 1;
		}
	}
	
	public void addExams(int i, Exam e) {
		exams.put(i, e);
		e.setTake();
		for(Exam ex:e.getExamConf()) {
			this.addConf(ex.getId());
		}
	}
	
	public int getId() {
		return id;
	}
	
	public Map<Integer, Exam> getExams() {
		return exams;
	}
	
	public void subExams(int i, Exam e) {
		exams.remove(i);
		e.setNoTake();
		for(Exam ex:e.getExamConf()) {
			this.subConf(ex.getId());
		}
	}
	
}
