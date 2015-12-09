package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.util.Arrays;

public class StateMachine implements EventHandler {

	public final static byte[][] ALTERNATING = { { 3, 1 }, { 2, 3 }, { 3, 1 },
			{ 3, 3 } };

	public final static byte[][] MULTIEFFECT = { { 3, 1 }, { 2, 3 }, { 2, 1 },
			{ 3, 3 } };

	public final static byte[][] MULTICAUSE = { { 3, 1 }, { 2, 1 }, { 3, 1 },
			{ 3, 3 } };

	public final static byte[][] EFFECTFIRST = { { 0, 1 }, { 2, 3 }, { 3, 1 },
			{ 3, 3 } };

	public final static byte[][] CAUSE_FIRST = { { 3, 1 }, { 2, 1 }, { 2, 1 },
			{ 3, 3 } };

	public final static byte[][] ONE_EFFECT = { { 0, 1 }, { 2, 1 }, { 3, 1 },
			{ 3, 3 } };

	public final static byte[][] ONE_CAUSE = { { 0, 1 }, { 2, 3 }, { 2, 1 },
			{ 3, 3 } };

	public final static byte[][] RESPONSE = { { 0, 1 }, { 2, 1 }, { 2, 1 } };

	byte state[][];

	int types;

	byte rule[][];

	String name;

	int patternCode;

	public StateMachine(int types, int patternCode, byte rule[][]) {
		this.types = types;
		state = new byte[types][types];
		this.rule = rule;
		this.patternCode = patternCode;
		this.name = TripleStateMachine.PATTERNNAMES[patternCode];
		for (int i = 0; i < state.length; i++)
			Arrays.fill(state[i], (byte) 0);
	}

	public String getName() {
		return name;
	}

	public int getPatternCode() {
		return patternCode;
	}

	public void enterMethod(int event) {
		for (int j = 0; j < types; j++) {
			if (j != event) {
				// Update row with S event
				state[event][j] = rule[state[event][j]][1];
				// Update column with P event
				state[j][event] = rule[state[j][event]][0];
			}
		}
	}

	public void leaveMethod(int event) {
	}

	public void processTrace() {
		for (int i = 0; i < state.length; i++)
			Arrays.fill(state[i], (byte) 0);
	}

	public void processEnd() {
	}

	public void resetPairOfEvents(int P, int S) {
		state[P][S] = 0;
	}

	public byte getCurrentState(int P, int S) {
		return state[P][S];
	}
}