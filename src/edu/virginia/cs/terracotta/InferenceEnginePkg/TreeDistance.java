package edu.virginia.cs.terracotta.InferenceEnginePkg;
public class TreeDistance implements ScoringHeuristic {
    TraceReader tr;
    int mindepth[];
    int last[];
    int count[][];
    double mean[][];
    int min[][];
    int max[][];
    int sum[][];
    int types;
    int depth=0;

    public TreeDistance(TraceReader tr) {
        this.tr = tr;
        types = tr.getEventTypes();
        sum = new int[types][types];
        mean = new double[types][types];
        count = new int[types][types];
        min = new int[types][types];
        max = new int[types][types];
        mindepth = new int[types];
        last = new int[types];
        last = new int[types];
    }

    public void enterMethod(int event) { 
        depth++;
        for (int i=0; i<types; i++) {
            if (last[i]>0) {
                int distance = (last[i]-mindepth[i]) + (depth-mindepth[i]);
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
        last[event]=mindepth[event]=depth;
    } 

    public void leaveMethod(int event) {
        if (last[event]==0) {
            // This should not happen in a valid trace
            System.err.println("unexpected exit: "+tr.getEvent(event));
        }
        if (last[event]<depth) {
            // This should not happen either in a valid trace
            System.err.println("missing exit noted at "+
                               tr.getEvent(event));
            depth=last[event];
        } else if (last[event]>depth) {
            // Nor this
            System.err.println("extra exit noted at "+
                               tr.getEvent(event));
            depth=last[event];
        }
        depth--;
        for (int i=0; i<types; i++) {
            if (depth < mindepth[i])
                mindepth[i]=depth;
        }
    }

    public void processTrace() { 
        if (depth != 0) {
            System.err.println("Unbalanced trace "+depth);
        }
        depth=0;
        for (int i=0; i<types; i++) {
            mindepth[i]=last[i]=0;
        }
    }

    public void processEnd() { }

    public String getScores(int a,int b) {
        return " tree_min="+min[a][b]+
               " tree_mean="+mean[a][b]+
               " tree_max="+max[a][b];
    }
}
