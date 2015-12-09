/*
 * NormalOrStateMachine.java
 * Created on 2004-12-17
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.util.Arrays;

/**
 * @author Jinlin Yang
 */
public class NormalOrStateMachine extends OrStateMachine {

    boolean alive[][];

    int total = 0;

    /**
     * @param types
     * @param patternCode
     * @param rule
     */
    public NormalOrStateMachine(int types, int patternCode, byte[][] rule) {
        super(types, patternCode, rule);
        alive = new boolean[rows][cols];
        total = rows * cols;
        for (int i = 0; i < rows; i++)
            Arrays.fill(alive[i], true);
    }

    /**
     * @see edu.virginia.cs.terracotta.inferenceengine.EventHandler#processTrace()
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

    public boolean getProperty(int p, int s, int q) {
        return alive[p][getColIndex(s, q)];
    }
}