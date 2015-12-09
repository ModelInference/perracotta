/*
 * Created on Jul 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg.prob;

import java.util.Arrays;

import edu.virginia.cs.terracotta.InferenceEnginePkg.EventHandler;
import edu.virginia.cs.terracotta.InferenceEnginePkg.NormalStateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.StateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TraceReader;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TripleNormalStateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TripleStateMachine;

/**
 * @author t-jinyan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class OneMonitorAndTripleStateMachine implements EventHandler {

	private NormalStateMachine mFSM;

	private TripleNormalStateMachine triFSM;

	int types;

	int[][][] stat;

	TraceReader tr;

	public OneMonitorAndTripleStateMachine(TraceReader tr) {
		mFSM = new NormalStateMachine(tr.getEventTypes(),
				TripleStateMachine.RESPONSE, StateMachine.RESPONSE);
		triFSM = new TripleNormalStateMachine(tr);

		this.tr = tr;
		types = tr.getEventTypes();

		stat = new int[types][types][8];
		for (int i = 0; i < types; i++)
			for (int j = 0; j < types; j++) {
				Arrays.fill(stat[i][j], 0);
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.virginia.cs.terracotta.InferenceEnginePkg.EventHandler#enterMethod(int)
	 */
	public void enterMethod(int event) {
		// Then let us check whether we reach the end of a subtrace
		for (int i = 0; i < types; i++) {
			if (i == event)
				continue;
			if (mFSM.getCurrentState(event, i) == 2) {
				triFSM.updateAlive(event, i);
				stat[event][i][triFSM.getPattern(event, i)]++;
				triFSM.resetPairOfEvents(event, i);
			}
		}

		mFSM.enterMethod(event);
		triFSM.enterMethod(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.virginia.cs.terracotta.InferenceEnginePkg.EventHandler#leaveMethod(int)
	 */
	public void leaveMethod(int event) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.virginia.cs.terracotta.InferenceEnginePkg.EventHandler#processTrace()
	 */
	public void processTrace() {
		for (int p = 0; p < types; p++) {
			for (int s = 0; s < types; s++) {
				if (s == p)
					continue;
				// This condition indicates that we have seen either P or S
				// This is used to distinguish empty trace from a trace that
				// only has P Events or S Events
				if ((mFSM.getCurrentState(p, s) != 0)
						|| (mFSM.getCurrentState(s, p) != 0)) {
					triFSM.updateAlive(p, s);
					stat[p][s][triFSM.getPattern(p, s)]++;
					mFSM.resetPairOfEvents(p, s);
					triFSM.resetPairOfEvents(p, s);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.virginia.cs.terracotta.InferenceEnginePkg.EventHandler#processEnd()
	 */
	public void processEnd() {
		// TODO Auto-generated method stub

	}

	public double getStat(int p, int s, int pattern) {
		int total = 0;
		for (int i = 0; i < stat[p][s].length; i++)
			total += stat[p][s][i];
		if (total != 0)
			return 1.0 * stat[p][s][pattern] / total;
		else
			return 0.0;
	}

	public double getScore(int p, int s) {
		int al = stat[p][s][TripleStateMachine.ALTERNATING];
		int me = stat[p][s][TripleStateMachine.MULTI_EFFECT];
		int mc = stat[p][s][TripleStateMachine.MULTI_CAUSE];
		int ef = stat[p][s][TripleStateMachine.EFFECT_FIRST];
		int oc = stat[p][s][TripleStateMachine.ONE_CAUSE];
		int oe = stat[p][s][TripleStateMachine.ONE_EFFECT];
		int cf = stat[p][s][TripleStateMachine.CAUSE_FIRST];
		int na = stat[p][s][TripleStateMachine.NA];

		int total = al + me + mc + ef + oc + oe + cf + na;

		double score;
		if (total != 0)
			score = 3.0 * al / total + 2.0 * (me + mc + ef) / total + 1.0
					* (oc + oe + cf) / total;
		else
			score = 0.0;
		return score;
	}

	public String collateResults(int p, int s) {
		return tr.getEventPair(p, s);
	}
}