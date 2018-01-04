package it.polito.oma.solver;

import java.util.*;
import it.polito.oma.solver.threads.TimeSlot;

public class Exam {
	//Parameters
	private int id;
	private int enrolledStudents;
	
	private boolean taken;
	private TimeSlot timeSlot;
	private List<Exam> examsInConflict = new ArrayList<Exam>();
	private boolean flagIsChanged = false;
	
	public Exam(int id, int enrolledStudents) {
		this.id = id;
		this.enrolledStudents = enrolledStudents;
		taken = false;
	}

	public int getId() {
		return id;
	}

	public int getEnrolledStudents() {
		return enrolledStudents;
	}
	
	public void setTaken() {
		taken = true;
	}
	
	public void setUntaken() {
		taken = false;
	}
	
	public boolean isTaken() {
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
		this.timeSlot = ts;
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
}
