package it.polito.oma.solver.threads;

import java.util.Map;
import java.util.Random;

import it.polito.oma.solver.*;

public class Generator implements Runnable  {

	private int N=5;
	private int N2=4;
	private int T;
	private Map<Integer, Exam> exams;
	private int E;
	private Map<Integer, Student> students;
	private int S;
	private int[] solution;
	private int[][] tmpSol;
	private boolean finish = false;
	private Handler hand;
	private int[][] taboList;

	/*
	 * Set variables into the random generator
	 */
	public Generator(int T, Map<Integer, Exam> exams, Map <Integer, Student> students,Handler hand) {
		this.T = T;
		this.exams = exams;
		this.E = exams.size();
		this.students = students;
		this.S = students.size();
		solution = new int[E];
		tmpSol=new int[E][T];
		this.hand=hand;
		taboList=new int[N][N2];
	}
	
	@Override
	public void run() {
		/*
		 * set random parameters
		 */
		Random rand = new Random();
		boolean feasible = false;
		rand.setSeed(System.nanoTime());
		
		/*
		 * the vector isn't filled linearly, but every jump position, chosen randomly 
		 */
//        int jump = rand.nextInt(E/2)+1;
//        int key=rand.nextInt(T)+1;
//        for(int k=0;k<E;k++)
//        	solution[k]=key;
//		
//		for(int i = 0; i < E; ++i) {
//			solution[i] = rand.nextInt(T) + 1; //timeslot is choosen randomly
//			jump = rand.nextInt(jump/4 + 1) + jump/4 + 1; //then reset the jump parameter
//		}
//		solution=solv(solution,0);
//		for(int l=0;l<N;l++) {
//			System.out.print(taboList[l][0]+" "+taboList[l][1]+",");
//		}
//		System.out.println();
		
		for(int i=0;i<E;i++) {
			tmpSol[i][rand.nextInt(T)]=1;
		}
		
		tmpSol=solv(tmpSol,tmpSol,0,0);
//		tmpSol=solv(tmpSol,0);
//		for(int i=0;i<E;i++) {
//			for(int j=0;j<T;j++) {
//				System.out.print(tmpSol[i][j]);
//			}
//			System.out.println();
//		}
		for(int i=0;i<E;i++) {
			for(int j=0;j<T;j++) {
				if(tmpSol[i][j]==1)
					solution[i]=j+1;
			}
		}
		
		
		
		finish = true;
	}

// bestImprovement Second solution rappresentation
//	private int[][] solv(int[][] etSol, int count) {
//		count++;
//		if (count == 100000000)
//			return etSol;
//		int sol[][], bestSol[][];
//		int k, i, j;
//		if (hand.oF2(etSol) == 0) {
//			return etSol;
//		}
//
//		sol = new int[E][T];
//		bestSol = new int[E][T];
//
//		for (i = 0; i < E; i++) {
//			for (j = 0; j < E; j++) {
//				for (k = 0; k < T; k++) {
//					sol[j][k] = etSol[j][k];
//				}
//			}
//			for (j = 0; j < T; j++) {
//				for (k = 0; k < T; k++) {
//					sol[i][k] = 0;
//				}
//				sol[i][j] = 1;
//				if (hand.oF2(etSol) > hand.oF2(sol)) {
//					etSol = solv(sol, count);
//					return etSol;
//				}
//			}
//		}
//
//		return etSol;
//	}
	
	
// cambio soluzione
	private int[][] solv(int[][] bestSol, int[][] bestSolSoFar, int count,int status) {
		int i, j, k, l, bestTime = -1, bestEx = -1, preTime = -1, preEx = -1, tmpTime = -1, tmpEx = -1,
				NTabo=0;
		boolean tabo = false;
		Random rand=new Random(System.nanoTime());
//		if(count/3<N){
//		NTabo=N-count/3;
//		}
		if(hand.oF2(bestSolSoFar)==0) {
			return bestSolSoFar;
		}
		count++;
		if (count == 3000)
			return bestSolSoFar;
		int[][] sol = new int[E][T];
		int[][] saveCurrentState = new int[E][T];
		int bestBest, bestSoFar;
		bestSoFar=hand.oF2(bestSolSoFar);
		for (i = 0; i < E; i++)
			for (j = 0; j < T; j++) {
				saveCurrentState[i][j] = bestSol[i][j];
				bestSol[i][j]=1;
			}
		for (i = 0; i < E; i++) {
			for (k = 0; k < E; k++)
				for (l = 0; l < T; l++) {
					 sol[k][l] = saveCurrentState[k][l];
				}

			for (j = 0; j < T; j++) {
				bestBest=hand.oF2(bestSol);
				tabo = false;
				for (k = 0; k < T; k++) {
					if (sol[i][k] == 1) {
						sol[i][k] = 0;
						tmpEx = i;
						tmpTime = k;
					}
				}
				sol[i][j] = 1;
				for (k = 0; k < NTabo; k++) {
					if (taboList[k][0] == i && taboList[k][1] == j && taboList[k][2] == tmpEx
							&& taboList[k][1] == tmpTime)
						tabo = true;
				}
				if (!tabo) {
					if (bestBest > hand.oF2(sol)) {
						bestEx = i;
						bestTime = j;
						preEx = tmpEx;
						preTime = tmpTime;
						for (k = 0; k < E; k++)
							for (l = 0; l < T; l++) {
								bestSol[k][l] = sol[k][l];
							}

					}
				}
				if (bestSoFar > hand.oF2(sol)) {
					bestEx = i;
					bestTime = j;
					preEx = tmpEx;
					preTime = tmpTime;
					for (k = 0; k < E; k++)
						for (l = 0; l < T; l++) {
							bestSol[k][l] = sol[k][l];
						}

				}
			}
		}
		System.out.println(hand.oF2(bestSol)+" count"+count);
		if(hand.oF2(bestSol)==bestSoFar)
			status++;
		else
			status=0;
		if(status>=3) {
			i=rand.nextInt(E);
			j=rand.nextInt(T);
			for (k = 0; k < T; k++) {
				if (bestSol[i][k] == 1) {
					bestSol[i][k] = 0;
					preEx = i;
					bestTime = k;
				}
			}
			
			bestSol[i][j]=1;
			for (l = NTabo - 1; l > 0; l--) {
				taboList[l][0] = taboList[l - 1][0];
				taboList[l][1] = taboList[l - 1][1];
				taboList[l][2] = taboList[l - 1][2];
				taboList[l][3] = taboList[l - 1][3];

			}
			taboList[0][0] = preEx;
			taboList[0][1] = preTime;
			taboList[0][2] = i;
			taboList[0][3] = j;
			bestSolSoFar = solv(bestSol, bestSol, count,status);
			return bestSolSoFar;
		}
		
		for (l = NTabo - 1; l > 0; l--) {
			taboList[l][0] = taboList[l - 1][0];
			taboList[l][1] = taboList[l - 1][1];
			taboList[l][2] = taboList[l - 1][2];
			taboList[l][3] = taboList[l - 1][3];

		}
		taboList[0][0] = preEx;
		taboList[0][1] = preTime;
		taboList[0][2] = bestEx;
		taboList[0][3] = bestTime;
		if (bestSoFar > hand.oF2(bestSol)) {
			bestSolSoFar = solv(bestSol, bestSol, count,status);
			return bestSolSoFar;
		}
		bestSolSoFar = solv(bestSol, bestSolSoFar, count,status);
		return bestSolSoFar;
	}
	
//	taboList con best
//	private int[] solv(int[] bestSol, int[] bestSolSoFar, int count) {
//		 count++;
//		 if (count == 30) {
//		 return bestSolSoFar;
//		 }
//		int i, j, k, tmp, l, bestI = 0, bestJ = 0,bestEx1=E,bestEx2=E;
//		int[] sol = new int[E];
//		int[] saveCurrentGen = new int[E];
//		boolean status = false;
//		boolean tabo;
//		for (k = 0; k < E; k++) {
//			saveCurrentGen[k] = bestSol[k];
//		}
////		for (k = 0; k < E; k++) {
////			bestSol[k] = 1;
////		}
//		for (i = 0; i < E / 2; i++) {
//			for (j = i + 1; j < E; j++) {
//				for (k = 0; k < E; k++) {
//					// funge meglio ma non so dire il perchè
//					 sol[k] = bestSol[k];
//					// save the solution that generate the neighborhood
////					sol[k] = saveCurrentGen[k];
//				}
//				tabo = false;
//				if (sol[i] != sol[j]) {
//					tmp = sol[i];
//					sol[i] = sol[j];
//					sol[j] = tmp;
//					for (l = 0; l < N; l++) {
//						if (taboList[l][0] == j || taboList[l][1] == i || taboList[l][2] == sol[i]
//								|| taboList[l][3] == sol[j]) {
//							tabo = true;
//							break;
//						}
//					}
//					// in the taboList? BestSolutionSoFar?
//					if (!tabo) {
//						if (hand.oF(bestSol) >= hand.oF(sol)) {
//							for (k = 0; k < E; k++) {
//								bestSol[k] = sol[k];
//							}
//							if (hand.oF(bestSol) == 0) {
//								return bestSol;
//							}
//							bestI = i;
//							bestJ = j;
//							bestEx1=sol[i];
//							bestEx2=sol[j];
//						}
//					}
//					if (hand.oF(bestSolSoFar) > hand.oF(sol)) {
//						for (k = 0; k < E; k++) {
//							bestSol[k] = sol[k];
//						}
//						if (hand.oF(bestSol) == 0) {
//							return bestSol;
//						}
//						bestI = i;
//						bestJ = j;
//						bestEx1=sol[i];
//						bestEx2=sol[j];
//					}
//				}
//			}
//		}
//		for (l = N - 1; l > 0; l--) {
//			taboList[l][0] = taboList[l - 1][0];
//			taboList[l][1] = taboList[l - 1][1];
//			taboList[l][2] = taboList[l - 1][2];
//			taboList[l][3] = taboList[l - 1][3];
//			
//		}
//		taboList[0][0] = bestJ;
//		taboList[0][1] = bestI;
//		taboList[0][2] = bestEx2;
//		taboList[0][3] = bestEx1;
//		if (hand.oF(bestSolSoFar) > hand.oF(bestSol)) {
//			// bestSol become the bestSolSoFar
//			bestSolSoFar = solv(bestSol, bestSol, count);
//			return bestSolSoFar;
//		}
//		bestSolSoFar = solv(bestSol, bestSolSoFar, count);
//		return bestSolSoFar;
//	}
	
	
	
	
//tabo con first
//	private int[] solv(int[] bestSol, int[] bestSolSoFar, int count) {
//		count++;
//		if (count == 3000) {
//			return bestSolSoFar;
//		}
//		int i, j, k, tmp, l;
//		int[] sol = new int[E];
//		int[] saveCurrentGen = new int[E];
//		boolean tabo;
//		for (k = 0; k < E; k++) {
//			saveCurrentGen[k] = bestSol[k];
//		}
//		 for (k = 0; k < E; k++) {
//		 bestSol[k] = 1;
//		 }
//		for (i = 0; i < E / 2; i++) {
//			for (j = i + 1; j < E; j++) {
//				for (k = 0; k < E; k++) {
//					// funge meglio ma non so dire il perchè
////					sol[k] = bestSol[k];
//					// save the solution that generate the neighborhood
//					 sol[k] = saveCurrentGen[k];
//				}
//				tabo = false;
//				if (sol[i] != sol[j]) {
//					tmp = sol[i];
//					sol[i] = sol[j];
//					sol[j] = tmp;
//					for (l = 0; l < N; l++) {
//						if (taboList[l][0] == j && taboList[l][1] == i && taboList[l][2] == sol[i]
//								&& taboList[l][3] == sol[j]) {
//							tabo = true;
//						}
//					}
//					// in the taboList? BestSolutionSoFar?
//					if (!tabo) {
//						if (hand.oF(bestSol) >= hand.oF(sol)) {
//							for (k = 0; k < E; k++) {
//								bestSol[k] = sol[k];
//							}
//							if (hand.oF(bestSol) == 0) {
//								return bestSol;
//							}
//							for (l = N - 1; l > 0; l--) {
//								taboList[l][0] = taboList[l - 1][0];
//								taboList[l][1] = taboList[l - 1][1];
//								taboList[l][2] = taboList[l - 1][2];
//								taboList[l][3] = taboList[l - 1][3];
//							}
//							taboList[0][0] = j;
//							taboList[0][1] = i;
//							taboList[0][2] = sol[j];
//							taboList[0][3] = sol[i];
//
//							if (hand.oF(bestSolSoFar) > hand.oF(bestSol)) {
//								// bestSol become the bestSolSoFar
//								bestSolSoFar = solv(bestSol, bestSol, count);
//								return bestSolSoFar;
//							}
//							bestSolSoFar = solv(bestSol, bestSolSoFar, count);
//							return bestSolSoFar;
//						}
//					}
//					if (hand.oF(bestSolSoFar) > hand.oF(sol)) {
//						for (k = 0; k < E; k++) {
//							bestSol[k] = sol[k];
//						}
//						if (hand.oF(bestSol) == 0) {
//							return bestSol;
//						}
//						for (l = N - 1; l > 0; l--) {
//							taboList[l][0] = taboList[l - 1][0];
//							taboList[l][1] = taboList[l - 1][1];
//							taboList[l][2] = taboList[l - 1][2];
//							taboList[l][3] = taboList[l - 1][3];
//						}
//						taboList[0][0] = j;
//						taboList[0][1] = i;
//						taboList[0][2] = sol[j];
//						taboList[0][3] = sol[i];
//						bestSolSoFar = solv(bestSol, bestSol, count);
//						return bestSolSoFar;
//
//					}
//				}
//			}
//		}
//		return bestSolSoFar;
//	}
	
//	private int[] solv(int[] etSol,int count) {
////		count++;
////		if(count==50)
////			return etSol;
//		int sol[],bestSol[];
//		int k,i,j;
//		if(hand.oF(etSol)==0) {
//			return etSol;
//		}
//		sol = new int[E];
//		bestSol = new int[E];
//
//		for(int l=0;l<E;l++) {
//			bestSol[l]=etSol[l];
//		}
//		for (i = 0; i < E / 2; i++) {
//			for (j = i + 1; j < E; j++) {
//				for (k = 0; k < E; k++) {
//					sol[k] = etSol[k];
//				}
//				if (i != j && sol[i] != sol[j]) {
//					int tmp;
//					tmp = sol[i];
//					sol[i] = sol[j];
//					sol[j] = tmp;
//					if (hand.oF(bestSol) > hand.oF(sol)) {
//						for (k = 0; k < E; k++) {
//							bestSol[k] = sol[k];
//						}
//						if (hand.oF(bestSol) == 0) {
//							return bestSol;
//						}
////						first improvement
////						if (hand.oF(bestSol) < hand.oF(etSol)) {
////							bestSol = solv(bestSol, count);
////							return bestSol;
////						}
//					}
//				}
//			}
//		}
//		search for the best and then iterate
//		if(hand.oF(bestSol)<hand.oF(etSol)) {
//			bestSol=solv(bestSol,count);
//			return bestSol;
//		}
//		return etSol;
//	}
	
	
	public int[] getSolution() {
		return solution;
	}
	
	public boolean isFinished() {
		return finish ;
	}

}
