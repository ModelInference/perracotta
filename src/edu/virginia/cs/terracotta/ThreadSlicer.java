/*
 * ThreadSlicer.java
 * Created on 2004-10-7
 */
package edu.virginia.cs.terracotta;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import gnu.getopt.Getopt;

/**
 * @author Jinlin Yang
 */
public class ThreadSlicer implements Processor {

    private String filename = null;

    private boolean threadComment = false;

    private final String delim = "----";

    public static void main(String[] args) {
        ThreadSlicer ts = new ThreadSlicer();
        ts.processOpts(args);
        ts.process();
    }

    public void process() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filename)));

            DataOutputStream output = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(filename
                            + ".threadslice")));

            String line = null;

            Vector methodtrace = new Vector();
            Vector threadtrace = new Vector();
            Vector threads = new Vector();
            Vector methods = new Vector();

            while ((line = input.readLine()) != null) {
                // filter comments lines
                if (line.startsWith("//"))
                    continue;
                if (line.equals(delim))
                    break;
                String[] parts = line.split(":Thread");
                assert (parts.length == 2);

                if (threads.contains(parts[1])) {
                    int index = threads.indexOf(parts[1]);
                    threadtrace.add(threads.get(index));
                } else {
                    threads.add(parts[1]);
                    threadtrace.add(threads.lastElement());
                }

                if (methods.contains(parts[0])) {
                    int index = methods.indexOf(parts[0]);
                    methodtrace.add(methods.get(index));
                } else {
                    methods.add(parts[0]);
                    methodtrace.add(methods.lastElement());
                }
            }

            while (!methodtrace.isEmpty()) {
                assert (methodtrace.size() == threadtrace.size());

                Collections.sort(threads);
                Vector[] thread2events = new Vector[threads.size()];
                for (int i = 0; i < thread2events.length; i++) {
                    thread2events[i] = new Vector();
                }

                for (int i = 0; i < threadtrace.size(); i++) {
                    String thread = (String) threadtrace.get(i);
                    String method = (String) methodtrace.get(i);

                    int k = threads.indexOf(thread);
                    thread2events[k].add(method);
                }

                for (int i = 0; i < thread2events.length; i++) {
                    String thread = (String) threads.get(i);
                    if (threadComment) {
                        output.writeBytes("//" + thread + "\n");
                    }
                    for (int j = 0; j < thread2events[i].size(); j++) {
                        output.writeBytes((String) thread2events[i].get(j));
                        output.writeBytes("\n");
                    }
                    output.writeBytes(delim + "\n");
                }

                methodtrace.clear();
                threadtrace.clear();
                threads.clear();
                methods.clear();
                while ((line = input.readLine()) != null) {
                    // filter comments lines
                    if (line.startsWith("//"))
                        continue;
                    if (line.equals(delim))
                        break;
                    String[] parts = line.split(":Thread");
                    assert (parts.length == 2);

                    if (threads.contains(parts[1])) {
                        int index = threads.indexOf(parts[1]);
                        threadtrace.add(threads.get(index));
                    } else {
                        threads.add(parts[1]);
                        threadtrace.add(threads.lastElement());
                    }

                    if (methods.contains(parts[0])) {
                        int index = methods.indexOf(parts[0]);
                        methodtrace.add(methods.get(index));
                    } else {
                        methods.add(parts[0]);
                        methodtrace.add(methods.lastElement());
                    }
                }
            }

            input.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processOpts(String[] args) {
        int c;
        Getopt opt = new Getopt("Synthesizer", args, "i:c");
        while ((c = opt.getopt()) != -1) {
            switch (c) {
            case 'i':
                filename = opt.getOptarg();
                break;
            case 'c':
                threadComment = true;
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
        System.out.println("ThreadSlicer");
        System.out.println("Required arguments:");
        System.out.println("-i filename\tThe input trace file");
        System.out.println("Optional arguments:");
        System.out
                .println("-c\tInsert the thread's identification before each trace");
    }
}