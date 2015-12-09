// import java.io.*;
package edu.virginia.cs.terracotta;

import edu.virginia.cs.terracotta.InferenceEnginePkg.EventHandler;
import edu.virginia.cs.terracotta.InferenceEnginePkg.ProbHeuristic;
import edu.virginia.cs.terracotta.InferenceEnginePkg.RawDistance;
import edu.virginia.cs.terracotta.InferenceEnginePkg.ScoringHeuristic;
import edu.virginia.cs.terracotta.InferenceEnginePkg.SingleThreadAwareNormalStateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.StateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.ThreadAwareTraceReader;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TraceReader;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TreeDistance;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TripleDetailedStateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TripleNormalStateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TripleStateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.TripleThreadAwareNormalStateMachine;
import edu.virginia.cs.terracotta.InferenceEnginePkg.prob.OneMonitorAndTripleStateMachine;
import gnu.getopt.Getopt;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

public class InferenceEngine implements ScoringHeuristic, Processor {

    boolean rawdist = false;

    boolean treedist = false;

    boolean prob = false;

    boolean detailedMode = false;

    boolean threadAwareMode = false;

    int freqThreshold = 0;

    String tracefilename = null;

    int singlePatternCode = 0;

    boolean singleStateMachineMode = false;

    boolean eventAnalysisMode = false;

    boolean approximationMode = false;

    double scoreThreshold = 0.0;

    List handlers = new ArrayList();

    List heuristics = new ArrayList();

    int types = 0;

    public static boolean printFreq = false;

    public void setTypes(int types) {
        this.types = types;
    }

    public void addHandler(EventHandler e) {
        handlers.add(e);
    }

    public void addHeuristic(ScoringHeuristic h) {
        handlers.add(h);
        heuristics.add(h);
    }

    public void enterMethod(int event) {
        for (Iterator i = handlers.iterator(); i.hasNext();) {
            ((EventHandler) i.next()).enterMethod(event);
        }
    }

    public void leaveMethod(int event) {
        for (Iterator i = handlers.iterator(); i.hasNext();) {
            ((EventHandler) i.next()).leaveMethod(event);
        }
    }

    public void processTrace() {
        for (Iterator i = handlers.iterator(); i.hasNext();) {
            ((EventHandler) i.next()).processTrace();
        }
    }

    public void processEnd() {
        for (Iterator i = handlers.iterator(); i.hasNext();) {
            ((EventHandler) i.next()).processEnd();
        }
    }

    public String getScores(int p, int s) {
        StringBuffer foo = new StringBuffer();
        for (Iterator i = heuristics.iterator(); i.hasNext();) {
            foo.append(((ScoringHeuristic) i.next()).getScores(p, s));
        }
        return foo.toString();
    }

    public void collateResults(TripleStateMachine tsm, String outputprefix) {

        BufferedWriter[] outputs = new BufferedWriter[TripleStateMachine.PATTERNS + 1];
        try {
            for (int i = 0; i < types; i++) {
                for (int j = 0; j < types; j++) {
                    if (i != j) {
                        int code = tsm.getPattern(i, j);
                        if (code == TripleStateMachine.NA)
                            continue;
                        if (outputs[code] == null) {
                            outputs[code] = new BufferedWriter(
                                    new OutputStreamWriter(
                                            new FileOutputStream(
                                                    outputprefix
                                                            + "."
                                                            + TripleStateMachine.PATTERNNAMES[code])));
                        }
                        outputs[code].write(tsm.collateResults(i, j) + " "
                                + getScores(i, j));
                        outputs[code].write("\n");
                    }
                }
            }
            for (int i = 0; i < outputs.length; i++) {
                if (outputs[i] != null)
                    outputs[i].close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void collateResults(SingleThreadAwareNormalStateMachine stansm,
            String outputprefix) {
        try {
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputprefix
                            + "."
                            + TripleStateMachine.PATTERNNAMES[stansm
                                    .getPatternCode()])));
            for (int i = 0; i < types; i++) {
                for (int j = 0; j < types; j++) {
                    if (i != j) {
                        int code = stansm.getPattern(i, j);
                        if (code == TripleStateMachine.NA)
                            continue;
                        output.write(stansm.collateResults(i, j) + " "
                                + getScores(i, j));
                        output.write("\n");
                    }
                }
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void collateResults(OneMonitorAndTripleStateMachine mTSM,
            String outputprefix) {
        try {
            Vector results = new Vector();
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputprefix + ".appro")));
            for (int i = 0; i < types; i++) {
                for (int j = i + 1; j < types; j++) {
                    double AltScore_ij = mTSM.getStat(i, j,
                            TripleStateMachine.ALTERNATING);
                    double AltScore_ji = mTSM.getStat(j, i,
                            TripleStateMachine.ALTERNATING);

                    int p;
                    int s;
                    double AltScore;
                    if (AltScore_ij >= AltScore_ji) {
                        AltScore = AltScore_ij;
                        p = i;
                        s = j;
                    } else {
                        AltScore = AltScore_ji;
                        p = j;
                        s = i;
                    }
                    if (AltScore > scoreThreshold) {
                        // results.add(mTSM.getScore(p, s)
                        // + " "
                        // + mTSM.getStat(p, s,
                        // TripleStateMachine.ALTERNATING)
                        // + " "
                        // + mTSM.getStat(p, s,
                        // TripleStateMachine.MULTI_EFFECT)
                        // + " "
                        // + mTSM.getStat(p, s,
                        // TripleStateMachine.MULTI_CAUSE)
                        // + " "
                        // + mTSM.getStat(p, s,
                        // TripleStateMachine.EFFECT_FIRST)
                        // + " "
                        // + mTSM.getStat(p, s,
                        // TripleStateMachine.ONE_CAUSE)
                        // + " "
                        // + mTSM.getStat(p, s,
                        // TripleStateMachine.ONE_EFFECT)
                        // + " "
                        // + mTSM.getStat(p, s,
                        // TripleStateMachine.CAUSE_FIRST) + " "
                        // + mTSM.getStat(p, s, TripleStateMachine.NA)
                        // + " " + mTSM.collateResults(p, s) + "\n");
                        results.add(mTSM.getStat(p, s,
                                TripleStateMachine.ALTERNATING)
                                + " " + mTSM.collateResults(p, s) + "\n");
                    }

                }
            }
            Collections.sort(results);

//            output.write("Score pAL pME pMC pEF pOC pOE pCF pNA\n");
            output.write("pAL Event1->Event2 pFreq sFreq\n");
            for (int i = 0; i < results.size(); i++) {
                output.write(((String) results.get(i)));
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printEvents(TraceReader tr, String outputfilename) {
        try {
            Vector events = new Vector();
            for (int i = 0; i < tr.getEventTypes(); i++) {
                if (printFreq) {
                    events.add(tr.getEvent(i).toString() + " "
                            + tr.getEvent(i).getFreq() + " "
                            + tr.getEvent(i).getTraces());
                } else {
                    events.add(tr.getEvent(i).toString());
                }
            }
            Collections.sort(events);

            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputfilename)));
            output.write("Event Freq Traces");
            output.write("\n");
            for (int i = 0; i < events.size(); i++) {
                output.write((String) events.get(i));
                output.write("\n");
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String argv[]) {
        InferenceEngine en = new InferenceEngine();
        en.processOpts(argv);
        en.process();
    }

    private void runSingleFSM() throws IOException {
        TraceReader tr = new ThreadAwareTraceReader(tracefilename,
                freqThreshold, eventAnalysisMode);
        SingleThreadAwareNormalStateMachine stansm = new SingleThreadAwareNormalStateMachine(
                (ThreadAwareTraceReader) tr, singlePatternCode,
                StateMachine.ALTERNATING);
        addHandler(stansm);

        File tracefile = new File(tracefilename);
        printEvents(tr, tracefile.getName() + ".Events");

        types = tr.getEventTypes();

        long init = System.currentTimeMillis();
        tr.process(this);
        long end = System.currentTimeMillis();
        System.out.println("processing time = " + (end - init) / 1000.0 + "s");
        collateResults(stansm, tracefile.getName());
    }

    private void runTriFSM() throws IOException {
        TraceReader tr;
        TripleStateMachine tsm = null;
        if (detailedMode) {
            tr = new TraceReader(tracefilename, freqThreshold);
            tsm = new TripleDetailedStateMachine(tr);
        } else if (threadAwareMode) {
            tr = new ThreadAwareTraceReader(tracefilename, freqThreshold,
                    eventAnalysisMode);
            tsm = new TripleThreadAwareNormalStateMachine(
                    (ThreadAwareTraceReader) tr);
        } else {
            tr = new TraceReader(tracefilename, freqThreshold);
            tsm = new TripleNormalStateMachine(tr);
        }
        addHandler(tsm);
        File tracefile = new File(tracefilename);
        printEvents(tr, tracefile.getName() + ".Events");

        types = tr.getEventTypes();

        if (treedist) {
            TreeDistance td = new TreeDistance(tr);
            addHeuristic(td);
        }
        if (rawdist) {
            RawDistance rd = new RawDistance(tr);
            addHeuristic(rd);
        }
        if (prob && !detailedMode) {
            ProbHeuristic ph = new ProbHeuristic(tr,
                    (TripleNormalStateMachine) tsm);
            addHeuristic(ph);
        }

        long init = System.currentTimeMillis();
        tr.process(this);
        long end = System.currentTimeMillis();
        System.out.println("processing time = " + (end - init) / 1000.0 + "s");
        collateResults(tsm, tracefile.getName());
    }

    private void runApproximation() throws IOException {
        TraceReader tr;
        tr = new TraceReader(tracefilename, freqThreshold);
        OneMonitorAndTripleStateMachine mTSM = new OneMonitorAndTripleStateMachine(
                tr);
        addHandler(mTSM);

        File tracefile = new File(tracefilename);
        printEvents(tr, tracefile.getName() + ".Events");

        types = tr.getEventTypes();

        long init = System.currentTimeMillis();
        tr.process(this);
        long end = System.currentTimeMillis();
        System.out.println("processing time = " + (end - init) / 1000.0 + "s");
        collateResults(mTSM, tracefile.getName());
    }

    public void process() {
        try {
            long start = System.currentTimeMillis();
            if (singleStateMachineMode) {
                runSingleFSM();
            } else if (approximationMode) {
                runApproximation();
            } else {
                runTriFSM();
            }

            long output = System.currentTimeMillis();
            System.out.println("elapsed time = " + (output - start) / 1000.0
                    + "s");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processOpts(String[] args) {
        int c;
        String arg;
        Getopt opt = new Getopt("Synthesizer", args, "a:l:i:s:rptdecf");
        while ((c = opt.getopt()) != -1) {
            switch (c) {
            case 'a':
                approximationMode = true;
                scoreThreshold = Double.parseDouble(opt.getOptarg());
                break;
            case 'c':
                eventAnalysisMode = true;
                break;
            case 'd':
                detailedMode = true;
                break;
            case 'e':
                threadAwareMode = true;
                break;
            case 'f':
                printFreq = true;
                break;
            case 'i':
                tracefilename = opt.getOptarg();
                break;
            case 'l':
                freqThreshold = Integer.parseInt(opt.getOptarg());
                break;
            case 'p':
                prob = true;
                break;
            case 'r':
                rawdist = true;
                break;
            case 's':
                singleStateMachineMode = true;
                singlePatternCode = Integer.parseInt(opt.getOptarg());
                break;
            case 't':
                treedist = true;
                break;
            case '?':
                System.err.println("Invalid option: " + c);
                usage();
                System.exit(1);
                break;
            default:
                System.out.println("getopt() returned " + c);
                break;
            }
        }

        if ((scoreThreshold < 0.0) || (scoreThreshold > 3.0)) {
            System.err
                    .println("Invalid scoreThreshold. Must be between 0.0 and 3.0");
            usage();
            System.exit(1);
        }

        if (tracefilename == null || freqThreshold < 0) {
            usage();
            System.exit(1);
        }
        if (detailedMode && threadAwareMode && approximationMode) {
            System.err
                    .println("Currently does not support more than one of"
                            + " detailed, thread-aware, or approximation modes simultaneously");
            System.exit(1);
        }
    }

    public void usage() {
        System.out.println("Inference Engine");
        System.out.println("Required arguments:");
        System.out.println("-i filename\tThe input trace file");
        System.out.println("Optional arguments:");
        System.out
                .println("-a double_number\tturn on the probabilistic approximation mode");
        System.out.println("-d\tturn on the detailed mode which prints out"
                + " how much percent of the traces satisfy each of the eight"
                + " patterns");
        System.out.println("-e\tturn on the thread-aware mode");
        System.out
                .println("-f\tprint out the frequency of events (default is false)");
        System.out.println("-l int\tset the minimum frequency of an event");
        System.out.println("-t\tEnable measure of tree distance");
        System.out.println("-r\tEnable measure of raw distance");
        System.out.println("-p\tEnable measure of probability");
        System.out.println("-s patternCode\tEnable single state machine mode");
    }
}