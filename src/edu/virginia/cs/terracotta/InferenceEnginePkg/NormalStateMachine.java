/*
 * NormalStateMachine.java
 * Created on 2004-10-28
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.util.Arrays;

/**
 * @author Jinlin Yang
 */
public class NormalStateMachine extends StateMachine {

	boolean alive[][];

	int total = 0;

	/**
	 * @param tr
	 * @param name
	 * @param rule
	 */
	public NormalStateMachine(int types, int patternCode, byte[][] rule) {
		super(types, patternCode, rule);
		alive = new boolean[types][types];
		total = types * types;
		for (int i = 0; i < types; i++)
			Arrays.fill(alive[i], true);
	}

	/**
	 * @see edu.virginia.cs.terracotta.inferenceengine.EventHandler#processTrace()
	 */
	public void processTrace() {
		for (int i = 0; i < types; i++) {
			for (int j = 0; j < types; j++) {
				if (i != j) {
					// Check to see if machine is in final state
					if (alive[i][j] && state[i][j] % 2 == 1) {
						alive[i][j] = false;
						total--;
					}
				}
			}
		}
		super.processTrace();
	}

	//	public boolean getProperty(int p, int s) {
	//		return alive[p][s];
	//	}

	public void updateAlive(int p, int s){
		if (alive[p][s] && state[p][s] % 2 == 1) {
			alive[p][s] = false;
		}
	}
	
	public void resetPairOfEvents(int p, int s) {
		alive[p][s] = true;
		super.resetPairOfEvents(p, s);
	}

	public boolean isAlive(int p, int s) {
		return alive[p][s];
	}
}