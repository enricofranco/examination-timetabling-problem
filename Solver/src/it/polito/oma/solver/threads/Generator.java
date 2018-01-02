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
	private TimeSlot[] timeslotAvaible;
	private int PENALTIES = 5;

	// Exams
	private Map<Integer, Exam> examsInit;
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
	private int ti = 0;
	private TabooList tabooList;
	private long time;
	private int S;
	private int[][] conflicts;
	private int[] p;
	private int[][][] con;
	
	//Other
	Random rand = new Random();

	/*
	 * Set variables into the random generator
	 */
	public Generator(int T, Map<Integer, Exam> exams, int[][] conflicts, long time, int S, int[] p) {
		this.p = p;
		this.S = S;
		this.time = time;
		this.T = T;
		this.E = exams.size();
		this.con = new int[E][E][PENALTIES];
		this.examsInit = new TreeMap<Integer, Exam>();
		this.timeslotAvaible = new TimeSlot[T];
		this.conflicts = conflicts;
		for (Integer i : exams.keySet()) {
			Exam exam = exams.get(i);
			Exam e = new Exam(i, exam.getEnrolledStudents());
			this.examsInit.put(i, e);
			// System.out.println(this.exams.get(i).getId() + " " +
			// this.exams.get(i).getEnrolledStudents());
		}
		for (Exam e1 : this.examsInit.values()) {
			int id1 = e1.getId() - 1;
			for (Exam e2 : this.examsInit.values()) {
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
		this.exams = new LinkedHashMap<Integer, Exam>();
		numberExamsWithoutTimeslot = E;
		minExamWithoutTimeslot = E;
		tabooList = new TabooList(100);
		setTimeSlot();
	}

	@Override
	public void run() {
		/**
		 * set random parameters
		 */

		int[] examRandom = new int[E];
		int examId;
		int conflicts;
		int indexMutation = 2;
		int min;
		int max;
		int maxExamWithoutTimeslot = 0;
		rand.setSeed(System.nanoTime());

		for (int i = 0; i < E; ++i) {
			examRandom[i] = i + 1;
		}
		for (int i = 0; i < E; ++i) {
			int r = rand.nextInt(E - i);
			Exam e = examsInit.get(examRandom[r]);
			exams.put(e.getId(), e);
			examRandom[r] = examRandom[E - 1 - i];
		}
		// for(Exam e:exams.values()) {
		// System.out.print(e.getId()+" ");
		// }
		//// System.out.println("");
		// System.out.println(numberExamsWithoutTimeslot + " first");

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

		LinkedHashMap<Integer, Exam> examsNotTaken = new LinkedHashMap<Integer, Exam>();

		for (Exam e : exams.values()) {
			if (!e.getTake()) {
				examsNotTaken.put(e.getId(), e);
			}
		}
		// System.out.println(numberExamsWithoutTimeslot + " before");

		while (!examsNotTaken.values().isEmpty()) {

			if (control == 1000) {/* Mutation */
				control = 0;

				/**
				 * For each exam, this loop probably (1/2) change the state of an exam, if the
				 * exam is in a timeslot and there are no conflicts with other timeslots
				 */
				ti = 0;
				for (Exam exam : exams.values()) {
					examId = exam.getId();
					if (exam.getTake()) {
						for (int i = 0; i < T; i++) {/* Search a free timeslot */
							if (timeslotsArray[i].getNumberOfConflicts(examId) == 0
									&& !tabooList.checkTaboo(timeslotsArray[i], exam)) {
								mutationFlag = true;
								timeslotAvaible[ti] = timeslotsArray[i];
								ti++;
							}
						}
						if (mutationFlag && ti > 1) {
							mutationFlag = false;
							timeslotChange = timeslotAvaible[rand.nextInt(ti)];
							ti = 0;
							exam.getTimeSlot().subExams(exam);
							tabooList.setTaboo(exam.getTimeSlot(), exam);
							exam.setTimeSlot(timeslotChange);
							timeslotChange.addExams(exam);
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
			LinkedHashMap<Integer, Exam> examsToAdd = new LinkedHashMap<Integer, Exam>();

			for (Exam exam : examsNotTaken.values()) {
				examId = exam.getId();
				for (int i = 0; i < T; i++) {/* Search a feasible timeslot */
					conflicts = timeslotsArray[i].getNumberOfConflicts(examId);
					if (conflicts <= minConflicts && !tabooList.checkTaboo(timeslotsArray[i], exam)) {
						minConflicts = conflicts;
						timeslotChange = timeslotsArray[i];
					} else {
						int calcoloMinimo;
						calcoloMinimo = numberExamsWithoutTimeslot + conflicts - 1;
						if (calcoloMinimo < minGlobalConflicts) {
							minConflicts = conflicts;
							timeslotChange = timeslotsArray[i];
							// System.out.println("oiwegnoewignweogingewowepgmpwe");
						}
					}
				}

				LinkedHashMap<Integer, Exam> listExamWithoutTimeslot = new LinkedHashMap<Integer, Exam>();
				/**
				 * Select all the exam in conflict
				 */
				for (Exam examInTimeslotChange : timeslotChange.getExams().values()) {
					if (examInTimeslotChange.searchConflictWithExam(exam)) {
						listExamWithoutTimeslot.put(examInTimeslotChange.getId(), examInTimeslotChange);
					}
				}
				/**
				 * Remove the searched exams
				 */
				for (Exam e : listExamWithoutTimeslot.values()) {
					int examIdWithoutTimeslot = e.getId();
					timeslotChange.subExams(exams.get(examIdWithoutTimeslot));
					numberExamsWithoutTimeslot++;
					tabooList.setTaboo(timeslotChange, exams.get(examIdWithoutTimeslot));
					examsToAdd.put(examIdWithoutTimeslot, e);
				}
				timeslotChange.addExams(exam);
				exam.setTimeSlot(timeslotChange);
				numberExamsWithoutTimeslot--;
				minConflicts = Integer.MAX_VALUE;
			}
			examsNotTaken.clear();
			for (Exam e : examsToAdd.values()) {
				examsNotTaken.put(e.getId(), e);
			}
			if (numberExamsWithoutTimeslot < minExamWithoutTimeslot) {
				minExamWithoutTimeslot = numberExamsWithoutTimeslot;
				control = 0;
				if (minExamWithoutTimeslot < minGlobalConflicts + 3) {
					control = -1000;
				}
				if (minExamWithoutTimeslot <= 3) {
					control = -10000;
				}
				if (minExamWithoutTimeslot <= 1) {
					control = -20000;
				}
			}
			// else {
			// if (numberExamsWithoutTimeslot > maxExamWithoutTimeslot) {
			// maxExamWithoutTimeslot = numberExamsWithoutTimeslot;
			// control=0;
			// } else
			control++;
			// }

			if (minExamWithoutTimeslot < minGlobalConflicts) {
				minGlobalConflicts = minExamWithoutTimeslot;
				// control = 0;
			}

			// System.out.println(numberExamsWithoutTimeslot + " control " + control + " min
			// " + minExamWithoutTimeslot
			// + " minGlobal " + minGlobalConflicts + " examMut " + variabileDiTest);
			//
			// System.out.println();
		}
		for (Exam e : exams.values()) {
			solution[e.getId() - 1] = e.getTimeSlot().getId();
		}

		this.optimization();
	}

	public void optimization() {

		this.buildDistancies();
		int[] initSol = new int[E];
		double initOf;
		/**
		 * ho utilizzato il codice precedente per avere una OF di partenza,
		 * con=conflicts conflicts=conflictsWeight ho fatto questi cambi per mantenere
		 * la coerenza con il codice precedente di questa classe
		 */
		TabooList tabooListOpt = new TabooList(1000);
		double objectiveFunction = this.objectiveFunction();
		double bestObjectiveFunction = objectiveFunction;
		initOf = objectiveFunction;
		/**
		 * soluzione locale confrontata con la migliore (possibile non utilizzata)
		 */
		int[] localSolution = new int[E];
		/**
		 * itera fino a quando hai tempo (50 sec)
		 */
		int examId;
		boolean slotAvaible = false;
		double bestConflict = Integer.MAX_VALUE;
		int control = 0;
		int count = 0;
		int t1, t2;
		TimeSlot temp;
		
		while (((float) System.nanoTime() - time) / 1000000000 < 50) {
			control++;
			if(count > 20000) {
				count = 0;
				tabooListOpt.cleanTabooList();
				t1 = rand.nextInt(T);
				do {
					t2 = rand.nextInt(T);
				}while(t2 == t1);
			
				temp = new TimeSlot(t2, E);
				for(Exam e2:timeslotsArray[t2].getExams().values()) {
					temp.addExams(e2);
				}
				timeslotsArray[t2].getExams().clear();
				for(Exam e1:timeslotsArray[t1].getExams().values()) {
					e1.setTimeSlot(timeslotsArray[t2]);
					timeslotsArray[t2].addExams(e1);
				}
				timeslotsArray[t1].getExams().clear();
				for(Exam e2:temp.getExams().values()) {
					e2.setTimeSlot(timeslotsArray[t1]);
					timeslotsArray[t1].addExams(e2);
				}
			}
			for (Exam exam : exams.values()) {
				ti = 0;
				bestConflict = Integer.MAX_VALUE;
				examId = exam.getId();
				for (int i = 0; i < T; i++) {/* Search a free timeslot */
					if (timeslotsArray[i].getNumberOfConflicts(examId) == 0
							&& !tabooListOpt.checkTaboo(timeslotsArray[i], exam)) {
						slotAvaible = true;
						timeslotAvaible[ti] = timeslotsArray[i];
						ti++;
					}
				}
				if (slotAvaible) {/* decido dove spostare in base all'abbassarsi dell'Of */
					slotAvaible = false;
					double bestDifference = Integer.MAX_VALUE;
					for (int j = 0; j < ti; j++) {
						TimeSlot t = timeslotAvaible[j];
						int timeSlotIdNext = t.getId();
						double differenceOf = 0;
						for (int i = 1; i <= 5; i++) {/*
														 * calcolo di quanto cambierebbe Of scegliendo questo timeSlot
														 */
							if (timeSlotIdNext - i - 1 >= 0) {
								for (Exam e : timeslotsArray[timeSlotIdNext - i - 1].getExams().values()) {
									if (e.searchConflictWithExam(exam)) {
										if (e.getId() > exam.getId()) {
											differenceOf = differenceOf
													+ conflicts[e.getId() - 1][exam.getId() - 1] * p[i - 1];
										} else {
											differenceOf = differenceOf
													+ conflicts[exam.getId() - 1][e.getId() - 1] * p[i - 1];
										}
									}
								}
							}
							if (timeSlotIdNext + i - 1 < T) {
								for (Exam e : timeslotsArray[timeSlotIdNext + i - 1].getExams().values()) {
									if (e.searchConflictWithExam(exam)) {
										if (e.getId() > exam.getId()) {
											differenceOf = differenceOf
													+ conflicts[e.getId() - 1][exam.getId() - 1] * p[i - 1];
										} else {
											differenceOf = differenceOf
													+ conflicts[exam.getId() - 1][e.getId() - 1] * p[i - 1];
										}
									}
								}
							}
						}
						if (bestDifference >= differenceOf) {
							timeslotChange = t;
							bestDifference = differenceOf;
						}

					}
					double preDifference = 0;
					int timeSlotId = exam.getTimeSlot().getId();
					for (int i = 1; i <= 5; i++) {/* calcolo di quanto dell'of dovuto al timeSlot precedente */
						if (timeSlotId - i - 1 >= 0) {
							for (Exam e : timeslotsArray[timeSlotId - i - 1].getExams().values()) {
								if (e.searchConflictWithExam(exam)) {
									if (e.getId() < exam.getId()) {
										preDifference += conflicts[e.getId() - 1][exam.getId() - 1] * p[i - 1];
									} else {
										preDifference += conflicts[exam.getId() - 1][e.getId() - 1] * p[i - 1];
									}
								}
							}
						}
						if (timeSlotId + i - 1 < T) {
							for (Exam e : timeslotsArray[timeSlotId + i - 1].getExams().values()) {
								if (e.searchConflictWithExam(exam)) {
									if (e.getId() < exam.getId()) {
										preDifference += conflicts[e.getId() - 1][exam.getId() - 1] * p[i - 1];
									} else {
										preDifference += conflicts[exam.getId() - 1][e.getId() - 1] * p[i - 1];
									}
								}
							}

						}
					}
					objectiveFunction = objectiveFunction + (-preDifference + bestDifference) / S;
					exam.getTimeSlot().subExams(exam);
					tabooListOpt.setTaboo(exam.getTimeSlot(), exam);
					exam.setTimeSlot(timeslotChange);
					timeslotChange.addExams(exam);
					if (objectiveFunction < bestObjectiveFunction) {
						count = 0;
						for (Exam e : exams.values()) {
							solution[e.getId() - 1] = e.getTimeSlot().getId();
						}
						bestObjectiveFunction = objectiveFunction;
					}
					else {
						count++;
					}
					System.out.println(" of " + objectiveFunction + " bof" + " " + bestObjectiveFunction + " initSol "
							+ initOf + " control " + control + " count " + count);
					// buildDistancies();
					// objectiveFunction = objectiveFunction();
					tabooListOpt.setTaboo(null, null);
				}
			}

		}

	}

	private void buildDistancies() {
		/*
		 * Reset the conflict matrix. Prevent the overlapping of conflicts during
		 * multiple threads execution
		 */
		for (int i = 0; i < E; i++)
			for (int j = 0; j < E; j++)
				for (int k = 0; k < PENALTIES; k++) {
					// System.out.println("i "+i+" j "+j+" k "+k);
					con[i][j][k] = 0;
				}

		for (int i = 0; i < E; ++i) {
			for (int j = 0; j < E; ++j) {
				if (i < j) { // Fill only the upper part of the matrix
					int k = Math.abs(solution[i] - solution[j]); // Distance
					if (k <= PENALTIES && k != 0) // Overlapping exams already checked
						con[i][j][k - 1] = 1;
				}
			}
		}
	}

	public double objectiveFunction() {
		double obj = 0.0;
		for (int i = 0; i < E; ++i) {
			for (int j = 0; j < E; ++j) {
				double partialSum = 0.0;
				for (int k = 0; k < PENALTIES; ++k) {
					partialSum += p[k] * con[i][j][k];
				}
				obj += conflicts[i][j] * partialSum;
			}
		}
		return obj / S;
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
