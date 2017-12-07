package it.polito.oma.solver.threads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import it.polito.oma.solver.*;

public class Generator implements Runnable  {

	private int T;
	private int E;
	private int[][] conflicts;
	private int[] solution;
	private int[] isSelected;
	private int[][] examPriority;
	private Random rand = new Random();

	/*
	 * Set variables into the random generator
	 */
	public Generator(int T, int E, int[][] conflicts) {
		this.T = T;
		this.E = E;
		this.conflicts = conflicts;
		solution = new int[E];
		rand.setSeed(System.nanoTime());
		isSelected = new int[E];
		examPriority = new int[E][2];
		for(int i = 0; i < E; i++) {
			examPriority[i][0] = Integer.MAX_VALUE;
		}
		int priority;
		for(int i = 0; i < E; i++) {
			priority = 0;
			for(int j = 0; j < E; j++) {
				if(conflicts[j][i] > 0 || conflicts[i][j] > 0)
					priority ++;
			}
//			System.out.println(i + " " + priority);
			for(int j = 0; j < E; j++) {
				if(examPriority[j][0] > priority) {
					for(int k = E-1; k > j; k--) {
						examPriority[k][0] = examPriority[k-1][0];
						examPriority[k][1] = examPriority[k-1][1];
					}
					examPriority[j][0] = priority;
					examPriority[j][1] = i;
					break;
				}
			}
		}
		System.out.println();
		for(int i = 0; i < E; i++) {
			System.out.print(examPriority[i][0] + " ");
			System.out.println(examPriority[i][1] + " ");
		}
		System.out.println();
	}
	
	@Override
	public void run() {
		Map<Integer, ArrayList<Integer>> buckets = setBuckets();
		for(ArrayList<Integer> a:buckets.values()) {
			for(Integer I:a) {
				System.out.print(I + " ");
			}
			System.out.println();
		}
		System.out.println(buckets.keySet().size());
	}
	
	private Map<Integer, ArrayList<Integer>> setBuckets() {
		Map<Integer, ArrayList<Integer>> bucket = new TreeMap<>();
		int iExm, jExm;
		for(int i = 0; i < E; i++) {
			iExm = examPriority[i][1];
			if(isSelected[iExm] != 1) {
				isSelected[iExm] = 1;
				ArrayList<Integer> l = new ArrayList<>();
				l.add(iExm);
				for(int j = E-1; j >= 0; j--) {
					jExm = examPriority[j][1];
					if(isSelected[jExm] != 1) {
						if(conflicts[jExm][iExm] == 0 && conflicts[iExm][jExm] == 0 && jExm != iExm) {
							isSelected[jExm] = 1;
							l.add(jExm);
							for(int k = 0; k < l.size(); k++) {
								int exm = l.get(k);
								if(conflicts[jExm][exm] > 0 || conflicts[exm][jExm] > 0) {
									l.remove(k);
									isSelected[exm] = 0;
								}
							}
						}
						
					}
				}
				bucket.put(i, l);
			}
		}
		List<Integer> reverseKeyset = bucket.keySet()
				.stream()
				.sorted(Collections.reverseOrder())
				.collect(Collectors.toList());
		ArrayList<Integer> list;
		ArrayList<Integer> revList;
		boolean insert = false;
		for(int l = 0; l < 10; l++) {
			for(Integer i:bucket.keySet()) {
				list = bucket.get(i);
				for(int j = 0; j < list.size(); j++) {
					int el = list.get(j);
					for(Integer rev:reverseKeyset) {
						if(rev == i)
							break;
						revList = bucket.get(rev);
						insert = true;
						for(int k = revList.size()-1; k >= 0; k--) {
							if(conflicts[el][revList.get(k)] > 0 || conflicts[revList.get(k)][el] > 0) {
								insert = false;
								break;
							}
						}
						if(insert) {
							revList.add(el);
							list.remove(j);
							break;
						}
					}
				}
			}
		}
		List<Integer> keySet =  bucket.keySet().stream().collect(Collectors.toList());
		for(int i = 0; i < keySet.size(); i++) {
			if(bucket.get(keySet.get(i)).size() == 0)
				bucket.remove(keySet.get(i));
		}
		return bucket;
	}

	public int[] getSolution() {
		return solution;
	}

}
