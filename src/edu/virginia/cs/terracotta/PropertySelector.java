/*
 * PropertySelector.java
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import edu.virginia.cs.terracotta.event.Event;

import gnu.getopt.Getopt;

/**
 * @author Jinlin Yang
 */
public class PropertySelector implements Processor {

    String filename = null;

    String eventfile = null;

    /**
     * @see edu.virginia.cs.terracotta.Processor#process()
     */
    public void process() {
        try {
            File propertyfile = new File(filename);
            BufferedReader event = new BufferedReader(new InputStreamReader(
                    new FileInputStream(eventfile)));
            String line = null;
            HashSet eventSet = new HashSet();
            while ((line = event.readLine()) != null) {
                if (line.startsWith("//"))
                    continue;
                Event e = new Event(line.trim());
                eventSet.add(e);
            }
            event.close();

            BufferedReader property = new BufferedReader(new InputStreamReader(
                    new FileInputStream(propertyfile)));

            Vector properties = new Vector();
            while ((line = property.readLine()) != null) {
                if (line.startsWith("//"))
                    continue;

                // split the line into two parts: a pair of events and a list of
                // dynamic heuristics
                String eventPair = line
                        .substring(0, line.lastIndexOf("\"") + 1);

                String[] parts = eventPair.trim().split("->");
                if (parts.length != 2) {
                    System.err.println("Line in wrong format: " + line);
                    System.exit(1);
                }

                if ((eventSet.contains(new Event(parts[0])))
                        && (eventSet.contains(new Event(parts[1])))) {
                    properties.add(line);
                }
            }
            property.close();

            LinkedList templist = new LinkedList(properties);
            Collections.sort(templist);

            BufferedWriter propertyfiltered = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(propertyfile
                            .getName()
                            + ".filtered")));
            Iterator itr = templist.iterator();
            while (itr.hasNext()) {
                propertyfiltered.write((String) itr.next() + "\n");
            }
            propertyfiltered.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @see edu.virginia.cs.terracotta.Processor#processOpts(java.lang.String[])
     */
    public void processOpts(String[] args) {
        int c;
        String arg;
        Getopt opt = new Getopt("PropertySelector", args, "f:e:");
        while ((c = opt.getopt()) != -1) {
            switch (c) {
            case 'f':
                filename = opt.getOptarg();
                break;
            case 'e':
                eventfile = opt.getOptarg();
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
        if ((filename == null) || (eventfile == null)) {
            usage();
            System.exit(1);
        }
    }

    /**
     * @see edu.virginia.cs.terracotta.Processor#usage()
     */
    public void usage() {
        System.out.println("PropertySelector");
        System.out.println("Required arguments:");
        System.out.println("-f filename\tThe property file");
        System.out.println("-e filename\tThe event file");
        System.out.println("Optional arguments:");
        System.out.println("There is no optional arguments now.");
    }

    public static void main(String[] args) {
        PropertySelector ps = new PropertySelector();
        ps.processOpts(args);
        ps.process();
    }
}