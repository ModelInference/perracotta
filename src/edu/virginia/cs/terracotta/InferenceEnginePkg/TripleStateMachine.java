/*
 * TripleStateMachine.java
 * Created on 2004-10-28
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg;

/**
 * @author Jinlin Yang
 */
abstract public class TripleStateMachine implements EventHandler {
	public static final int NA = 0;

	public static final int ONE_EFFECT = 1;

	public static final int ONE_CAUSE = 2;

	public static final int EFFECT_FIRST = 3;

	public static final int CAUSE_FIRST = 4;

	public static final int MULTI_CAUSE = 5;

	public static final int MULTI_EFFECT = 6;

	public static final int ALTERNATING = 7;

	public static final int RESPONSE = 8;

	public static final int MIXED = TripleStateMachine.PATTERNS;

	public static final int PATTERNS = 9;

	public static final String[] PATTERNNAMES = { "na", "oe", "oc", "ef", "cf",
			"mc", "me", "al", "rs", "mixed" };

	StateMachine cf, oe, oc;

	public TripleStateMachine(StateMachine cf, StateMachine oe, StateMachine oc) {
		this.cf = cf;
		this.oe = oe;
		this.oc = oc;
	}

	public void enterMethod(int event) {
		cf.enterMethod(event);
		oe.enterMethod(event);
		oc.enterMethod(event);
	}

	public void leaveMethod(int event) {
		cf.leaveMethod(event);
		oe.leaveMethod(event);
		oc.leaveMethod(event);
	}

	public void processTrace() {
		cf.processTrace();
		oe.processTrace();
		oc.processTrace();
	}

	public void processEnd() {
	}

	abstract public String collateResults(int p, int s);

	abstract public int getPattern(int p, int s);

	public void resetPairOfEvents(int p, int s) {
		cf.resetPairOfEvents(p, s);
		oe.resetPairOfEvents(p, s);
		oc.resetPairOfEvents(p, s);
	}
}