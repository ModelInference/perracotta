/*
 * OrStateMachine.java
 * Created on 2004-12-16
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.util.Arrays;

/**
 * @author Jinlin Yang
 */
public class OrStateMachine implements EventHandler {
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
    public OrStateMachine(int types, int patternCode, byte rule[][]) {
        this.types = types;
        this.rows = types;
        this.cols = types * (types + 1) / 2;
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
     * @see edu.virginia.cs.terracotta.inferenceengine.EventHandler#enterMethod(int)
     */
    public void enterMethod(int event) {
        for (int j = 0; j < cols; j++) {
            // Update row with S event
            state[event][j] = rule[state[event][j]][1];
        }
        for (int j = 0; j < rows; j++) {
            int currCol = getColIndex(j, event);
            for (int i = 0; i < rows; i++) {
                // Update column with P event
                // Avoid updating the elements on the ith row twice
                if (i != event)
                    state[i][currCol] = rule[state[i][currCol]][0];
            }
        }
    }

    /**
     * @see edu.virginia.cs.terracotta.inferenceengine.EventHandler#leaveMethod(int)
     */
    public void leaveMethod(int event) {
        // TODO Auto-generated method stub

    }

    /**
     * @see edu.virginia.cs.terracotta.inferenceengine.EventHandler#processTrace()
     */
    public void processTrace() {
        for (int i = 0; i < state.length; i++)
            Arrays.fill(state[i], (byte) 0);
    }

    /**
     * @see edu.virginia.cs.terracotta.inferenceengine.EventHandler#processEnd()
     */
    public void processEnd() {
        // TODO Auto-generated method stub

    }

    public int getColIndex(int A, int B) {
        int currCol = 0;
        if (A < B)
            currCol = (2 * rows - A + 1) * A / 2 + B - A;
        else
            currCol = (2 * rows - B + 1) * B / 2 + A - B;
        return currCol;
    }
}