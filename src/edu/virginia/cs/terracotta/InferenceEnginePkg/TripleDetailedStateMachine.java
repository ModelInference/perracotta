/*
 * TripleDetailedStateMachine.java
 * Created on 2004-10-28
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.util.Arrays;

/**
 * @author Jinlin Yang
 */
public class TripleDetailedStateMachine extends TripleStateMachine {

	int numberOfTraces = 0;

	TraceReader tr;

	public TripleDetailedStateMachine(TraceReader tr) {
		super(new DetailedStateMachine(tr.getEventTypes(), tr
				.getNumberOfTraces(), TripleStateMachine.CAUSE_FIRST,
				StateMachine.CAUSE_FIRST), new DetailedStateMachine(tr
				.getEventTypes(), tr.getNumberOfTraces(),
				TripleStateMachine.ONE_EFFECT, StateMachine.ONE_EFFECT),
				new DetailedStateMachine(tr.getEventTypes(), tr
						.getNumberOfTraces(), TripleStateMachine.ONE_CAUSE,
						StateMachine.ONE_CAUSE));
		numberOfTraces = tr.getNumberOfTraces();
		this.tr = tr;
	}

	public float[] getPercentages(int p, int s) {
		int[] absVal = new int[PATTERNS];
		Arrays.fill(absVal, 0);

		for (int i = 0; i < numberOfTraces; i++)
			absVal[getIthTracePattern(p, s, i)]++;

		float[] percentages = new float[PATTERNS];
		for (int i = 0; i < PATTERNS; i++)
			if (i != TripleStateMachine.NA) {
				//FIXME Max(freq_p, freq_s) is not the perfect way.
				percentages[i] = (float) (absVal[i] * 100.0 / Math.max(tr
						.getEvent(p).getTraces(), tr.getEvent(s).getTraces()));
			} else {
				percentages[i] = (float) (absVal[i] * 100.0 / numberOfTraces);
			}
		return percentages;
	}

	private int getIthTracePattern(int p, int s, int i) {
		boolean cfMatched = (((DetailedStateMachine) cf).alive[p][s][i / 8] & DetailedStateMachine.setBit[i % 8]) != 0;
		boolean oeMatched = (((DetailedStateMachine) oe).alive[p][s][i / 8] & DetailedStateMachine.setBit[i % 8]) != 0;
		boolean ocMatched = (((DetailedStateMachine) oc).alive[p][s][i / 8] & DetailedStateMachine.setBit[i % 8]) != 0;

		return (cfMatched ? TripleStateMachine.CAUSE_FIRST : 0)
				+ (ocMatched ? TripleStateMachine.ONE_CAUSE : 0)
				+ (oeMatched ? TripleStateMachine.ONE_EFFECT : 0);
	}

	public String collateResults(int p, int s) {
		StringBuffer buf = new StringBuffer();
		buf.append(tr.getEventPair(p, s));
		buf.append(" pTraces=" + tr.getEvent(p).getTraces() + " sTraces="
				+ tr.getEvent(s).getTraces());
		int code = getPattern(p, s);
		if (code == TripleStateMachine.MIXED) {
			float[] percentages = getPercentages(p, s);
			for (int i = percentages.length - 1; i > -1; i--)
				if (percentages[i] != 0.0)
					buf.append(" " + TripleStateMachine.PATTERNNAMES[i] + "="
							+ percentages[i]);
			if (percentages[TripleStateMachine.ALTERNATING] >= 99.0) {
				buf.append(" traces_violating_alternating:");
				for (int i = 0; i < numberOfTraces; i++)
					if (getIthTracePattern(p, s, i) != TripleStateMachine.ALTERNATING)
						buf.append(" " + i);
			}
		}
		return buf.toString();
	}

	public int getPattern(int p, int s) {
		float[] percentages = getPercentages(p, s);

		// check if events p and s 100% match one pattern
		int code = 0;
		for (code = 0; code < TripleStateMachine.PATTERNS; code++) {
			if (percentages[code] == 100.0)
				break;
		}

		if (code < TripleStateMachine.PATTERNS)
			return code;
		else
			return TripleStateMachine.MIXED;
	}
}