/*
 * SingleThreadAwareNormalStateMachine.java
 * Created on 2004-12-12
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.util.Arrays;

/**
 * @author Jinlin Yang
 */
public class SingleThreadAwareNormalStateMachine extends
		ThreadAwareNormalStateMachine {

	private ThreadAwareTraceReader tr;

	private int[][] switchesAt1;

	private int[][] switchesAt2;

	private int[][] switches1to2;

	/**
	 * @param tr
	 * @param name
	 * @param rule
	 */
	public SingleThreadAwareNormalStateMachine(ThreadAwareTraceReader tr,
			int patternCode, byte[][] rule) {
		super(tr, patternCode, rule);
		this.tr = tr;
		int types = tr.getEventTypes();
		switchesAt1 = new int[types][types];
		switchesAt2 = new int[types][types];
		switches1to2 = new int[types][types];
		for (int i = 0; i < types; i++) {
			Arrays.fill(switchesAt1[i], 0);
			Arrays.fill(switchesAt2[i], 0);
			Arrays.fill(switches1to2[i], 0);
		}
	}

	/**
	 * @see edu.virginia.cs.terracotta.inferenceengine.EventHandler#enterMethod(int)
	 */
	public void enterMethod(int event) {
		super.enterMethod(event);

		int method = tr.getMethodCode(event);

		for (int j = 0; j < tr.getEventTypes(); j++) {
			if (j == method)
				continue;

			// Check if there is any switches when event is the P event
			if (switchesAt1Row[j])
				switchesAt1[method][j]++;
			if (switchesAt2Row[j])
				switchesAt2[method][j]++;
			if (switches1to2Row[j])
				switches1to2[method][j]++;

			// Check if there is any switches when event is the S event
			if (switchesAt1Col[j])
				switchesAt1[j][method]++;
			if (switchesAt2Col[j])
				switchesAt2[j][method]++;
			if (switches1to2Col[j])
				switches1to2[j][method]++;
		}

		// Reset the switches in each State Machine
		resetSwitches();
	}

	public int getPattern(int p, int s) {
		return alive[p][s] ? patternCode : 0;
	}

	public String collateResults(int p, int s) {
		StringBuffer buf = new StringBuffer();
		buf.append(tr.getEventPair(p, s));
		buf.append(" threadSwitchesAtState1=" + getSwitchesAt1(p, s)
				+ " threadSwitchesAtState2=" + getSwitchesAt2(p, s)
				+ " threadSwitchesFromState1toState2=" + getSwitches1to2(p, s));
		return buf.toString();
	}

	public int getSwitchesAt1(int p, int s) {
		return switchesAt1[p][s];
	}

	public int getSwitchesAt2(int p, int s) {
		return switchesAt2[p][s];
	}

	public int getSwitches1to2(int p, int s) {
		return switches1to2[p][s];
	}
}