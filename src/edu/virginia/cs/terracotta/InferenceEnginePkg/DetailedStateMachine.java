/*
 * DetailedStateMachine.java
 * Created on 2004-10-28
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.util.Arrays;

/**
 * @author Jinlin Yang
 */
public class DetailedStateMachine extends StateMachine {

    public static final byte[] clearBit = { -(0x1 + 1), -(0x2 + 1), -(0x4 + 1),
            -(0x8 + 1), -(0x10 + 1), -(0x20 + 1), -(0x40 + 1), 127 };

    public static final byte[] setBit = { 0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40,
            -128 };

    byte[][][] alive;

    int numberOfTraces;

    int currentTrace = 0;

    /**
     * @param tr
     * @param name
     * @param rule
     */
    public DetailedStateMachine(int types, int traces, int patternCode, byte[][] rule) {
        super(types, patternCode, rule);
        numberOfTraces = traces;
        alive = new byte[types][types][(int) Math.ceil(numberOfTraces / 8.0)];
        for (int i = 0; i < types; i++) {
            for (int j = 0; j < types; j++) {
                if (i != j) {
                    Arrays.fill(alive[i][j], (byte) -1);
                }
            }
        }
    }

    /**
     * @see edu.virginia.cs.terracotta.inferenceengine.EventHandler#processTrace()
     */
    public void processTrace() {
        for (int i = 0; i < types; i++) {
            for (int j = 0; j < types; j++) {
                if (i != j) {
                    // Check to see if machine is in final state
                    if (state[i][j] != 2) {
                        alive[i][j][currentTrace / 8] &= DetailedStateMachine.clearBit[currentTrace % 8];
                    }
                }
            }
        }
        currentTrace++;
        super.processTrace();
    }
}