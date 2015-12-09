/**
 * Created on 2004-8-3
 *
 */

/**
 * @author Jinlin Yang
 *  
 */

package edu.virginia.cs.terracotta;

import edu.virginia.cs.terracotta.CFG.MethodFilterPolicy;
import edu.virginia.cs.terracotta.CFG.StaticCallGraph;
import edu.virginia.cs.terracotta.event.Event;
import edu.virginia.cs.terracotta.property.Heuristics;
import edu.virginia.cs.terracotta.property.Property;
import edu.virginia.cs.terracotta.property.PropertyChainList;
import edu.virginia.cs.terracotta.property.PropertyMatrix;
import edu.virginia.cs.terracotta.triangle.Triangle_ikjk;
import edu.virginia.cs.terracotta.triangle.Triangle_ikkj;
import edu.virginia.cs.terracotta.triangle.Triangle_kikj;
import gnu.getopt.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

public class Synthesizer implements Processor {

    public static DataOutputStream LOG = null;

    private Hashtable event2Index = new Hashtable();

    private Hashtable index2Event = new Hashtable();

    private PropertyMatrix matrix;

    private String altFile = "Alternate.txt";

    private String meFile = "MultiEffect.txt";

    private String mcFile = "MultiCause.txt";

    private String efFile = "EffectFirst.txt";

    private String ocFile = "OneCause.txt";

    private String oeFile = "OneEffect.txt";

    private String cfFile = "CauseFirst.txt";

    private String eventsFile = "Events.txt";

    private String propdir;

    private String classdir;

    private StaticCallGraph scg;

    private Vector preferedHeuristics = new Vector();

    private boolean doALT = false;

    private boolean doME = false;

    private boolean doMC = false;

    private boolean doEF = false;

    private boolean doOC = false;

    private boolean doOE = false;

    private boolean doCF = false;

    public static void main(String[] args) {
        Synthesizer syn = new Synthesizer();
        syn.processOpts(args);
        try {
            syn.process();
            Synthesizer.LOG.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processOpts(String[] args) {
        int c;
        String arg;
        LongOpt[] longOpts = prepareLongOpts();
        Getopt opt = new Getopt("Synthesizer", args, "o:", longOpts);
        while ((c = opt.getopt()) != -1) {
            switch (c) {
            case 1:
                doALT = true;
                if (opt.getOptarg() != null)
                    altFile = opt.getOptarg();
                break;
            case 2:
                doALT = true;
                doME = true;
                if (opt.getOptarg() != null)
                    meFile = opt.getOptarg();
                break;
            case 3:
                doMC = true;
                mcFile = opt.getOptarg();
                break;
            case 4:
                doEF = true;
                efFile = opt.getOptarg();
                break;
            case 5:
                doOC = true;
                ocFile = opt.getOptarg();
                break;
            case 6:
                doOE = true;
                oeFile = opt.getOptarg();
                break;
            case 7:
                doCF = true;
                cfFile = opt.getOptarg();
                break;
            case 8:
                eventsFile = opt.getOptarg();
                break;
            case 9:
                propdir = opt.getOptarg();
                break;
            case 10:
                classdir = opt.getOptarg();
                break;
            case 'o':
                try {
                    LOG = new DataOutputStream(new BufferedOutputStream(
                            new FileOutputStream(opt.getOptarg())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        if (LOG == null) {
            LOG = new DataOutputStream(System.out);
        }
        if (propdir == null) {
            usage();
            System.exit(1);
        }
    }

    private LongOpt[] prepareLongOpts() {
        int num = 10;
        LongOpt[] longOpts = new LongOpt[num];
        //        StringBuffer[] bufs = new StringBuffer[num];
        //        for (int i = 0; i < bufs.length; i++)
        //            bufs[i] = new StringBuffer();
        longOpts[0] = new LongOpt("alt", LongOpt.OPTIONAL_ARGUMENT, null, 1);
        longOpts[1] = new LongOpt("me", LongOpt.OPTIONAL_ARGUMENT, null, 2);
        longOpts[2] = new LongOpt("mc", LongOpt.OPTIONAL_ARGUMENT, null, 3);
        longOpts[3] = new LongOpt("ef", LongOpt.OPTIONAL_ARGUMENT, null, 4);
        longOpts[4] = new LongOpt("oc", LongOpt.OPTIONAL_ARGUMENT, null, 5);
        longOpts[5] = new LongOpt("oe", LongOpt.OPTIONAL_ARGUMENT, null, 6);
        longOpts[6] = new LongOpt("cf", LongOpt.OPTIONAL_ARGUMENT, null, 7);
        longOpts[7] = new LongOpt("events", LongOpt.REQUIRED_ARGUMENT, null, 8);
        longOpts[8] = new LongOpt("propdir", LongOpt.REQUIRED_ARGUMENT, null, 9);
        longOpts[9] = new LongOpt("classdir", LongOpt.REQUIRED_ARGUMENT, null,
                10);
        return longOpts;
    }

    public void usage() {
        System.out.println("Synthesizer");
        System.out.println("Required Options: User must supply these options");
        System.out
                .println("--propdir path :: specify the base dir that contains "
                        + "all property files");

        System.out.println("Options:");
        System.out.println("-o filename :: specify the output filename. "
                + "Default is the standard output.");
        System.out
                .println("--alt filename :: enable processing Alternate properties. "
                        + "The argument is the file containing Alternating properties. "
                        + "Default is Alternate.txt. "
                        + "Enable processing other types of properties automatically "
                        + "enables processing Alternate properties.");
        System.out
                .println("--me filename :: enable processing MultiEffect properties. "
                        + "The argument is the file containing MultiEffect properties. "
                        + "Default is MultiEffect.txt");
        System.out
                .println("--mc filename :: enable processing MultiCause properties. "
                        + "The argument is the file containing MultiCause properties. "
                        + "Default is MultiCause.txt");
        System.out
                .println("--ef filename :: enable processing EffectFirst properties. "
                        + "The argument is the file containing EffectFirst properties. "
                        + "Default is EffectFirst.txt");
        System.out
                .println("--oc filename :: enable processing OneCause properties. "
                        + "The argument is the file containing OneCause properties. "
                        + "Default is OneCause.txt");
        System.out
                .println("--oe filename :: enable processing OneEffect properties. "
                        + "The argument is the file containing OneEffect properties. "
                        + "Default is OneEffect.txt");
        System.out
                .println("--cf filename :: enable processing CauseFirst properties. "
                        + "The argument is the file containing CauseFirst properties. "
                        + "Default is CauseFirst.txt");
        System.out
                .println("--events filename :: specify the file containing Events. "
                        + "Default is Events.txt");
        System.out
                .println("--classdir path :: specify the base dir from which all "
                        + "java class files will be processed");
    }

    /**
     *  
     */
    public void process() {
        try {
            /**
             * Parse events
             */
            long start = System.currentTimeMillis();
            Vector Events = parseEvents(propdir + File.separator + eventsFile);
            long end = System.currentTimeMillis();
            Synthesizer.LOG.writeBytes("????Time to parse Events: "
                    + (end - start) / 1000.0 + "s\n");

            /**
             * Create an empty Property Matrix
             */
            matrix = new PropertyMatrix(event2Index, index2Event,
                    preferedHeuristics);

            /**
             * Alternate
             */
            if (doALT) {
                start = System.currentTimeMillis();
                processAlternate(propdir + File.separator + altFile);
                end = System.currentTimeMillis();
                Synthesizer.LOG.writeBytes("????Time to process Alternating "
                        + "properties: " + (end - start) / 1000.0 + "s\n");
            }
            /**
             * MultiEffect
             */
            if (doME) {
                start = System.currentTimeMillis();
                processMultiEffect(propdir + File.separator + meFile);
                end = System.currentTimeMillis();
                Synthesizer.LOG.writeBytes("????Time to process MultiEffect "
                        + "properties: " + (end - start) / 1000.0 + "s\n");
            }

            /**
             * Construct the static call graph and insert the static distance to
             * the property matrix.
             */
            if (classdir != null) {
                start = System.currentTimeMillis();
                scg = buildSCG();
                end = System.currentTimeMillis();
                Synthesizer.LOG.writeBytes("????Time to construct Static "
                        + "Call Graphs: " + (end - start) / 1000.0 + "s\n");

                start = System.currentTimeMillis();
                matrix.addHeuristic(scg);
                end = System.currentTimeMillis();
                Synthesizer.LOG.writeBytes("????Time to insert the Static "
                        + "Distance heuristic: " + (end - start) / 1000.0
                        + "s\n");
            }

            /**
             * Construct the Alternating chains. If we have the static distance,
             * then sort the Alternating chains.
             */
            if (doALT) {
                start = System.currentTimeMillis();
                PropertyChainList pcl = matrix
                        .makePropertyChainList(Property.ALT);
                end = System.currentTimeMillis();
                Synthesizer.LOG.writeBytes("????Time to construct "
                        + "Alternating chains: " + (end - start) / 1000.0
                        + "s\n");

                if (classdir != null) {
                    preferedHeuristics.clear();
                    preferedHeuristics.add(Heuristics.STATICDIST);
                    preferedHeuristics.add(Heuristics.LENGTH);
                    start = System.currentTimeMillis();
                    pcl.sort();
                    end = System.currentTimeMillis();
                    Synthesizer.LOG.writeBytes("????Time to sort the "
                            + "Alternating Chains according to Static "
                            + "Distance heuristic: " + (end - start) / 1000.0
                            + "s\n");

                    Synthesizer.LOG.writeBytes("\n++++Alternating Chains "
                            + "sorted according to Static Distance");
                    Synthesizer.LOG.writeBytes("\n");
                    pcl.print("Static Distance");
                } else {
                    preferedHeuristics.clear();
                    preferedHeuristics.add(Heuristics.LENGTH);
                    pcl.sort();
                    Synthesizer.LOG.writeBytes("\n++++Alternating Chains "
                            + "sorted by lengths");
                    Synthesizer.LOG.writeBytes("\n");
                    pcl.print();
                }
            }

            /**
             * MultiCause
             */

            /**
             * EffectFirst
             */
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * FIXME Currently this method hard-coded all filtering criteria. Any change
     * require recompilation. It would be nice to put these into a configuration
     * file. This file could be a standard .properties file and its name will be
     * passed as an argument to the synthesizer.
     * 
     * @return
     */
    private StaticCallGraph buildSCG() {
        Vector classNamesInclude = new Vector();
        //        classNamesInclude.add("org\\.jboss\\.tm");
        //        classNamesInclude.add("org\\.jboss\\.mq");
        //        classNamesInclude.add("org\\.jboss\\.resource");
        //classNamesInclude.add("org\\.mortbay\\.http\\.");

        Vector classNamesExclude = new Vector();
        classNamesExclude.add("test\\.");

        Vector methodNamesInclude = new Vector();

        Vector methodNamesExclude = new Vector();
        // Ignore constructors
        methodNamesExclude.add("<.*>");

        // Ignore overriden methods of the Object class
        methodNamesExclude.add("toString");
        methodNamesExclude.add("hashCode");
        methodNamesExclude.add("equals");
        methodNamesExclude.add("clone");

        //        // Ignore getter/setter methods
        //        methodNamesExclude.add("get.*");
        //        methodNamesExclude.add("set.*");

        // Ignore test methods
        methodNamesExclude.add("test.*");

        MethodFilterPolicy policy = new MethodFilterPolicy(classNamesInclude,
                classNamesExclude, methodNamesInclude, methodNamesExclude);

        StaticCallGraph ret = new StaticCallGraph(classdir, policy, policy);

        return ret;
    }

    private void processAlternate(String filename) throws IOException {
        Synthesizer.LOG.writeBytes("\n#### Begin processing Alternating "
                + "properties");
        Synthesizer.LOG.writeBytes("\n");
        // Parsing
        long start = System.currentTimeMillis();
        matrix.addProperties(Property.ALT, filename);
        long end = System.currentTimeMillis();
        Synthesizer.LOG.writeBytes("????Time to parse Alternating properties: "
                + (end - start) / 1000.0 + "s\n");

        // Adding triangles we want
        Vector triangleSet = new Vector();
        triangleSet.add(new Triangle_ikkj(Property.ALT, Property.ALT, matrix));

        // Filtering
        start = System.currentTimeMillis();
        matrix.filter(triangleSet);
        end = System.currentTimeMillis();
        Synthesizer.LOG.writeBytes("????Time to filter Alternating "
                + "properties: " + (end - start) / 1000.0 + "s\n");

        // Printing
        Synthesizer.LOG.writeBytes("\n++++Alternating properties unsorted");
        Synthesizer.LOG.writeBytes("\n");
        matrix.print(Property.ALT);
    }

    private void processMultiEffect(String filename) throws IOException {
        Synthesizer.LOG.writeBytes("\n#### Begin processing MultiEffect "
                + "properties");
        Synthesizer.LOG.writeBytes("\n");
        // Parsing
        long start = System.currentTimeMillis();
        matrix.addProperties(Property.ME, filename);
        long end = System.currentTimeMillis();
        Synthesizer.LOG.writeBytes("????Time to parse MultiEffect properties: "
                + (end - start) / 1000.0 + "s\n");

        // Add triangles we want
        Vector triangleSet = new Vector();
        triangleSet.add(new Triangle_kikj(Property.ALT, Property.ME, matrix));
        triangleSet.add(new Triangle_ikjk(Property.ME, Property.ALT, matrix));
        triangleSet.add(new Triangle_ikkj(Property.ALT, Property.ME, matrix));
        triangleSet.add(new Triangle_ikkj(Property.ME, Property.ALT, matrix));

        // Filtering
        start = System.currentTimeMillis();
        matrix.filter(triangleSet);
        end = System.currentTimeMillis();
        Synthesizer.LOG.writeBytes("????Time to filter MultiEffect "
                + "properties: " + (end - start) / 1000.0 + "s\n");

        // Printing
        Synthesizer.LOG.writeBytes("\n++++MultiEffect properties unsorted");
        Synthesizer.LOG.writeBytes("\n");
        matrix.print(Property.ME);
    }

    /**
     * This method parse the file that contains Events and return a Vector
     * contains all the Events.
     * 
     * As a side-effect, it also construct the mapping between an Event to an
     * Index number and store the relationships in two hashtables event2index
     * and index2event.
     * 
     * @param fname
     * @return
     */
    private Vector parseEvents(String fname) {
        Vector Events = new Vector();
        try {
            RandomAccessFile input = new RandomAccessFile(fname, "r");
            String line;
            while ((line = input.readLine()) != null) {
                // filter comments
                if (line.matches("^//.*"))
                    continue;
                Event event = new Event(line);
                if (!event2Index.containsKey(event)) {
                    event2Index.put(event, new Integer(Events.size()));
                    index2Event.put(new Integer(Events.size()), event);
                }
                Events.add(event);
            }
            //Always close file
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return Events;
    }
}