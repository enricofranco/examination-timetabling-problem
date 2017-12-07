package it.polito.oma.solver;

import java.util.*;

import it.polito.oma.solver.threads.TimeSlot;

public class Exam {
	private int id;
	private int enrolledStudents;
	private int take=0;
	int nBuffer=5;
	private TimeSlot ts;
	private List<Exam> examConf=new ArrayList<Exam>();
	private TimeSlot[] tabooSlot=new TimeSlot[nBuffer];
	int indTaboo=0;
	int i=0;
	
	
	public Exam(int id, int enrolledStudents) {
		this.id = id;
		this.enrolledStudents = enrolledStudents;
		indTaboo=0;
		take=0;
	}

	public int getId() {
		return id;
	}

	public int getEnrolledStudents() {
		return enrolledStudents;
	}
	
	public void setTake() {
		take=1;
	}
	
	public int getTake() {
		return take;
	}
	
	public int isExamConf(Exam e) {
		if(examConf.contains(e)) {
			return 1;
		}
		else {
			return 0;
		}
	}
	
	public void addExamConf(Exam e) {
		examConf.add(e);
	}
	
	public List<Exam> getExamConf() {
		return examConf;
	}
	
	public void setTimeSlot(TimeSlot ts) {
		this.ts=ts;
	}
	
	public TimeSlot getTimeSlot() {
		return ts;
	}
	
	public void setNoTake() {
		take=0;
	}
	
	public void setTaboo(TimeSlot ts) {
		tabooSlot[indTaboo]=ts;
		indTaboo++;
		if(indTaboo>=nBuffer) {
			indTaboo=0;
		}
	}
	
	public int checkTaboo(TimeSlot ts) {
		for(i=0; i<nBuffer; i++) {
			if(tabooSlot[i]!=null) {
				if(tabooSlot[i].getId()==ts.getId()) {
					return 1;
				}
			}	
		}
		return 0;
	}
}
