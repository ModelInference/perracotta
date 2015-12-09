/*
 * ThreadAwareNormalOrStateMachine.java
 * Created on 2004-12-17
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.util.Arrays;

/**
 * @author Jinlin Yang
 */
public class ThreadAwareNormalOrStateMachine extends NormalOrStateMachine {

    private ThreadAwareTraceReader tr;

    private int[][] lastThread;

    boolean[][] switchesAt1Row;

    boolean[] switchesAt1Col;

    boolean[][] switchesAt2Row;

    boolean[] switchesAt2Col;

    boolean[][] switches1to2Row;

    boolean[] switches1to2Col;

    private byte[][] lastStateRow;

    private byte[] lastStateCol;

    /**
     * @param types
     * @param patternCode
     * @param rule
     */
    public ThreadAwareNormalOrStateMachine(ThreadAwareTraceReader tr,
            int patternCode, byte[][] rule) {
        super(tr.getEventTypes(), patternCode, rule);
        this.tr = tr;
        lastThread = new int[rows][cols];
        switchesAt1Row = new boolean[rows][rows];
        switchesAt1Col = new boolean[cols];
        switchesAt2Row = new boolean[rows][rows];
        switchesAt2Col = new boolean[cols];
        switches1to2Row = new boolean[rows][rows];
        switches1to2Col = new boolean[cols];
        lastStateRow = new byte[rows][rows];
        lastStateCol = new byte[cols];
        for (int i = 0; i < rows; i++) {
            Arrays.fill(lastThread[i], -1);
            Arrays.fill(switchesAt1Row[i], false);
            Arrays.fill(switchesAt2Row[i], false);
            Arrays.fill(switches1to2Row[i], false);
            Arrays.fill(lastStateRow[i], (byte) 0);
        }
        Arrays.fill(switchesAt1Col, false);
        Arrays.fill(switchesAt2Col, false);
        Arrays.fill(switches1to2Col, false);
        Arrays.fill(lastStateCol, (byte) 0);
    }

    /**
     * @see edu.virginia.cs.terracotta.inferenceengine.EventHandler#enterMethod(int)
     */
    public void enterMethod(int event) {
        int method = tr.getMethodCode(event);
        int thread = tr.getThreadCode(event);
        for (int j = 0; j < rows; j++) {
            if (j != method)
                lastStateCol[j] = state[j][method];
            int currCol = getColIndex(j, method);
            for (int i = 0; i < rows; i++) {
                if (i != method)
                    lastStateRow[i][j] = state[i][currCol];
            }
        }

        super.enterMethod(method);

        for (int j = 0; j < cols; j++) {
            if (j != method) {
                // Update row with S event
                if ((state[method][j] == 1) && (lastStateCol[j] == 0)) {
                    lastThread[method][j] = thread;
                } else if ((state[method][j] == 1) && (lastStateCol[j] == 1)
                        && (lastThread[method][j] != thread)) {
                    switchesAt1Col[j] = true;
                    lastThread[method][j] = thread;
                } else if ((state[method][j] == 2) && (lastStateCol[j] == 1)
                        && (lastThread[method][j] != thread)) {
                    switches1to2Col[j] = true;
                    lastThread[method][j] = thread;
                } else if ((state[method][j] == 2) && (lastStateCol[j] == 2)
                        && (lastThread[method][j] != thread)) {
                    switchesAt2Col[j] = true;
                    lastThread[method][j] = thread;
                } else if ((state[method][j] == 1) && (lastStateCol[j] == 2)) {
                    lastThread[method][j] = thread;
                }
            }
        }
        for (int j = 0; j < rows; j++) {
            int currCol = getColIndex(j, method);
            for (int i = 0; i < rows; i++) {
                if (i != method) {
                    // Update column with P event
                    if ((state[i][currCol] == 1) && (lastStateRow[i][j] == 0)) {
                        lastThread[i][currCol] = thread;
                    } else if ((state[i][currCol] == 1)
                            && (lastStateRow[i][j] == 1)
                            && (lastThread[i][currCol] != thread)) {
                        switchesAt1Row[i][j] = true;
                        lastThread[i][currCol] = thread;
                    } else if ((state[i][currCol] == 2)
                            && (lastStateRow[i][j] == 1)
                            && (lastThread[i][currCol] != thread)) {
                        switches1to2Row[i][j] = true;
                        lastThread[i][currCol] = thread;
                    } else if ((state[i][currCol] == 2)
                            && (lastStateRow[i][j] == 2)
                            && (lastThread[i][currCol] != thread)) {
                        switchesAt2Row[i][j] = true;
                        lastThread[i][currCol] = thread;
                    } else if ((state[i][currCol] == 1)
                            && (lastStateRow[i][j] == 2)) {
                        lastThread[i][currCol] = thread;
                    }
                }
            }
        }
    }
    
    public void resetSwitches(){
        for (int i = 0; i < rows; i++) {
            Arrays.fill(switchesAt1Row[i], false);
            Arrays.fill(switchesAt2Row[i], false);
            Arrays.fill(switches1to2Row[i], false);
        }
        Arrays.fill(switchesAt1Col, false);
        Arrays.fill(switchesAt2Col, false);
        Arrays.fill(switches1to2Col, false);
    }
}