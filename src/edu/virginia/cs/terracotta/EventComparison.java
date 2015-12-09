/*
 * EventComparison.java
 * Created on 2004-10-24
 */
package edu.virginia.cs.terracotta;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
 * @author Jinlin Yang
 */
public class EventComparison implements Processor {

    private String filename1 = null;

    private String filename2 = null;

    /**
     * @see edu.virginia.cs.terracotta.Processor#process()
     */
    public void process() {
        try {
            File f1 = new File(filename1);
            File f2 = new File(filename2);
            BufferedReader input1 = new BufferedReader(new InputStreamReader(
                    new FileInputStream(f1)));
            BufferedReader input2 = new BufferedReader(new InputStreamReader(
                    new FileInputStream(f2)));

            LinkedList chain1 = new LinkedList();
            LinkedList chain2 = new LinkedList();
            String line = null;
            while ((line = input1.readLine()) != null) {
                if (line.startsWith("//"))
                    continue;
                chain1.add(line);
            }
            while ((line = input2.readLine()) != null) {
                if (line.startsWith("//"))
                    continue;
                chain2.add(line);
            }
            input1.close();
            input2.close();

            BufferedWriter shared = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f1.getName() + "." + f2.getName()
                            + ".shared")));
            BufferedWriter only1 = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f1.getName() + ".only")));
            BufferedWriter only2 = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f2.getName() + ".only")));

            Collections.sort(chain1);
            Collections.sort(chain2);
            Vector v1 = new Vector(chain1);
            Vector v2 = new Vector(chain2);
            int i = 0;
            int j = 0;

            while ((i < v1.size()) && (j < v2.size())) {
                String event1 = (String) v1.get(i);
                String event2 = (String) v2.get(j);
                int result = event1.compareTo(event2);
                if (result == 0) {
                    shared.write(event1 + "\n");
                    i++;
                    j++;
                } else if (result < 0) {
                    only1.write(event1 + "\n");
                    i++;
                } else {
                    only2.write(event2 + "\n");
                    j++;
                }
            }

            if (i < v1.size()) {
                for (; i < v1.size(); i++) {
                    only1.write((String) v1.get(i) + "\n");
                }
            } else if (j < v2.size()) {
                for (; j < v2.size(); j++) {
                    only2.write((String) v2.get(j) + "\n");
                }
            }

            shared.close();
            only1.close();
            only2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see edu.virginia.cs.terracotta.Processor#processOpts(java.lang.String[])
     */
    public void processOpts(String[] args) {
        int c;
        LongOpt[] longOpts = prepareLongOpts();
        Getopt opt = new Getopt("EventComparison", args, "", longOpts);
        while ((c = opt.getopt()) != -1) {
            switch (c) {
            case 1:
                filename1 = opt.getOptarg();
                break;
            case 2:
                filename2 = opt.getOptarg();
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
        if ((filename1 == null) || (filename2 == null)) {
            usage();
            System.exit(1);
        }
    }

    private LongOpt[] prepareLongOpts() {
        int num = 2;
        LongOpt[] longOpts = new LongOpt[num];
        longOpts[0] = new LongOpt("file1", LongOpt.REQUIRED_ARGUMENT, null, 1);
        longOpts[1] = new LongOpt("file2", LongOpt.REQUIRED_ARGUMENT, null, 2);
        return longOpts;
    }

    /**
     * @see edu.virginia.cs.terracotta.Processor#usage()
     */
    public void usage() {
        System.out.println("EventComparison");
        System.out.println("Required arguments:");
        System.out.println("--file1 eventfile1\tThe first event file");
        System.out.println("--file2 eventfile2\tThe second event file");
        System.out.println("Optional arguments:");
        System.out.println("There is no optional arguments now.");
    }

    public static void main(String[] args) {
        EventComparison ec = new EventComparison();
        ec.processOpts(args);
        ec.process();
    }
}