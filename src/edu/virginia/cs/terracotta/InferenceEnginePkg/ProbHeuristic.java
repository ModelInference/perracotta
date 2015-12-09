package edu.virginia.cs.terracotta.InferenceEnginePkg;
public class ProbHeuristic implements ScoringHeuristic {
    TraceReader tr;
    TripleNormalStateMachine tsm;
    int event_count[];
    public ProbHeuristic(TraceReader tr,TripleNormalStateMachine tsm) {
        this.tr= tr;
        this.tsm= tsm;
        event_count = new int[tr.getEventTypes()];
    }
    public void enterMethod(int event) {
        event_count[event]++;
    }
    public void leaveMethod(int event) { }
    public void processTrace() { }
    public void processEnd() { }

    public String getScores(int p,int s) { 
        int pattern = tsm.getPattern(p,s); 
        int n = event_count[p]+event_count[s];
        int k = event_count[s];
        double prob;
        switch(pattern) {
            case TripleStateMachine.ALTERNATING:
                prob = (k==n/2)?log_choose(n,k):Double.POSITIVE_INFINITY; break;
            case TripleStateMachine.EFFECT_FIRST:
                prob = (k>=n/2)?log_choose(n,k):Double.POSITIVE_INFINITY; break;
            case TripleStateMachine.CAUSE_FIRST:
                prob = log_choose(n,k) - log_choose(n-2,k-1); break;
            case TripleStateMachine.NA:
                prob = log_choose(n,k) - log_choose(n-1,k-1); break;
            case TripleStateMachine.MULTI_CAUSE:
                prob = log_choose(n,k) - log_choose(n-k-1,k-1); break;
            case TripleStateMachine.MULTI_EFFECT:
                prob = log_choose(n,k) - log_choose(k-1,n-k-1); break;
            case TripleStateMachine.ONE_CAUSE:
                prob = log_choose(n,k) - log_choose(k,n-k); break;
            case TripleStateMachine.ONE_EFFECT:
                prob = Double.NaN; break; // Not implemented
            default:
                throw new RuntimeException("unexpected pattern "+pattern);
        }
        return " prob="+prob+" counts=("+event_count[p]+","+event_count[s]+")";
    }

    double log_fact(double n) {
        return n*Math.log(n)-n+1;
    }

    double log_choose(double n,double k) {
        return log_fact(n) - (log_fact(k)+log_fact(n-k));
    }
}
