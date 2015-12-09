package edu.virginia.cs.terracotta.InferenceEnginePkg;
public class RawDistance implements ScoringHeuristic {
    TraceReader tr;
    int last[];
    int count[][];
    double mean[][];
    int min[][];
    int max[][];
    int sum[][];
    int types;
    int n=0;

    public RawDistance(TraceReader tr) {
        this.tr = tr;
        types = tr.getEventTypes();
        sum = new int[types][types];
        mean = new double[types][types];
        count = new int[types][types];
        min = new int[types][types];
        max = new int[types][types];
        last = new int[types];
    }

    public void enterMethod(int event) { 
        n++;
        for (int i=0; i<types; i++) {
            if (last[i]>0) {
                int distance = n-last[i];
                sum[i][event] += distance;
                mean[i][event] = mean[i][event]*
                                 (count[i][event]/(count[i][event]+1.0)) + 
                                 distance/(count[i][event]+1.0);
                count[i][event]++;
                if (min[i][event]==0 || min[i][event]>distance) {
                    min[i][event]=distance;
                }
                if (max[i][event]<distance) {
                    max[i][event]=distance;
                }
            }
        }
        last[event]=n;
    }

    public void leaveMethod(int event) { }

    public void processTrace() { 
        n=0;
        for (int i=0; i<types; i++) {
            last[i]=0;
        }
    }

    public void processEnd() { }

    public String getScores(int a,int b) {
        return " rawmin="+min[a][b]+
               " rawmean="+mean[a][b]+
               " rawmax="+max[a][b];
    }
}
