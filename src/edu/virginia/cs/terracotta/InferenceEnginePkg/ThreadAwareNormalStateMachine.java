/*
 * ThreadAwareNormalStateMachine.java
 * Created on 2004-10-29
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.util.Arrays;

/**
 * @author Jinlin Yang
 */
public class ThreadAwareNormalStateMachine extends NormalStateMachine {

    private ThreadAwareTraceReader tr;
    
    private int[][] lastThread;

    boolean[] switchesAt1Row;

    boolean[] switchesAt1Col;

    boolean[] switchesAt2Row;

    boolean[] switchesAt2Col;

    boolean[] switches1to2Row;

    boolean[] switches1to2Col;

    private byte[] lastStateRow;

    private byte[] lastStateCol;

    /**
     * @param tr
     * @param name
     * @param rule
     */
    public ThreadAwareNormalStateMachine(ThreadAwareTraceReader tr,
            int patternCode, byte[][] rule) {
        super(tr.getEventTypes(), patternCode, rule);
        this.tr = tr;
        lastThread = new int[types][types];
        switchesAt1Row = new boolean[types];
        switchesAt1Col = new boolean[types];
        switchesAt2Row = new boolean[types];
        switchesAt2Col = new boolean[types];
        switches1to2Row = new boolean[types];
        switches1to2Col = new boolean[types];
        lastStateRow = new byte[types];
        lastStateCol = new byte[types];
        for (int i = 0; i < types; i++) {
            Arrays.fill(lastThread[i], -1);
            switchesAt1Row[i] = false;
            switchesAt1Col[i] = false;
            switchesAt2Row[i] = false;
            switchesAt2Col[i] = false;
            switches1to2Row[i] = false;
            switches1to2Col[i] = false;
            lastStateRow[i] = 0;
            lastStateCol[i] = 0;
        }
    }

    /**
     * @see edu.virginia.cs.terracotta.inferenceengine.EventHandler#enterMethod(int)
     */
    public void enterMethod(int event) {
        int method = tr.getMethodCode(event);
        int thread = tr.getThreadCode(event);
        for (int j = 0; j < types; j++) {
            if (j != method) {
                lastStateCol[j] = state[j][method];
                lastStateRow[j] = state[method][j];
            }
        }
        super.enterMethod(method);
        for (int j = 0; j < types; j++) {
            if (j != method) {
                // Update row with S event
                if ((state[method][j] == 1) && (lastStateRow[j] == 0)) {
                    lastThread[method][j] = thread;
                } else if ((state[method][j] == 1) && (lastStateRow[j] == 1)
                        && (lastThread[method][j] != thread)) {
                    switchesAt1Row[j] = true;
                    lastThread[method][j] = thread;
                } else if ((state[method][j] == 2) && (lastStateRow[j] == 1)
                        && (lastThread[method][j] != thread)) {
                    switches1to2Row[j] = true;
                    lastThread[method][j] = thread;
                } else if ((state[method][j] == 2) && (lastStateRow[j] == 2)
                        && (lastThread[method][j] != thread)) {
                    switchesAt2Row[j] = true;
                    lastThread[method][j] = thread;
                } else if ((state[method][j] == 1) && (lastStateRow[j] == 2)) {
                    lastThread[method][j] = thread;
                }
                // Update column with P event
                if ((state[j][method] == 1) && (lastStateCol[j] == 0)) {
                    lastThread[j][method] = thread;
                } else if ((state[j][method] == 1) && (lastStateCol[j] == 1)
                        && (lastThread[j][method] != thread)) {
                    switchesAt1Col[j] = true;
                    lastThread[j][method] = thread;
                } else if ((state[j][method] == 2) && (lastStateCol[j] == 1)
                        && (lastThread[j][method] != thread)) {
                    switches1to2Col[j] = true;
                    lastThread[j][method] = thread;
                } else if ((state[j][method] == 2) && (lastStateCol[j] == 2)
                        && (lastThread[j][method] != thread)) {
                    switchesAt2Col[j] = true;
                    lastThread[j][method] = thread;
                } else if ((state[j][method] == 1) && (lastStateCol[j] == 2)) {
                    lastThread[j][method] = thread;
                }
            }
        }
    }

    public void resetSwitches() {
        Arrays.fill(switchesAt1Row, false);
        Arrays.fill(switchesAt2Row, false);
        Arrays.fill(switches1to2Row, false);
        Arrays.fill(switchesAt1Col, false);
        Arrays.fill(switchesAt2Col, false);
        Arrays.fill(switches1to2Col, false);
    }
}