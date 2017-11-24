package it.polito.oma.solver;

import java.util.ArrayList;
import java.util.List;

public class Student {
	private int id;
	private List<Exam> exams = new ArrayList<>();
	
	public Student(int id) {
		this.id = id;
	}
	
	public void addExam(Exam e) {
		exams.add(e);
	}
	
	public int getId() {
		return id;
	}

	public List<Exam> getExams() {
		return exams;
	}
}
