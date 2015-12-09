/*
 * StateMachine.java
 * Created on 2005-1-1
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg.twocausing;

import java.util.Arrays;

import edu.virginia.cs.terracotta.InferenceEnginePkg.EventHandler;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TripleStateMachine;

/**
 * @author Jinlin Yang
 */
public class StateMachine implements EventHandler {

    byte state[][];

    int types;

    int rows;

    int cols;

    byte rule[][];

    String name;

    int patternCode;

    /**
     *  
     */
    public StateMachine(int types, int patternCode, byte rule[][]) {
        this.types = types;
        this.rows = types * (types + 1) / 2;
        this.cols = types;
        state = new byte[this.rows][this.cols];
        this.rule = rule;
        this.patternCode = patternCode;
        this.name = TripleStateMachine.PATTERNNAMES[patternCode];
        for (int i = 0; i < state.length; i++)
            Arrays.fill(state[i], (byte) 0);
    }

    public String getName() {
        return name;
    }

    public int getPatternCode() {
        return patternCode;
    }

    /**
     * @see edu.virginia.cs.terracotta.InferenceEnginePkg.EventHandler#enterMethod(int)
     */
    public void enterMethod(int event) {
        for (int i = 0; i < rows; i++) {
            // Treat event as an S event. So we need to update the whole col
            state[i][event] = rule[state[i][event]][0];
        }
        // Treat event as a P event. So we need to update all rows
        // First try all Q events
        for (int k = 0; k < cols; k++) {
            // Compute the index for event|Q
            int currRow = getRowIndex(event, k);
            // then try all S events
            for (int j = 0; j < cols; j++) {
                if (j != event)
                    state[currRow][j] = rule[state[currRow][j]][1];
            }
        }
    }

    /**
     * @see edu.virginia.cs.terracotta.InferenceEnginePkg.EventHandler#leaveMethod(int)
     */
    public void leaveMethod(int event) {
        // TODO Auto-generated method stub

    }

    /**
     * @see edu.virginia.cs.terracotta.InferenceEnginePkg.EventHandler#processTrace()
     */
    public void processTrace() {
        for (int i = 0; i < state.length; i++)
            Arrays.fill(state[i], (byte) 0);
    }

    /**
     * @see edu.virginia.cs.terracotta.InferenceEnginePkg.EventHandler#processEnd()
     */
    public void processEnd() {
        // TODO Auto-generated method stub

    }

    public int getRowIndex(int A, int B) {
        int currRow = 0;
        if (A < B)
            currRow = (2 * cols - A + 1) * A / 2 + B - A;
        else
            currRow = (2 * cols - B + 1) * B / 2 + A - B;
        return currRow;
    }
}