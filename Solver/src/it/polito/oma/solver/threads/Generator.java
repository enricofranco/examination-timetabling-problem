package it.polito.oma.solver.threads;

import java.util.ArrayList;
import java.util.*;

import java.util.Random;

import it.polito.oma.solver.*;

public class Generator implements Runnable {

	// Timeslots
	private int T;
	private TimeSlot[] timeslotsArray;
	private TimeSlot timeslotChange;

	// Exams
	private Map<Integer, Exam> exams;
	private int E;
	private int numberExamsWithoutTimeslot = 0;

	// Controls
	private boolean mutationFlag = false;
	private int minExamWithoutTimeslot = 0, minConflicts = Integer.MAX_VALUE, minGlobalConflicts = Integer.MAX_VALUE;
	private long control = 0, controlMut = 0;
	private int variabileDiTest = 0, j = 0;
	// Solution vector
	private int[] solution;

	/*
	 * Set variables into the random generator
	 */
	public Generator(int T, Map<Integer, Exam> exams, int[][] conflicts) {
		this.T = T;
		this.E = exams.size();
		this.exams = new TreeMap<Integer, Exam>();
		for (Integer i : exams.keySet()) {
			Exam exam = exams.get(i);
			Exam e = new Exam(i, exam.getEnrolledStudents());
			this.exams.put(i, e);
			// System.out.println(this.exams.get(i).getId() + " " +
			// this.exams.get(i).getEnrolledStudents());
		}
		for (Exam e1 : this.exams.values()) {
			int id1 = e1.getId() - 1;
			for (Exam e2 : this.exams.values()) {
				int id2 = e2.getId() - 1;
				if (conflicts[id1][id2] > 0) {
					if (!e1.searchConflictWithExam(e2)) {
						e1.addExamConflict(e2);
					}
					if (!e2.searchConflictWithExam(e1)) {
						e2.addExamConflict(e1);
					}
				}
			}
		}
		this.solution = new int[E];

		numberExamsWithoutTimeslot = E;
		minExamWithoutTimeslot = E;

		setTimeSlot();
	}

	@Override
	public void run() {
		/**
		 * set random parameters
		 */
		Random rand = new Random();
		int examId;
		int conflicts;
		int indexMutation = 2;
		int min;
		int max;
		int maxExamWithoutTimeslot = 0;
		rand.setSeed(System.nanoTime());

		System.out.println(numberExamsWithoutTimeslot + " first");

		/**
		 * This loop assigns an exam to the first free timeslot, paying attention to
		 * conflicts with other exams. Greedy Loop.
		 */
		for (int i = 0; i < T; i++) {
			for (Exam exam : exams.values()) {
				if (!exam.getTake()) {
					examId = exam.getId();
					if (timeslotsArray[i].isInConflict(examId) == 0) {
						timeslotsArray[i].addExams(exam);
						exam.setTimeSlot(timeslotsArray[i]);
						numberExamsWithoutTimeslot--;
					}
				}
			}
		}

		/**
		 * At the end of this loop, there could be not assigned exams, because of
		 * conflicts. These exams are assigned to the slot with the smallest number of
		 * conflicts, then the other exams in conflicts, are removed. The loop ends when
		 * there are no exam to assign
		 */

		System.out.println(numberExamsWithoutTimeslot + " before");

		while (numberExamsWithoutTimeslot > 0) {

			if (control == 5000) {/* Mutation */
				// control = 0;

				minExamWithoutTimeslot = Integer.MAX_VALUE;

				/**
				 * For each exam, this loop probably (1/2) change the state of an exam, if the
				 * exam is in a timeslot and there are no conflicts with other timeslots
				 */
				for (Exam exam : exams.values()) {
					examId = exam.getId();
					if (exam.getTake()) {
						for (int i = 0; i < T; i++) {/* Search a free timeslot */
							if (timeslotsArray[i].getNumberOfConflicts(examId) == 0
									&& !exam.checkTaboo(timeslotsArray[i])) {
								if (rand.nextInt(indexMutation) == 0) {
									mutationFlag = true;
									timeslotChange = timeslotsArray[i];
								}
							}
						}
						if (mutationFlag) {
							mutationFlag = false;
							exam.getTimeSlot().subExams(exam);
							exam.setTaboo(exam.getTimeSlot());
							exam.setTimeSlot(timeslotChange);
							timeslotChange.addExams(exam);
							for (Exam e : exams.values()) {
								if (e.getId() != exam.getId()) {
									e.setTaboo(null);
								}
							}
						}
					}
				}
				maxExamWithoutTimeslot = 0;
				minExamWithoutTimeslot = Integer.MAX_VALUE;
			}

			/**
			 * for each exam, if the exam isn't taken, search the first timeslot with the
			 * smallest number of conflicts, then remove all the other exam in conflict with
			 * the added exam
			 */
			for (Exam exam : exams.values()) {
				examId = exam.getId();
				if (!exam.getTake()) {
					for (int i = 0; i < T; i++) {/* Search a feasible timeslot */
						conflicts = timeslotsArray[i].getNumberOfConflicts(examId);
						if (conflicts <= minConflicts && !exam.checkTaboo(timeslotsArray[i])) {
							minConflicts = conflicts;
							timeslotChange = timeslotsArray[i];
						}
						int calcoloMinimo;
						calcoloMinimo = numberExamsWithoutTimeslot + conflicts - 1;
						if (calcoloMinimo < minGlobalConflicts) {
							minConflicts = conflicts;
							timeslotChange = timeslotsArray[i];
							System.out.println("oiwegnoewignweogingewowepgmpwe");
//							System.out.println("oiwegnoewignweogingewowepgmpwe");
//							System.out.println("oiwegnoewignweogingewowepgmpwe");
//							System.out.println("oiwegnoewignweogingewowepgmpwe");
//							System.out.println("oiwegnoewignweogingewowepgmpwe");
//							System.out.println("oiwegnoewignweogingewowepgmpweoiwegnoewignweogingewowepgmpwe");
//							System.out.println("oiwegnoewignweogingewowepgmpwe");
//							System.out.println("oiwegnoewignweogingewowepgmpwe");
//							System.out.println("oiwegnoewignweogingewowepgmpwe");
//							System.out.println("oiwegnoewignweogingewowepgmpwe");
//							System.out.println("oiwegnoewignweogingewowepgmpwe");
						}
					}

					ArrayList<Integer> listExamWithoutTimeslot = new ArrayList<>();
					/**
					 * Select all the exam in conflict
					 */
					for (Exam examInTimeslotChange : timeslotChange.getExams().values()) {
						if (examInTimeslotChange.searchConflictWithExam(exam)) {
							listExamWithoutTimeslot.add(examInTimeslotChange.getId());
						}
					}
					/**
					 * Remove the searched exams
					 */
					for (int i = 0; i < listExamWithoutTimeslot.size(); i++) {
						int examIdWithoutTimeslot = listExamWithoutTimeslot.get(i);
						timeslotChange.subExams(exams.get(examIdWithoutTimeslot));
						numberExamsWithoutTimeslot++;
						exams.get(examIdWithoutTimeslot).setTaboo(timeslotChange);
						for (Exam e : exams.values()) {
							if (e.getId() != exam.getId()) {
								e.setTaboo(null);
							}
						}
					}
					timeslotChange.addExams(exam);
					exam.setTimeSlot(timeslotChange);
					numberExamsWithoutTimeslot--;
					minConflicts = Integer.MAX_VALUE;
				}
			}

			if (numberExamsWithoutTimeslot < minExamWithoutTimeslot) {
				minExamWithoutTimeslot = numberExamsWithoutTimeslot;
				control = 0;
			} else {
				if (numberExamsWithoutTimeslot > maxExamWithoutTimeslot) {
					maxExamWithoutTimeslot = numberExamsWithoutTimeslot;
					control=0;
				} else
					control++;
			}

			if (minExamWithoutTimeslot < minGlobalConflicts) {
				minGlobalConflicts = minExamWithoutTimeslot;
				control = 0;
			}

//			System.out.println(numberExamsWithoutTimeslot + " control " + control + " min " + minExamWithoutTimeslot
//					+ " minGlobal " + minGlobalConflicts + " examMut " + variabileDiTest);
//
//			System.out.println();
		}

		for (Exam e : exams.values()) {
			solution[e.getId() - 1] = e.getTimeSlot().getId();
		}
	}

	public int[] getSolution() {
		return solution;
	}

	private void setTimeSlot() {
		timeslotsArray = new TimeSlot[T];
		for (int i = 0; i < T; i++) {
			timeslotsArray[i] = new TimeSlot(i + 1, E);
		}
	}

}
