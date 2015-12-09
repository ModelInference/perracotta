/*
 * ThreadAnalyzer.java
 * Created on 2004-9-27
 */
package edu.virginia.cs.terracotta;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import edu.virginia.cs.terracotta.event.MyThread;
import gnu.getopt.Getopt;

/**
 * @author Jinlin Yang
 */
public class ThreadAnalyzer implements Processor {

    private String filename = null;

    public static void main(String[] args) {
        ThreadAnalyzer ta = new ThreadAnalyzer();
        ta.processOpts(args);
        ta.process();
    }

    public void process() {
        HashSet visited = new HashSet();
        Hashtable table = new Hashtable();
        int count = 0;
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filename)));

            String line = null;
            String trace_id = "";
            while ((line = input.readLine()) != null) {
                //filter out comments
                if (line.startsWith("//")) {
                    trace_id += line;
                    continue;
                }

                //encounter trace delimiter
                if (line.equals("----")) {
                    count++;
                    //print out the thread count for this trace
                    System.out.println("Trace #" + count + ":" + trace_id
                            + " has " + visited.size() + " threads");
                    trace_id = "";
                    Iterator itr = visited.iterator();
                    while (itr.hasNext()) {
                        System.out.println("\t"
                                + ((MyThread) itr.next()).getName());
                    }
                    //clear the visited set
                    visited.clear();

                    continue;
                }

                String[] parts = line.split(":Thread");
                if (parts.length == 2) {
                    MyThread e = new MyThread("Thread" + parts[1]);
                    visited.add(e);
                    if (!table.containsKey(e)) {
                        HashSet traceSet = new HashSet();
                        traceSet.add(new Integer(count + 1));
                        table.put(e, traceSet);
                    } else {
                        ((HashSet) table.get(e)).add(new Integer(count + 1));
                    }
                }
            }

            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Set threadSet = table.keySet();
        Iterator itr = threadSet.iterator();
        while (itr.hasNext()) {
            MyThread e = (MyThread) itr.next();
            HashSet traceSet = (HashSet) table.get(e);
            if (traceSet.size() >= 1) {
                System.out.print(e + " occurs in traces: ");
                Iterator itr2 = traceSet.iterator();
                while (itr2.hasNext())
                    System.out.print(itr2.next() + ", ");
                System.out.println();
            }
        }
    }

    public void processOpts(String[] args) {
        int c;
        String arg;
        Getopt opt = new Getopt("Synthesizer", args, "i:");
        while ((c = opt.getopt()) != -1) {
            switch (c) {
            case 'i':
                filename = opt.getOptarg();
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
        if (filename == null) {
            usage();
            System.exit(1);
        }
    }

    public void usage() {
        System.out.println("ThreadAnalyzer");
        System.out.println("Required arguments:");
        System.out.println("-i tracefilename\tThe input trace file");
        System.out.println("Currently there is no optional arguments");
    }
}