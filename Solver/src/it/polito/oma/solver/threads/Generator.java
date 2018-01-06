package it.polito.oma.solver.threads;

import java.util.*;
import it.polito.oma.solver.*;

public class Generator implements Runnable {

	//OF-related variables
	private final int PENALTIES = 5;
	private final int[] p;
	private final int[][] conflictWeight;
	private int[][][] conflict;
	private final int S;
	
	// Timeslots
	private final int T;
	private TimeSlot[] timeslotsArray,
						timeslotAvailable;
	private TimeSlot timeslotChange;

	// Exams
	private final Map<Integer, Exam> examsInit;
	private Map<Integer, Exam> exams;
	private LinkedHashMap<Integer, Exam> examsNotTaken;
	private int E,
				numberExamsWithoutTimeslot = 0;

	// Controls
	private boolean mutationFlag = false;
	private int minExamWithoutTimeslot = 0,
				minConflicts = Integer.MAX_VALUE,
				minGlobalConflicts = Integer.MAX_VALUE;
	private long control = 0;
	private int timeslotPosition = 0;
	private TabooList tabooList;
	private TabooList optimizationTabooList;
	private boolean setTaboo;

	// Solution vector
	private int[] solution;

	// Other
	private final long timeStart;
	private final long timeout;
	Random rand = new Random(System.nanoTime());

	/**
	 * This method creates a new Generator and sets its parameters.
	 * @param T - number of timeslots available
	 * @param exams - map containing all exams to assign
	 * @param conflictWeight - matrix containing the conflicts among each pair of exams
	 * @param timeStart - instant when the program started, expressed in nanosecond
	 * @param S - number of students
	 * @param p - array of penalties
	 * @param timeout - max time to execute the program, expressed in second
	 */
	public Generator(int T, Map<Integer, Exam> exams, int[][] conflictWeight, long timeStart, int S, int[] p, long timeout) {
		this.T = T;
		this.timeslotAvailable = new TimeSlot[T];
		this.E = exams.size();
		this.examsInit = new TreeMap<Integer, Exam>();
		this.exams = new LinkedHashMap<Integer, Exam>();
		this.conflict = new int[E][E][PENALTIES];
		this.conflictWeight = conflictWeight;
		this.p = p;
		this.S = S;
		this.timeStart = timeStart;
		this.timeout = timeout * 1000000000; // Conversion in nanosecond to avoid calculus in the optimization loop
		
		buildExamMap(exams);
		buildExamsConflicts();
		setTimeSlot();
		
		this.solution = new int[E];
		tabooList = new TabooList(100);
		optimizationTabooList = new TabooList(100);
		
		numberExamsWithoutTimeslot = E;
		minExamWithoutTimeslot = E;
	}

	@Override
	public void run() {
		// Set random parameters
		randomizeExams();
		
		// First of all, assign all the exam in a greedy way
		greedyAssignment();
		
		// Then, for the not taken exams, find the first feasible solution
		feasibleSearch();
		
		// Saving the current solution
		saveSolution();

		if(! isFeasibile()) 
			return;

		// When a feasible solution is found, optimize it
		optimization();
	}

	/**
	 * This method randomizes the exams.
	 */
	private void randomizeExams() {
		int[] examRandom = new int[E];
		
		for (int i = 0; i < E; ++i) {
			examRandom[i] = i + 1;
		}
		
		for (int i = 0; i < E; ++i) {
			int r = rand.nextInt(E - i);
			Exam e = examsInit.get(examRandom[r]);
			exams.put(e.getId(), e);
			examRandom[r] = examRandom[E - 1 - i];
		}
	}

	/**
	 * This method assigns an exam into the first free timeslot, paying attention to
	 * conflicts with other exams.
	 */
	private void greedyAssignment() {
		int examId;
		for (int i = 0; i < T; i++) {
			for (Exam exam : exams.values()) {
				if (!exam.isTaken()) {
					examId = exam.getId();
					if (timeslotsArray[i].isInConflict(examId) == 0) {
						timeslotsArray[i].addExams(exam);
						exam.setTimeSlot(timeslotsArray[i]);
						numberExamsWithoutTimeslot--;
					}
				}
			}
		}
	}

	/**
	 * With this method unassigned exams are assigned to the slot with the smallest number of
	 * conflicts, then the other exams in conflicts, are removed. The method ends when
	 * there are no exam to assign.
	 */
	private void feasibleSearch() {
		examsNotTaken = unsignedExams();
		while (!examsNotTaken.values().isEmpty() && (System.nanoTime() - timeStart) < timeout) {
			if (control == 1000) {
				/* Mutation */
				mutation();
			}

			forcedInsertion();
			updateControl();
			
			control++;

			if (minExamWithoutTimeslot < minGlobalConflicts) {
				minGlobalConflicts = minExamWithoutTimeslot;
			}
		}
	}

	/**
	 * This method save the current solution.
	 */
	private void saveSolution() {
		for (Exam e : exams.values()) {
			solution[e.getId() - 1] = e.getTimeSlot().getId();
		}
	}

	/**
	 * This method creates the map of not taken exams
	 * @return LinkedHashMap of not taken exams.
	 */
	private LinkedHashMap<Integer, Exam> unsignedExams() {
		LinkedHashMap<Integer, Exam> examsNotTaken = new LinkedHashMap<>();
		for (Exam e : exams.values()) {
			if (!e.isTaken()) {
				examsNotTaken.put(e.getId(), e);
			}
		}
		return examsNotTaken;
	}

	/**
	 * For each exam, this method changes the state of an exam, if the
	 * exam is in a timeslot and there are no conflicts with other timeslots.
	 */
	private void mutation() {
		int examId;
		control = 0;
		timeslotPosition = 0;
		for (Exam exam : exams.values()) {
			examId = exam.getId();
			if (exam.isTaken()) {
				for (int i = 0; i < T; i++) {/* Search a free timeslot */
					if (timeslotsArray[i].getNumberOfConflicts(examId) == 0
							&& !tabooList.checkTaboo(timeslotsArray[i], exam)) {
						mutationFlag = true;
						timeslotAvailable[timeslotPosition] = timeslotsArray[i];
						timeslotPosition++;
					}
				}
				if (mutationFlag) {
					mutationFlag = false;
					timeslotChange = timeslotAvailable[rand.nextInt(timeslotPosition)];
					timeslotPosition = 0;
					exam.getTimeSlot().subExams(exam);
					tabooList.setTaboo(exam.getTimeSlot(), exam);
					exam.setTimeSlot(timeslotChange);
					timeslotChange.addExams(exam);
				}

			}
		}
		minExamWithoutTimeslot = Integer.MAX_VALUE;
	}

	/**
	 * This method, for each not taken exam, searches the first timeslot with the
	 * smallest number of conflicts, then it removes all the other exam in conflict with
	 * the added one.
	 */
	private void forcedInsertion() {
		LinkedHashMap<Integer, Exam> examsToAdd = new LinkedHashMap<Integer, Exam>();
		LinkedHashMap<Integer, Exam> listExamsInConflict = new LinkedHashMap<Integer, Exam>();
		int examId, conflictNumber, minimum;

		for (Exam exam : examsNotTaken.values()) {
			examId = exam.getId();
			for (int i = 0; i < T; i++) {/* Search a feasible timeslot */
				conflictNumber = timeslotsArray[i].getNumberOfConflicts(examId);
				if (conflictNumber <= minConflicts && !tabooList.checkTaboo(timeslotsArray[i], exam)) {
					minConflicts = conflictNumber;
					timeslotChange = timeslotsArray[i];
				} else {
					minimum = numberExamsWithoutTimeslot + conflictNumber - 1;
					if (minimum < minGlobalConflicts) {
						minConflicts = conflictNumber;
						timeslotChange = timeslotsArray[i];
					}
				}
			}

			/**
			 * Select all the exam in conflict
			 */
			listExamsInConflict.clear();
			for (Exam examInTimeslotChange : timeslotChange.getExams().values()) {
				if (examInTimeslotChange.searchConflictWithExam(exam)) {
					listExamsInConflict.put(examInTimeslotChange.getId(), examInTimeslotChange);
				}
			}
			/**
			 * Remove the searched exams
			 */
			for (Exam e : listExamsInConflict.values()) {
				int examInConflictId = e.getId();
				timeslotChange.subExams(exams.get(examInConflictId));
				numberExamsWithoutTimeslot++;
				tabooList.setTaboo(timeslotChange, exams.get(examInConflictId));
				examsToAdd.put(examInConflictId, e);
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
	}

	/**
	 * This method updates the control parameter.
	 */
	private void updateControl() {
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
	}

	/**
	 * This method tries to optimize a feasible solution.
	 */
	public void optimization() {
		buildDistancies();
		double objectiveFunction = objectiveFunction();
		double bestObjectiveFunction = objectiveFunction;
		
		int examId;
		boolean slotAvailable = false;
		int count = 0;
		int t1;
		
		//Debug variables
//		double initOf;
//		initOf = objectiveFunction;
		
		System.out.println("Optimization of a feasible solution..." + " Time: " + (System.nanoTime() - timeStart)/1000000000.0);
		control = 0;
		
		// Loop until timeout expires
		while ((System.nanoTime() - timeStart) < timeout) {
			control++;
			if (count > E) { /*Mutation of two timeslots*/
				int timeSlotBest;
				double ofBest = Double.MAX_VALUE;
				int[] tmpSol = new int[E];
				Map<Integer, Exam> examsT1 = new HashMap<>();
				Map<Integer, Exam> examsT2 = new HashMap<>();
				
				setTaboo = false;
				control = 0;
				count = 0;
				
				t1 = rand.nextInt(T);
				timeSlotBest = t1;
				
				//Save previous solution temporarily
				for (int i = 0; i < E; i++) {
					tmpSol[i] = solution[i];
				}
				
				
				/*
				 * For each timeslot, find the best one to make the switch
				 */
				for (int i = 0; i < T; i++) {
					if (t1 != i) {
						examsT1.clear();
						examsT2.clear();
						examsT1.putAll(timeslotsArray[t1].getExams());
						examsT2.putAll(timeslotsArray[i].getExams());
						
						changeExamsTimeslot(examsT1, t1, examsT2, i);
						saveSolution();
						this.buildDistancies();
						objectiveFunction = this.objectiveFunction();
						
						if(objectiveFunction < ofBest) {
							ofBest = objectiveFunction;
							timeSlotBest = i;
						}
						
						changeExamsTimeslot(examsT1, i, examsT2, t1);
					}
				}
				examsT1.clear();
				examsT2.clear();
				examsT1.putAll(timeslotsArray[t1].getExams());
				examsT2.putAll(timeslotsArray[timeSlotBest].getExams());
				
				//If there are a best timeslot, make the change
				if(t1 != timeSlotBest) {
					setTaboo = true;
					changeExamsTimeslot(examsT1, t1, examsT2, timeSlotBest);
				}
				
				//Save new solution
				saveSolution();
				this.buildDistancies();
				objectiveFunction = this.objectiveFunction();
				
				//If new OF is worse than old, return to old solution
				if(bestObjectiveFunction < objectiveFunction) {
					for (int i = 0; i < E; i++) {
						solution[i] = tmpSol[i];
					}
				}
				else {
					bestObjectiveFunction = objectiveFunction;
				}
			}
			
			for (Exam exam : exams.values()) {
				timeslotPosition = 0;
				examId = exam.getId();
				
				//Searching of available slots
				slotAvailable = searchFreeTimeslots(examId, exam);
				
				if (slotAvailable) {
					slotAvailable = false;
					
					double bestDifference = Integer.MAX_VALUE;
					
					//Estimate all the possible difference of the objective function and select the best timeslot
					for (int j = 0; j < timeslotPosition; j++) {
						TimeSlot t = timeslotAvailable[j];
						int timeSlotIdNext = t.getId();
						double differenceOf = estimateOF(timeSlotIdNext, exam);
						
						if (bestDifference >= differenceOf) {
							timeslotChange = t;
							bestDifference = differenceOf;
						}

					}
					double prevDifference;
					int timeSlotId = exam.getTimeSlot().getId();
					
					//Estimates the actual difference
					prevDifference = estimateOF(timeSlotId, exam);
					
					//Calculate the new OF
					objectiveFunction = objectiveFunction + (-prevDifference + bestDifference) / S;
					exam.getTimeSlot().subExams(exam);
					exam.setTimeSlot(timeslotChange);
					
					optimizationTabooList.setTaboo(exam.getTimeSlot(), exam);
					timeslotChange.addExams(exam);
					
					if (objectiveFunction < bestObjectiveFunction) {
						count = 0;
						saveSolution();
						bestObjectiveFunction = objectiveFunction;
					} else {
						count++;
					}

					optimizationTabooList.setTaboo(null, null);
				}
			}

		}

	}

	/**
	 * This method make the switch between exams of two timeslots.
	 * @param examsT1 - map of the exams in first timeslot
	 * @param t1 - id of the first timeslot
	 * @param examsT2 - map of exams in second timeslot
	 * @param t2 - id of the second timeslot
	 */
	private void changeExamsTimeslot(Map<Integer, Exam> examsT1, int t1, Map<Integer, Exam> examsT2, int t2) {
		for (Exam e : examsT1.values()) {
			timeslotsArray[t1].subExams(e);
		}
		
		for (Exam e : examsT2.values()) {
			timeslotsArray[t2].subExams(e);
		}
		
		for (Exam e : examsT2.values()) {
			timeslotsArray[t1].addExams(e);
			if(setTaboo)
				optimizationTabooList.setTaboo(e.getTimeSlot(), e);
			e.setTimeSlot(timeslotsArray[t1]);
		}
		
		for (Exam e : examsT1.values()) {
			timeslotsArray[t2].addExams(e);
			if(setTaboo)
				optimizationTabooList.setTaboo(e.getTimeSlot(), e);
			e.setTimeSlot(timeslotsArray[t2]);
		}
	}
	
	/**
	 * This method searches all the free timeslots for a given exam
	 * @param examId - the id of the exam which look for free timeslot
	 * @param exam - the exam which look for free timeslot
	 * @return true if there are free timeslots, false otherwise.
	 */
	private boolean searchFreeTimeslots(int examId, Exam exam) {
		boolean slotAvailable = false;
		
		for (int i = 0; i < T; i++) {/* Search all the free timeslots */
			if (timeslotsArray[i].getNumberOfConflicts(examId) == 0
					&& !optimizationTabooList.checkTaboo(timeslotsArray[i], exam)) {
				slotAvailable = true;
				timeslotAvailable[timeslotPosition] = timeslotsArray[i];
				timeslotPosition++;
			}
		}
		return slotAvailable;
	}
	
	/**
	 * This method estimates the OF if an exam is moved into a new timeslot
	 * @param timeSlotId - id of the timeslot where the exam may be assigned
	 * @param exam - exam which may be assigned to the timeslot
	 * @return the objective function value.
	 */
	private double estimateOF(int timeSlotId, Exam exam) {
		double value = 0;
		int examId = exam.getId();
		for (int i = 1; i <= 5; i++) {/* calcolo di quanto cambierebbe Of scegliendo questo timeSlot */
			if (timeSlotId - i - 1 >= 0) {
				for (Exam e : timeslotsArray[timeSlotId - i - 1].getExams().values()) {
					if (e.searchConflictWithExam(exam)) {
						if (e.getId() > examId) {
							value = value
									+ conflictWeight[e.getId() - 1][examId - 1] * p[i - 1];
						} else {
							value = value
									+ conflictWeight[examId - 1][e.getId() - 1] * p[i - 1];
						}
					}
				}
			}
			if (timeSlotId + i - 1 < T) {
				for (Exam e : timeslotsArray[timeSlotId + i - 1].getExams().values()) {
					if (e.searchConflictWithExam(exam)) {
						if (e.getId() > examId) {
							value = value
									+ conflictWeight[e.getId() - 1][examId - 1] * p[i - 1];
						} else {
							value = value
									+ conflictWeight[examId - 1][e.getId() - 1] * p[i - 1];
						}
					}
				}
			}
		}
		return value;
	}

	/**
	 * This method builds the mutual distances between two exams and check
	 * the possibility of penalties due to the little distance.
	 */
	private void buildDistancies() {
		/* 
		 * Reset the conflict matrix.
		 * Prevent the overlapping of conflicts during multiple threads execution
		 */
		for(int i = 0; i < E; ++i)
			for(int j = 0; j < E; ++j)
				for(int k = 0; k < PENALTIES; ++k)
					conflict[i][j][k] = 0;

		for(int i = 0; i < E; ++i) {
			for(int j = 0; j < E; ++j) {
				if(i < j) {	// Fill only the upper part of the matrix
					int k = Math.abs(solution[i] - solution[j]); // Distance
					if(k <= PENALTIES && k != 0) // Overlapping exams already checked
						conflict[i][j][k-1] = 1;
				}
			}
		}		
	}
	
	/**
	 * This method checks the feasibility of the founded solution.
	 * @return true if the solution is feasible, false otherwise.
	 */
	public boolean isFeasibile() {
		for(int i = 0; i < E; ++i) {
			if(solution[i] < 1 || solution[i] > T) // Invalid timeslot
				return false;
			for(int j = 0; j < E; ++j) {
				if(solution[j] < 1 || solution[j] > T)  // Invalid timeslot
					return false;
				if(solution[i] == solution[j] && conflictWeight[i][j] > 0) // Exam in the same timeslot and students in conflict
					return false;
			}
		}
		return true;
	}
	
	/**
	 * This method describes the objective function and resolve it, for the current solution
	 * @return the objective function value.
	 */
	public double objectiveFunction() {
		double obj = 0.0;
		for (int i = 0; i < E; ++i) {
			for (int j = 0; j < E; ++j) {
				double partialSum = 0.0;
				for (int k = 0; k < PENALTIES; ++k) {
					partialSum += p[k] * conflict[i][j][k];
				}
				obj += conflictWeight[i][j] * partialSum;
			}
		}
		return obj / S;
	}

	/**
	 * This method returns the best solution generated
	 * @return the best solution generated.
	 */
	public int[] getSolution() {
		return solution;
	}
	
	private void buildExamMap(Map<Integer, Exam> exams) {
		for (Integer id : exams.keySet()) {
			Exam exam = exams.get(id);
			Exam e = new Exam(id, exam.getEnrolledStudents());
			this.examsInit.put(id, e);
		}
	}

	private void buildExamsConflicts() {
		for (Exam e1 : this.examsInit.values()) {
			int id1 = e1.getId() - 1;
			for (Exam e2 : this.examsInit.values()) {
				int id2 = e2.getId() - 1;
				if (conflictWeight[id1][id2] > 0) {
					if (!e1.searchConflictWithExam(e2)) {
						e1.addExamConflict(e2);
					}
					if (!e2.searchConflictWithExam(e1)) {
						e2.addExamConflict(e1);
					}
				}
			}
		}
	}

	private void setTimeSlot() {
		timeslotsArray = new TimeSlot[T];
		for (int i = 0; i < T; i++) {
			timeslotsArray[i] = new TimeSlot(i + 1, E);
		}
	}

}
