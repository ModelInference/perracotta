/*
 * InferenceEngine3.java
 * Created on 2005-1-1
 */
package edu.virginia.cs.terracotta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import edu.virginia.cs.terracotta.InferenceEnginePkg.StateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TraceReader;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TripleStateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.twocausing.NormalStateMachine;

/**
 * @author Jinlin Yang
 */
public class InferenceEngine3 extends InferenceEngine {
    public void collateResults(TraceReader tr, NormalStateMachine nsm,
            String outputprefix) {
        try {
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputprefix
                            + "."
                            + TripleStateMachine.PATTERNNAMES[nsm
                                    .getPatternCode()])));
            for (int i = 0; i < types; i++) {
                for (int j = 0; j < types; j++) {
                    for (int k = j; k < types; k++) {
                        if (i == j || i == k || j == k)
                            continue;
                        if (nsm.getProperty(i, j, k)) {
                            StringBuffer buf = new StringBuffer();
                            buf.append(tr.getEvent(i) + "|" + tr.getEvent(j)
                                    + "->" + tr.getEvent(k));
                            buf.append(" pFreq=" + tr.getEvent(i).getFreq()
                                    + " qFreq=" + tr.getEvent(j).getFreq()
                                    + " sFreq=" + tr.getEvent(k).getFreq());
                            output.write(buf.toString());
                            output.write("\n");
                        }
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
        InferenceEngine3 en = new InferenceEngine3();
        en.processOpts(argv);
        en.process();
    }

    public void process() {
        try {
            long start = System.currentTimeMillis();

            TraceReader tr = new TraceReader(tracefilename, freqThreshold);
            NormalStateMachine nsm = new NormalStateMachine(tr.getEventTypes(),
                    7, StateMachine.ALTERNATING);
            addHandler(nsm);

            File tracefile = new File(tracefilename);
            printEvents(tr, tracefile.getName() + ".Events");

            types = tr.getEventTypes();

            long init = System.currentTimeMillis();
            tr.process(this);
            long end = System.currentTimeMillis();
            System.out.println("processing time = " + (end - init) / 1000.0
                    + "s");
            collateResults(tr, nsm, tracefile.getName());
            long output = System.currentTimeMillis();
            System.out.println("elapsed time = " + (output - start) / 1000.0
                    + "s");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}