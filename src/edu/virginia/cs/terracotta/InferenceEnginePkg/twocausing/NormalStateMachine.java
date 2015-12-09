/*
 * NormalStateMachine.java
 * Created on 2005-1-1
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg.twocausing;

import java.util.Arrays;

/**
 * @author Jinlin Yang
 */
public class NormalStateMachine extends StateMachine {
    boolean alive[][];

    int total = 0;

    /**
     * @param types
     * @param patternCode
     * @param rule
     */
    public NormalStateMachine(int types, int patternCode, byte[][] rule) {
        super(types, patternCode, rule);
        alive = new boolean[rows][cols];
        total = rows * cols;
        for (int i = 0; i < rows; i++)
            Arrays.fill(alive[i], true);
    }

    /**
     * @see edu.virginia.cs.terracotta.InferenceEnginePkg.EventHandler#processTrace()
     */
    public void processTrace() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Check to see if machine is in final state
                if (alive[i][j] && state[i][j] % 2 == 1) {
                    alive[i][j] = false;
                    total--;
                }
            }
        }
        super.processTrace();
    }

    public boolean getProperty(int p, int q, int s) {
        return alive[getRowIndex(p, q)][s];
    }
}