/*
 * InferenceEngine2.java
 * Created on 2004-12-17
 */
package edu.virginia.cs.terracotta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import edu.virginia.cs.terracotta.InferenceEnginePkg.SingleThreadAwareNormalOrStateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.StateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.ThreadAwareTraceReader;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TraceReader;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TripleStateMachine;

/**
 * @author Jinlin Yang
 */
public class InferenceEngine2 extends InferenceEngine {

    public void collateResults(SingleThreadAwareNormalOrStateMachine stanosm,
            String outputprefix) {
        try {
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputprefix
                            + "."
                            + TripleStateMachine.PATTERNNAMES[stanosm
                                    .getPatternCode()])));
            for (int i = 0; i < types; i++) {
                for (int j = 0; j < types; j++) {
                    for (int k = j; k < types; k++) {
                        if(i==j || i==k || j==k)
                            continue;
                        int code = stanosm.getPattern(i, j, k);
                        if (code == TripleStateMachine.NA)
                            continue;
                        output.write(stanosm.collateResults(i, j, k));
                        output.write("\n");
                    }
                }
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printEvents(TraceReader tr, String outputfilename) {
        try {
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputfilename)));
            output.write("//Total events: " + tr.getEventTypes());
            output.write("\n");
            for (int i = 0; i < tr.getEventTypes(); i++) {
                output.write(tr.getEvent(i).toString() + " Freq="
                        + tr.getEvent(i).getFreq() + " Traces="
                        + tr.getEvent(i).getTraces());
                output.write("\n");
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String argv[]) {
        InferenceEngine2 en = new InferenceEngine2();
        en.processOpts(argv);
        en.process();
    }

    public void process() {
        try {
            long start = System.currentTimeMillis();

            ThreadAwareTraceReader tr = new ThreadAwareTraceReader(
                    tracefilename, freqThreshold, eventAnalysisMode);
            SingleThreadAwareNormalOrStateMachine stanosm = new SingleThreadAwareNormalOrStateMachine(
                    tr, 7, StateMachine.ALTERNATING);
            addHandler(stanosm);

            File tracefile = new File(tracefilename);
            printEvents(tr, tracefile.getName() + ".Events");

            types = tr.getEventTypes();

            long init = System.currentTimeMillis();
            tr.process(this);
            long end = System.currentTimeMillis();
            System.out.println("processing time = " + (end - init) / 1000.0
                    + "s");
            collateResults(stanosm, tracefile.getName());
            long output = System.currentTimeMillis();
            System.out.println("elapsed time = " + (output - start) / 1000.0
                    + "s");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}