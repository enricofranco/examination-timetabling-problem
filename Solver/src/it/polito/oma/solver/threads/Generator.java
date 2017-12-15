package it.polito.oma.solver.threads;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Generator implements Runnable  {

	private int T;
	private int E;
	private int totExm;
	private int[][] conflicts;
	private int[] isSelected;
	private Random rand = new Random();

	List<ArrayList<Integer>> buckets;
	List<ArrayList<Integer>> timeslots;

	private int[] solution;

	/*
	 * Set variables into the random generator
	 */
	public Generator(int T, int E, int[][] conflicts) {
		this.T = T;
		this.E = E;
		this.totExm = E;
		this.conflicts = conflicts;
		this.timeslots = new ArrayList<>();
		this.solution = new int[E];
		this.rand.setSeed(System.nanoTime());
		this.isSelected = new int[E];
		
	}
	
	@Override
	public void run() {
		buckets = setBuckets();
//		for(ArrayList<Integer> a:buckets) {
//			for(Integer I:a) {
//				System.out.print(I + " ");
//			}
//			System.out.println();
//		}
//		
//		System.out.println(buckets.size());
//		
//		System.out.println(buckets.get(0).size());
		
		try {
			writeLog();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<Integer> l;
		
		for(int i = 0; i < T; i++) {
			l = new ArrayList<>();
			timeslots.add(l);
		}
		solver(0, 0, buckets.get(0).size());
		solver(0, buckets.get(0).size(), T);
		for(ArrayList<Integer> a:timeslots) {
			for(Integer I:a) {
				System.out.print(I + " ");
			}
			System.out.println();
		}
		
		System.out.println(totExm);
	}
	
	private List<ArrayList<Integer>> setBuckets() {
		Map<Integer, ArrayList<Integer>> bucket = new TreeMap<>();
		ArrayList<Integer> list;
		boolean selected;
		
		for(int i = 0; i <E; i++) {
			if(!bucket.containsKey(i)) {
				list = new ArrayList<Integer>();
				list.add(i);
				bucket.put(i, list);
				
			}
			else {
				list = bucket.get(i);
				list.add(i);
			}
			for(int j = 0; j < E; j++) {
				if(conflicts[i][j] > 0 || conflicts[j][i] > 0) {
					if(!bucket.containsKey(j)) {
						list = new ArrayList<Integer>();
						list.add(i);
						bucket.put(j, list);
					}
					else {
						list = bucket.get(j);
						selected = true;
						for(Integer exam:list) {
							if(conflicts[exam][i] == 0 && conflicts[i][exam] == 0 && i != exam) {
								selected = false;
								break;
							}
						}
						if(selected) {
							list.add(i);
						}
					}
				}
			}
		}
		
		return bucket.values().stream()
				.sorted((s1, s2) -> Integer.compare(s2.size(), s1.size()))
				.collect(Collectors.toList());
		
	}

	private void solver(List<Integer> timeSlot, int bucketPosition, int position) {
		if(bucketPosition >= buckets.size()) {
			System.out.println("Catena finita");
			return;
		}
		
		boolean select = false;
		
		ArrayList<Integer> list = buckets.get(bucketPosition);
		for(Integer i:list) {
			if(isSelected[i] == 0) {
				select = true;
				for(Integer j: timeSlot) {
					if(conflicts[j][i] > 0 || conflicts[i][j] > 0) {
						select = false;
						break;
					}
				}
				if(select) {
					timeSlot.add(i);
					isSelected[i] = 1;
					totExm--;
					solver(timeSlot, bucketPosition+1, position+1);
					break;
				}
			}
			
			
		}
		solver(timeSlot, bucketPosition + 1, position);
	}
	
	private void solver() {
		for(ArrayList<Integer> bucket:buckets) {
			for(Integer exam:bucket) {
				if(isSelected[exam] == 0) {
					for(ArrayList<Integer> timeslot:timeslots) {
						boolean selected = true;
						for(Integer e2:timeslot) {
							if(conflicts[exam][e2] > 0 || conflicts[e2][exam] > 0) {
								selected = false;
								break;
							}
						}
						if(selected) {
							timeslot.add(exam);
							isSelected[exam] = 1;
							totExm--;
							break;
						}
					}
				}
			}
		}
	}
	
	private void solver(int position, int minPos, int maxPos) {
		if(position >= buckets.size()) {
			return;
		}
		
		ArrayList<Integer> list = buckets.get(position);
		for(int i = minPos; i < maxPos; i++) {
			ArrayList<Integer> t = timeslots.get(i);
			for(Integer e1:list) {
				if(isSelected[e1] == 0) {
					boolean selected = true;
					for(Integer e2:t) {
						if(conflicts[e1][e2] > 0 || conflicts[e2][e1] > 0) {
							selected = false;
							break;
						}
					}
					if(selected) {
						t.add(e1);
						isSelected[e1] = 1;
						totExm--;
						break;
					}
				}
			}
		}
		solver(position+1, minPos, maxPos);
	}
	
	public int[] getSolution() {
		return solution;
	}
	
	private void writeLog() throws IOException {
		String fileName = "log.txt";
		try(BufferedWriter r = new BufferedWriter(new FileWriter(fileName))) {
			for(ArrayList<Integer> a:buckets) {
				String s = "";
				for(Integer I:a) {
					s = s + I + " ";
				}
				s = s + "\n";
				r.write(s);
			}
			
			r.write(Integer.toString(buckets.get(0).size()));
		}
	}

}
