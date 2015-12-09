/*
 * SingleThreadAwareNormalOrStateMachine.java
 * Created on 2004-12-17
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.util.Arrays;

/**
 * @author Jinlin Yang
 */
public class SingleThreadAwareNormalOrStateMachine extends
        ThreadAwareNormalOrStateMachine {

    private ThreadAwareTraceReader tr;

    private int[][] switchesAt1;

    private int[][] switchesAt2;

    private int[][] switches1to2;

    /**
     * @param tr
     * @param patternCode
     * @param rule
     */
    public SingleThreadAwareNormalOrStateMachine(ThreadAwareTraceReader tr,
            int patternCode, byte[][] rule) {
        super(tr, patternCode, rule);
        this.tr = tr;
        switchesAt1 = new int[rows][cols];
        switchesAt2 = new int[rows][cols];
        switches1to2 = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            Arrays.fill(switchesAt1[i], 0);
            Arrays.fill(switchesAt2[i], 0);
            Arrays.fill(switches1to2[i], 0);
        }
    }

    /**
     * @see edu.virginia.cs.terracotta.inferenceengine.EventHandler#enterMethod(int)
     */
    public void enterMethod(int event) {
        super.enterMethod(event);

        int method = tr.getMethodCode(event);

        for (int j = 0; j < cols; j++) {
            if (j == method)
                continue;

            // Check if there is any switches when event is the S event
            if (switchesAt1Col[j])
                switchesAt1[method][j]++;
            if (switchesAt2Col[j])
                switchesAt2[method][j]++;
            if (switches1to2Col[j])
                switches1to2[method][j]++;
        }

        for (int j = 0; j < rows; j++) {
            int currCol = getColIndex(j, method);
            for (int i = 0; i < rows; i++) {
                if (i == method)
                    continue;
                // Check if there is any switches when event is the P event
                if (switchesAt1Row[i][j])
                    switchesAt1[i][currCol]++;
                if (switchesAt2Row[i][j])
                    switchesAt2[i][currCol]++;
                if (switches1to2Row[i][j])
                    switches1to2[i][currCol]++;
            }
        }

        // Reset the switches in each State Machine
        resetSwitches();
    }

    public int getPattern(int p, int s, int q) {
        return alive[p][getColIndex(s, q)] ? patternCode : 0;
    }

    public String collateResults(int p, int s, int q) {
        StringBuffer buf = new StringBuffer();
        buf.append(tr.getEvent(p) + "->" + tr.getEvent(s) + "|"
                + tr.getEvent(q));
        buf.append(" pFreq=" + tr.getEvent(p).getFreq() + " sFreq="
                + tr.getEvent(s).getFreq() + " qFreq="
                + tr.getEvent(q).getFreq());
        buf.append(" threadSwitchesAtState1=" + getSwitchesAt1(p, s, q)
                + " threadSwitchesAtState2=" + getSwitchesAt2(p, s, q)
                + " threadSwitchesFromState1toState2="
                + getSwitches1to2(p, s, q));
        return buf.toString();
    }

    public int getSwitchesAt1(int p, int s, int q) {
        return switchesAt1[p][getColIndex(s, q)];
    }

    public int getSwitchesAt2(int p, int s, int q) {
        return switchesAt2[p][getColIndex(s, q)];
    }

    public int getSwitches1to2(int p, int s, int q) {
        return switches1to2[p][getColIndex(s, q)];
    }
}