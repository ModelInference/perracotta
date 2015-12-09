/*
 * ThreadAwareTripleNormalStateMachine.java
 * Created on 2004-10-29
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.util.Arrays;

/**
 * @author Jinlin Yang
 */
public class ThreadAwareTripleNormalStateMachine extends
        TripleNormalStateMachine {

    ThreadAwareTraceReader tr;

    int[][] lastThread;

    int[][] switches;

    int types;

    /**
     * @param tr
     */
    public ThreadAwareTripleNormalStateMachine(ThreadAwareTraceReader tr) {
        super(tr);
        this.tr = tr;
        this.types = tr.getEventTypes();
        lastThread = new int[types][types];
        switches = new int[types][types];
        for (int i = 0; i < types; i++) {
            Arrays.fill(lastThread[i], -1);
            Arrays.fill(switches[i], 0);
        }
    }

    public void enterMethod(int event) {
        int method = tr.getMethodCode(event);
        int thread = tr.getThreadCode(event);
        super.enterMethod(method);
        
        for (int j = 0; j < types; j++) {
            if (j > method) {
                // Update row with S event
                if (lastThread[method][j] == -1)
                    lastThread[method][j] = thread;
                else if (lastThread[method][j] != thread) {
                    switches[method][j]++;
                    lastThread[method][j] = thread;
                }
                switches[j][method] = switches[method][j];
            } else if (j < method) {
                // Update column with P event
                if (lastThread[j][method] == -1)
                    lastThread[j][method] = thread;
                else if (lastThread[j][method] != thread) {
                    switches[j][method]++;
                    lastThread[j][method] = thread;
                }
                switches[method][j] = switches[j][method];
            }
        }
    }

    public int getSwitches(int p, int s) {
        return switches[p][s];
    }
}