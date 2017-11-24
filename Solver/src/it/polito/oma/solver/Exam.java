package it.polito.oma.solver;

public class Exam {
	private int id;
	private int enrolledStudents;
	
	public Exam(int id, int enrolledStudents) {
		this.id = id;
		this.enrolledStudents = enrolledStudents;
	}

	public int getId() {
		return id;
	}

	public int getEnrolledStudents() {
		return enrolledStudents;
	}
}
