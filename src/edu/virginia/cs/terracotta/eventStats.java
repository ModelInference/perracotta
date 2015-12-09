/*
 * eventStats.java
 * Created on 2004-10-13
 */
package edu.virginia.cs.terracotta;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import edu.virginia.cs.terracotta.event.Event;

import gnu.getopt.Getopt;

/**
 * @author Jinlin Yang
 */
public class eventStats implements Processor {

    private String filename = null;

    private final String delim = "----";

    /**
     * @see edu.virginia.cs.terracotta.Processor#process()
     */
    public void process() {
        Hashtable event2lastTrace = new Hashtable();
        Vector visited = new Vector();

        long start = System.currentTimeMillis();

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filename)));
            int traceCount = 0;
            String line = null;
            while ((line = input.readLine()) != null) {
                if (line.startsWith("//"))
                    continue;
                if (line.equals(delim)) {
                    traceCount++;
                } else if (line.startsWith("Enter: ")) {
                    Event event = new Event(line.substring(7));
                    if (visited.contains(event)) {
                        int i = visited.indexOf(event);
                        ((Event) visited.get(i)).increaseFreq();
                    } else {
                        visited.add(event);
                        event.increaseFreq();
                    }
                    if (event2lastTrace.containsKey(event)) {
                        if (((Integer) event2lastTrace.get(event)).intValue() != traceCount) {
                            int i = visited.indexOf(event);
                            ((Event) visited.get(i)).increaseTraces();
                            event2lastTrace.put(event, new Integer(traceCount));
                        }
                    } else {
                        event2lastTrace.put(event, new Integer(traceCount));
                        event.increaseTraces();
                    }
                }
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();

        Collections.sort(visited);
        for (int i = 0; i < visited.size(); i++) {
            Event event = (Event) visited.get(i);
            System.out.println("freq=" + event.getFreq() + ", traces="
                    + event.getTraces() + ", event=" + event);
        }

        System.out.println("Total processing time: " + (end - start)/1000.0 + "s");
    }

    /**
     * @see edu.virginia.cs.terracotta.Processor#processOpts(java.lang.String[])
     */
    public void processOpts(String[] args) {
        int c;
        String arg;
        Getopt opt = new Getopt("Synthesizer", args, "f:");
        while ((c = opt.getopt()) != -1) {
            switch (c) {
            case 'f':
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

    /**
     * @see edu.virginia.cs.terracotta.Processor#usage()
     */
    public void usage() {
        System.out.println("eventStats");
        System.out.println("Required arguments:");
        System.out.println("-f filename\tThe input trace file");
        System.out.println("Optional arguments:");
    }

    public static void main(String[] args) {
        eventStats x = new eventStats();
        x.processOpts(args);
        x.process();
    }
}