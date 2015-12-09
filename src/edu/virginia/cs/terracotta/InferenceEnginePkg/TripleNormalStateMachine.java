package edu.virginia.cs.terracotta.InferenceEnginePkg;

public class TripleNormalStateMachine extends TripleStateMachine {

	TraceReader tr;

	public TripleNormalStateMachine(TraceReader tr) {
		super(
				new NormalStateMachine(tr.getEventTypes(),
						TripleStateMachine.CAUSE_FIRST,
						StateMachine.CAUSE_FIRST),
				new NormalStateMachine(tr.getEventTypes(),
						TripleStateMachine.ONE_EFFECT, StateMachine.ONE_EFFECT),
				new NormalStateMachine(tr.getEventTypes(),
						TripleStateMachine.ONE_CAUSE, StateMachine.ONE_CAUSE));
		this.tr = tr;
	}

	public int getPattern(int p, int s) {
		return (((NormalStateMachine) cf).isAlive(p, s) ? 4 : 0)
				+ (((NormalStateMachine) oc).isAlive(p, s) ? 2 : 0)
				+ (((NormalStateMachine) oe).isAlive(p, s) ? 1 : 0);
	}

	public String collateResults(int p, int s) {
		StringBuffer buf = new StringBuffer();
		buf.append(tr.getEventPair(p, s));
		return buf.toString();
	}

	public void updateAlive(int p, int s) {
		((NormalStateMachine) cf).updateAlive(p, s);
		((NormalStateMachine) oe).updateAlive(p, s);
		((NormalStateMachine) oc).updateAlive(p, s);
	}
}