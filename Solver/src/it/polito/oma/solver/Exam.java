package it.polito.oma.solver;

import java.util.*;

import it.polito.oma.solver.threads.TimeSlot;

public class Exam {
	private int id, i;
	private int enrolledStudents;
	private boolean taken;
	private int tabooBuffer = 100;
	private TimeSlot timeSlot;
	private List<Exam> examsInConflict = new ArrayList<Exam>();
	private TimeSlot[] tabooSlot = new TimeSlot[tabooBuffer];
	private int tabooPosition = 0;
	private boolean flagIsChanged = false;
	private TimeSlot tsPrec;
	private boolean presoPrec=false;
	
	
	public Exam(int id, int enrolledStudents) {
		this.id = id;
		this.enrolledStudents = enrolledStudents;
		tabooPosition=0;
		taken = false;
	}

	public int getId() {
		return id;
	}

	public int getEnrolledStudents() {
		return enrolledStudents;
	}
	
	public void setTake() {
		taken = true;
	}
	
	public void setNoTake() {
		taken = false;
	}
	
	public boolean getTake() {
		return taken;
	}
	
	public boolean searchConflictWithExam(Exam e) {
		if(examsInConflict.contains(e)) {
			return true;
		}
		return false;
	}
	
	public void addExamConflict(Exam e) {
		examsInConflict.add(e);
	}
	
	public List<Exam> getExamConflict() {
		return examsInConflict;
	}
	
	public void setTimeSlot(TimeSlot ts) {
		this.timeSlot=ts;
	}
	
	public void setTimeSlotPrec(TimeSlot ts) {
		this.tsPrec=ts;
	}
	
	public TimeSlot getTimeSlotPrec() {
		return tsPrec;
	}
	
	public TimeSlot getTimeSlot() {
		return timeSlot;
	}
	
	public void setChange() {
		flagIsChanged = true;
	}
	
	public void setNoChange() {
		flagIsChanged = false;
	}
	
	public boolean getChange() {
		return flagIsChanged;
	}
	
	public void setPresoPrec() {
		presoPrec=true;
	}
	
	public void setNoPresoPrec() {
		presoPrec=false;
	}
	
	public boolean getPresoPrec() {
		return presoPrec;
	}
}
