/*
 * ThreadAwareTraceReader.java
 * Created on 2004-10-29
 */
package edu.virginia.cs.terracotta.InferenceEnginePkg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import edu.virginia.cs.terracotta.CFG.MyMethod;
import edu.virginia.cs.terracotta.event.Event;
import edu.virginia.cs.terracotta.event.MyThread;

/**
 * @author Jinlin Yang
 */
public class ThreadAwareTraceReader extends TraceReader {

    private Vector methods = new Vector();

    private Vector threads = new Vector();

    private Map event2method = new HashMap();

    private Map event2thread = new HashMap();

    private int freqThreshold;

    private boolean eventAnalysis;

    public ThreadAwareTraceReader(String filename, String delim)
            throws IOException {
        super(filename, delim);
        this.freqThreshold = 0;
        init(filename);
    }

    public ThreadAwareTraceReader(String filename) throws IOException {
        super(filename);
        this.freqThreshold = 0;
        init(filename);
    }

    /**
     * @param filename
     * @param freqThreshold
     * @throws IOException
     */
    public ThreadAwareTraceReader(String filename, int freqThreshold,
            boolean eventAnalysis) throws IOException {
        super(filename);
        this.freqThreshold = freqThreshold;
        this.eventAnalysis = eventAnalysis;
        init(filename);
    }

    private void init(String filename) throws IOException {
        String line = null;
        HashMap method2lastTrace = new HashMap();
        Vector visited = new Vector();
        int traceCount = 0;

        // The purpose for re-scan the trace file again is for indentifying how
        // many traces in which a method appears. In the meantime, we also count
        // the absolute frequency of a method.
        while ((line = file.readLine()) != null) {
            // filter out comments
            if (line.startsWith("//"))
                continue;
            if (line.matches("^$"))
                continue;
            
            if (line.startsWith(delim)) {
                traceCount++;
            } else {
                int mode;
                String event_name;
                if (line.startsWith("Enter: ")) {
                    event_name = line.substring(7);
                    mode = 0;
                } else if (line.startsWith("Exit: ")) {
                    event_name = line.substring(6);
                    mode = 1;
                } else if (line.startsWith("Error: ")) {
                    event_name = line.substring(7);
                    mode = 2;
                } else {
                    System.err.println("cannot parse: " + line);
                    event_name = line;
                    mode = 3;
                }
                if (mode == 0) {
                    int k = events.indexOf(new Event(event_name));
                    Event event = (Event) events.get(k);
                    MyMethod method = event.getMethod();
                    if (!visited.contains(method)) {
                        method.increaseFreq();
                        method.increaseTraces();
                        visited.add(method);
                        method2lastTrace.put(method, new Integer(traceCount));
                    } else {
                        int l = visited.indexOf(method);
                        method = (MyMethod) visited.get(l);
                        method.increaseFreq();
                        if (((Integer) method2lastTrace.get(method)).intValue() != traceCount) {
                            method.increaseTraces();
                            method2lastTrace.put(method,
                                    new Integer(traceCount));
                        }
                    }
                }
            }
        }
        file.close();
        this.file = new BufferedReader(new FileReader(filename));

        // Only copy those methods whose frequencies are above the threshold to
        // the 'methods' Vector.
        for (int i = 0; i < visited.size(); i++) {
            MyMethod m = (MyMethod) visited.get(i);
            if (m.getFreq() > freqThreshold)
                methods.add(m);
        }

        // Construct the event2method and event2thread mappings.
        for (int i = 0; i < events.size(); i++) {
            Event event = (Event) events.get(i);
            MyMethod method = event.getMethod();
            MyThread thread = event.getThread();

            if (methods.contains(method)) {
                event2method.put(new Integer(i), new Integer(methods
                        .indexOf(method)));
                if (!threads.contains(thread))
                    threads.add(thread);
                event2thread.put(new Integer(i), new Integer(threads
                        .indexOf(thread)));
            }
            // When the frequency of an event does not exceed the threshold, it
            // will be removed from the events list.
            else {
                events.remove(i);
                i--;
            }
        }

        if (eventAnalysis) {
            doEventAnalysis();
        }
    }

    private Vector doEventAnalysis() {
        Vector ret = new Vector();
        for (int i = 0; i < methods.size(); i++) {
            MyMethod m_i = (MyMethod) methods.get(i);
            int freq_i = m_i.getFreq();
            for (int j = i + 1; j < methods.size(); j++) {
                MyMethod m_j = (MyMethod) methods.get(j);
                int freq_j = m_j.getFreq();
                if (freq_j == freq_i)
                    continue;
                for (int k = 0; k < methods.size(); k++) {
                    if ((k == i) || (k == j))
                        continue;
                    MyMethod m_k = (MyMethod) methods.get(k);
                    int freq_k = m_k.getFreq();
                    if (freq_k == (freq_i + freq_j)) {
                        Vector pair = new Vector();
                        pair.add(new Integer(i));
                        pair.add(new Integer(j));
                        ret.add(pair);
                        break;
                    }
                }
            }
        }

        if (ret.size() > 0) {
            for (int i = 0; i < ret.size(); i++) {
                Vector pair = (Vector) ret.get(i);
                int first = ((Integer) pair.get(0)).intValue();
                int second = ((Integer) pair.get(1)).intValue();
                MyMethod m1 = (MyMethod) methods.get(first);
                MyMethod m2 = (MyMethod) methods.get(second);
                System.out.println(m1 + "(" + m1.getFreq() + ")" + " == " + m2
                        + "(" + m2.getFreq() + ")");
            }
        }

        return ret;
    }

    public int getMethodCode(int eventCode) {
        return ((Integer) event2method.get(new Integer(eventCode))).intValue();
    }

    public int getThreadCode(int eventCode) {
        return ((Integer) event2thread.get(new Integer(eventCode))).intValue();
    }

    public int getEventTypes() {
        return methods.size();
    }

    /**
     * @see edu.virginia.cs.terracotta.inferenceengine.TraceReader#getEvent(int)
     */
    public Event getEvent(int eventCode) {
        return new Event((MyMethod) methods.get(eventCode));
    }
}