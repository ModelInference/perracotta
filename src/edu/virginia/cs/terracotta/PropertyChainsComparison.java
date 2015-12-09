/*
 * PropertyChainsComparison.java
 * Created on 2004-10-25
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
import java.util.Hashtable;
import java.util.Vector;

import edu.virginia.cs.terracotta.event.Event;
import edu.virginia.cs.terracotta.property.Property;
import edu.virginia.cs.terracotta.property.PropertyChain;
import edu.virginia.cs.terracotta.property.PropertyChainList;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
 * @author Jinlin Yang
 */
public class PropertyChainsComparison implements Processor {

    private String filename1 = null;

    private String filename2 = null;

    private String eventfilename1 = null;

    private String eventfilename2 = null;

    /**
     * @see edu.virginia.cs.terracotta.Processor#process()
     */
    public void process() {
        try {
            Hashtable event2freq1 = parseEventsFreq(eventfilename1);
            Hashtable event2freq2 = parseEventsFreq(eventfilename2);
            File f1 = new File(filename1);
            File f2 = new File(filename2);

            PropertyChainList chains1 = parsePropertyChains(filename1);
            chains1.sort();
            PropertyChainList chains2 = parsePropertyChains(filename2);
            chains2.sort();

            BufferedWriter shared = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f1.getName() + "." + f2.getName()
                            + ".shared")));
            BufferedWriter only1 = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f1.getName() + ".only")));
            BufferedWriter only2 = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f2.getName() + ".only")));

            int i = 0;
            int j = 0;
            PropertyChainList sh = new PropertyChainList();
            PropertyChainList o1 = new PropertyChainList();
            PropertyChainList o2 = new PropertyChainList();
            while ((i < chains1.size()) && (j < chains2.size())) {
                PropertyChain a = (PropertyChain) chains1.get(i);
                PropertyChain b = (PropertyChain) chains2.get(j);
                int temp = a.compareTo(b);
                if (temp == 0) {
                    Integer freq = (Integer) event2freq1
                            .get(a.get(0).getFrom());
                    shared.write("Chain #" + (sh.size() + 1) + " freq=" + freq
                            + "\n");
                    shared.write(a.toString());
                    sh.add(a);
                    i++;
                    j++;
                } else if (temp < 0) {
                    Integer freq = (Integer) event2freq1
                            .get(a.get(0).getFrom());
                    only1.write("Chain #" + (o1.size() + 1) + " freq=" + freq
                            + "\n");
                    only1.write(a.toString());
                    o1.add(a);
                    i++;
                } else {
                    Integer freq = (Integer) event2freq2
                            .get(b.get(0).getFrom());
                    only2.write("Chain #" + (o2.size() + 1) + " freq=" + freq
                            + "\n");
                    only2.write(b.toString());
                    o2.add(b);
                    j++;
                }
            }

            if (i < chains1.size()) {
                for (; i < chains1.size(); i++) {
                    Integer freq = (Integer) event2freq1.get(chains1.get(i)
                            .get(0).getFrom());
                    only1.write("Chain #" + (o1.size() + 1) + " freq=" + freq
                            + "\n");
                    only1.write(chains1.get(i).toString());
                    o1.add(chains1.get(i));
                }
            } else if (j < chains2.size()) {
                for (; j < chains2.size(); j++) {
                    Integer freq = (Integer) event2freq2.get(chains2.get(j)
                            .get(0).getFrom());
                    only2.write("Chain #" + (o2.size() + 1) + " freq=" + freq
                            + "\n");
                    only2.write(chains2.get(j).toString());
                    o2.add(chains2.get(j));
                }
            }

            shared.close();
            only1.close();
            only2.close();

            Vector[] a2b = new Vector[o1.size()];
            for (i = 0; i < a2b.length; i++)
                a2b[i] = new Vector();
            Vector[] b2a = new Vector[o2.size()];
            for (j = 0; j < b2a.length; j++)
                b2a[j] = new Vector();

            for (i = 0; i < o1.size(); i++) {
                for (j = 0; j < o2.size(); j++) {
                    if (compareTwoPropertyChains(o1.get(i), o2.get(j))) {
                        a2b[i].add(new Integer(j));
                        b2a[j].add(new Integer(i));
                    }
                }
            }

            boolean[] flag_a2b = new boolean[a2b.length];
            for (i = 0; i < flag_a2b.length; i++)
                flag_a2b[i] = false;
            boolean[] flag_b2a = new boolean[b2a.length];
            for (j = 0; j < flag_b2a.length; j++)
                flag_b2a[j] = false;

            Vector froms = new Vector();
            Vector tos = new Vector();
            for (i = 0; i < a2b.length; i++) {
                if (flag_a2b[i])
                    continue;
                if (a2b[i].size() == 0)
                    continue;
                Vector visited_a = new Vector();
                Vector visited_b = new Vector();
                Vector work_a = new Vector();
                Vector work_b = new Vector();
                work_a.add(new Integer(i));
                while (!work_a.isEmpty() || !work_b.isEmpty()) {
                    if (!work_a.isEmpty()) {
                        int idx = ((Integer) work_a.remove(0)).intValue();
                        visited_a.add(new Integer(idx));
                        flag_a2b[idx] = true;
                        for (int k = 0; k < a2b[idx].size(); k++) {
                            Integer temp = (Integer) a2b[idx].get(k);
                            if (!visited_b.contains(temp))
                                work_b.add(temp);
                        }
                    } else if (!work_b.isEmpty()) {
                        int idx = ((Integer) work_b.remove(0)).intValue();
                        visited_b.add(new Integer(idx));
                        flag_b2a[idx] = true;
                        for (int k = 0; k < b2a[idx].size(); k++) {
                            Integer temp = (Integer) b2a[idx].get(k);
                            if (!visited_a.contains(temp))
                                work_a.add(temp);
                        }
                    }
                }
                froms.add(visited_a);
                tos.add(visited_b);
            }

            // check invariants
            if (froms.size() != tos.size())
                throw new AssertionError(froms.size() != tos.size());

            BufferedWriter mapping = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f1.getName() + ".mapping."
                            + f2.getName())));

            mapping.write("++++" + f1.getName() + " to " + f2.getName() + "\n");
            for (i = 0; i < froms.size(); i++) {
                Vector a = (Vector) froms.get(i);
                for (int k = 0; k < a.size(); k++) {
                    int temp = ((Integer) a.get(k)).intValue() + 1;
                    mapping.write((new Integer(temp)).toString());
                    Event topEvent = ((PropertyChain) o1.get(temp - 1)).get(0)
                            .getFrom();
                    Integer freq = (Integer) event2freq1.get(topEvent);
                    mapping.write("(" + freq.toString() + ")");
                    if (k != a.size() - 1)
                        mapping.write(", ");
                    else
                        mapping.write("\t: ");

                }
                Vector b = (Vector) tos.get(i);
                for (int k = 0; k < b.size(); k++) {
                    int temp = ((Integer) b.get(k)).intValue() + 1;
                    mapping.write((new Integer(temp)).toString());
                    Event topEvent = ((PropertyChain) o2.get(temp - 1)).get(0)
                            .getFrom();
                    Integer freq = (Integer) event2freq2.get(topEvent);
                    mapping.write("(" + freq.toString() + ")");
                    if (k != b.size() - 1)
                        mapping.write(", ");
                    else
                        mapping.write("\n");
                }
            }

            mapping.write("\n++++" + f1.getName() + " unmatched\n");
            for (i = 0; i < a2b.length; i++) {
                if (a2b[i].size() == 0) {
                    Event topEvent = ((PropertyChain) o1.get(i)).get(0)
                            .getFrom();
                    Integer freq = (Integer) event2freq1.get(topEvent);
                    mapping.write((i + 1) + "(" + freq.toString() + ")"
                            + ": unmatched\n");
                }
            }

            mapping.write("\n++++" + f2.getName() + " unmatched\n");
            for (i = 0; i < b2a.length; i++) {
                if (b2a[i].size() == 0) {
                    Event topEvent = ((PropertyChain) o2.get(i)).get(0)
                            .getFrom();
                    Integer freq = (Integer) event2freq2.get(topEvent);
                    mapping.write((i + 1) + "(" + freq.toString() + ")"
                            + ": unmatched\n");
                }
            }
            mapping.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean compareTwoPropertyChains(PropertyChain a, PropertyChain b) {
        int i = 0;
        int j = 0;
        for (i = 0; i < a.size(); i++) {
            Property a_i = a.get(i);
            Event a_i_from = a_i.getFrom();
            Event a_i_to = a_i.getTo();
            for (j = 0; j < b.size(); j++) {
                Property b_j = b.get(j);
                Event b_j_from = b_j.getFrom();
                Event b_j_to = b_j.getTo();
                if (a_i_from.equals(b_j_from) || a_i_from.equals(b_j_to)
                        || a_i_to.equals(b_j_from) || a_i_to.equals(b_j_to))
                    return true;
            }
        }

        return false;
    }

    private Hashtable parseEventsFreq(String filename) {
        Hashtable event2freq = new Hashtable();
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filename)));
            String line = null;
            while ((line = input.readLine()) != null) {
                if (line.startsWith("//"))
                    continue;
                //Assume the line format is as follows 
                // freq: eventName
                int firstColon = line.indexOf(':');
                String freqStr = line.substring(0, firstColon);
                Integer freq = Integer.valueOf(freqStr);
                String eventStr = line.substring(firstColon + 2);
                eventStr.trim();
                Event event = new Event(eventStr);
                event2freq.put(event, freq);
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return event2freq;
    }

    private PropertyChainList parsePropertyChains(String filename) {
        PropertyChainList chains = new PropertyChainList();
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filename)));
            Vector preferedHeuristicsforChains = new Vector();
            Vector preferedHeuristics = new Vector();
            PropertyChain pc = null;
            String line = null;
            while ((line = input.readLine()) != null) {
                if (line.startsWith("//"))
                    continue;
                line = line.trim();
                if (line.startsWith("Chain #")) {
                    line = input.readLine();
                    if ((line == null) || (!line.startsWith("Length: "))) {
                        System.err.println("Invalid file format");
                        System.exit(1);
                    }
                    pc = new PropertyChain(preferedHeuristicsforChains);
                    chains.add(pc);
                } else {
                    String[] events = line.split("->");
                    if (events.length != 2) {
                        System.err.println("Invalid file format");
                        System.exit(1);
                    }
                    if (pc == null) {
                        System.err.println("Invalid file format: No "
                                + "'Chain #', " + "and 'Length:' lines");
                        System.exit(1);
                    }
                    Event from = new Event(events[0]);
                    Event to = new Event(events[1]);
                    Property p = new Property(from, to, Property.ALT,
                            Property.CONNECTED, preferedHeuristics);
                    pc.add(p);
                }
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chains;
    }

    /**
     * @see edu.virginia.cs.terracotta.Processor#processOpts(java.lang.String[])
     */
    public void processOpts(String[] args) {
        int c;
        String arg;
        LongOpt[] longOpts = prepareLongOpts();
        Getopt opt = new Getopt("PropertyChainsComparison", args, "", longOpts);
        while ((c = opt.getopt()) != -1) {
            switch (c) {
            case 1:
                filename1 = opt.getOptarg();
                break;
            case 2:
                filename2 = opt.getOptarg();
                break;
            case 3:
                eventfilename1 = opt.getOptarg();
                break;
            case 4:
                eventfilename2 = opt.getOptarg();
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
        if ((filename1 == null) || (filename2 == null)
                || (eventfilename1 == null) || (eventfilename2 == null)) {
            usage();
            System.exit(1);
        }
    }

    private LongOpt[] prepareLongOpts() {
        int num = 4;
        LongOpt[] longOpts = new LongOpt[num];
        longOpts[0] = new LongOpt("file1", LongOpt.REQUIRED_ARGUMENT, null, 1);
        longOpts[1] = new LongOpt("file2", LongOpt.REQUIRED_ARGUMENT, null, 2);
        longOpts[2] = new LongOpt("event1", LongOpt.REQUIRED_ARGUMENT, null, 3);
        longOpts[3] = new LongOpt("event2", LongOpt.REQUIRED_ARGUMENT, null, 4);
        return longOpts;
    }

    /**
     * @see edu.virginia.cs.terracotta.Processor#usage()
     */
    public void usage() {
        System.out.println("PropertyChainsComparison");
        System.out.println("Required arguments:");
        System.out.println("--file1 eventfile1\t"
                + "The first property chains file");
        System.out.println("--file2 eventfile2\t"
                + "The second property chains file");
        System.out.println("--event1 eventfile1\t"
                + "The frequency of events in the first trace file");
        System.out.println("--event2 eventfile2\t"
                + "The frequency of events in the second trace file");
        System.out.println("Optional arguments:");
        System.out.println("There is no optional arguments now.");
    }

    public static void main(String[] args) {
        PropertyChainsComparison pcc = new PropertyChainsComparison();
        pcc.processOpts(args);
        pcc.process();
    }
}