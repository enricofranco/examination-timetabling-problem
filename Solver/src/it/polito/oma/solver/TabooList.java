package it.polito.oma.solver;

import it.polito.oma.solver.threads.TimeSlot;

public class TabooList {
	
	private int[][] tabooList;
	private int tabooNumber;
	private int tabooPosition;
	
	public TabooList(int tabooNumber){
		this.tabooNumber=tabooNumber;
		tabooList=new int[this.tabooNumber][2];
		for(int i=0;i<tabooNumber;i++) {
			tabooList[i][0]=-1;
			tabooList[i][1]=-1;
		}
		tabooPosition=0;
	}

	
	public void setTaboo(TimeSlot ts,Exam e) {
		if(ts==null) {
			tabooList[tabooPosition][0]=-1;
			tabooList[tabooPosition][1]=-1;
		}
		else {
			tabooList[tabooPosition][0]=ts.getId();
			tabooList[tabooPosition][1]=e.getId();
		}
		tabooPosition++;
		if(tabooPosition>=tabooNumber) {
			tabooPosition=0;
		}
	}
	
	public boolean checkTaboo(TimeSlot ts,Exam e) {
		for(int i=0; i<tabooNumber; i++) {
			if(tabooList[i][0]!=-1) {
				if(tabooList[i][0]==ts.getId()&&tabooList[i][1]==e.getId()) {
					return true;
				}
			}	
		}
		return false;
	}
	
	public void cleanTabooList() {
		for(int i=0; i<tabooNumber; i++) {
			tabooList[i][0]=-1;
			tabooList[i][1]=-1;
		}
	}
}
