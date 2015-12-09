/*
 * Analyzer.java
 * Created on 2004-9-19
 */
package edu.virginia.cs.terracotta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import edu.virginia.cs.terracotta.CFG.MyMethod;
import edu.virginia.cs.terracotta.event.Event;
import edu.virginia.cs.terracotta.event.MyThread;

/**
 * @author Jinlin Yang
 */
public class Analyzer {

    public static void main(String[] args) {
        Analyzer analyzer = new Analyzer();
        analyzer.process(args);
    }

    private void process(String[] args) {
        if (args.length != 1) {
            usage();
        }
        Vector events = parse(args[0]);
        //        for (int i = 0; i < events.size(); i++)
        //            System.out.println((Event) events.get(i));
        Hashtable thread2methods = collectThreadStats(events);
        System.out.println("Total number of threads: " + thread2methods.size());
        Enumeration threads = thread2methods.keys();
        while (threads.hasMoreElements()) {
            String thread = (String) threads.nextElement();
            Vector methods = (Vector) thread2methods.get(thread);
            System.out.println("#### Thread \"" + thread + "\" has "
                    + methods.size() + " method events");
            for (int i = 0; i < methods.size(); i++)
                System.out.println("\t" + (MyMethod) methods.get(i));
        }
    }

    private Hashtable collectThreadStats(Vector events) {
        Hashtable thread2methods = new Hashtable();
        for (int i = 0; i < events.size(); i++) {
            Event e = (Event) events.get(i);
            MyThread thread = e.getThread();
            MyMethod method = e.getMethod();

            if (!thread2methods.containsKey(thread)) {
                Vector methods = new Vector();
                methods.add(method);
                thread2methods.put(thread, methods);
            } else {
                Vector methods = (Vector) thread2methods.get(thread);
                methods.add(method);
                thread2methods.put(thread, methods);
            }
        }
        return thread2methods;
    }

    private void usage() {
        System.out.println("Analyzer eventsFileName");
    }

    private Vector parse(String filename) {
        Vector events = new Vector();
        try {
            BufferedReader file = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = file.readLine()) != null) {
                if (line.startsWith("//"))
                    continue;
                Event e = new Event(line);
                events.add(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return events;
    }
}