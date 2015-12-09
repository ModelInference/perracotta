/*
 * PropertyMatrix.java
 * Created on 2004-9-20
 */
package edu.virginia.cs.terracotta.property;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import edu.virginia.cs.terracotta.Synthesizer;
import edu.virginia.cs.terracotta.CFG.MyMethod;
import edu.virginia.cs.terracotta.CFG.MyVertex;
import edu.virginia.cs.terracotta.CFG.StaticCallGraph;
import edu.virginia.cs.terracotta.event.Event;
import edu.virginia.cs.terracotta.triangle.Triangle;

/**
 * @author Jinlin Yang
 */
public class PropertyMatrix {

    private Property[][] matrix;

    private Hashtable event2index;

    private Hashtable index2event;

    private Vector preferedHeuristics;

    public PropertyMatrix(Hashtable event2index, Hashtable index2event,
            Vector preferedHeuristics) {
        this.event2index = event2index;
        this.index2event = index2event;
        this.preferedHeuristics = preferedHeuristics;
        int size = event2index.size();
        matrix = new Property[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++) {
                Event from = (Event) index2event.get(new Integer(i));
                Event to = (Event) index2event.get(new Integer(j));
                matrix[i][j] = new Property(from, to, preferedHeuristics);
            }
    }

    public void addProperties(String type, String filename) {
        String line = null;
        String eventPair = null;
        String dyn_Heur = null;
        String[] events = null;
        Event pEvent = null;
        Event sEvent = null;
        int p = 0;
        int s = 0;
        try {
            RandomAccessFile input = new RandomAccessFile(filename, "r");
            line = input.readLine();
            while (line != null) {
                if (line.matches("^//.*")) {
                    line = input.readLine();
                    continue;
                }

                // 1) split the line into two parts
                eventPair = line.substring(0, line.lastIndexOf("\"") + 1);
                dyn_Heur = line.substring(line.lastIndexOf("\"") + 1, line
                        .length());

                // 2) parse the first part which is the event pair
                events = eventPair.split("->");
                //Check that events.length == 2
                if (events.length != 2) {
                    System.out.println("Illegal line:" + line);
                    input.close();
                    System.exit(1);
                }

                pEvent = new Event(events[0]);
                sEvent = new Event(events[1]);
                p = ((Integer) (event2index.get(pEvent))).intValue();
                s = ((Integer) (event2index.get(sEvent))).intValue();
                matrix[p][s].setEdgeType(Property.CONNECTED);
                matrix[p][s].setType(type);

                // Add the Length heuristics.
                matrix[p][s].addHeuristic(new Heuristics(Heuristics.LENGTH, 1));
                
                // 3) parse the second part which is the dynamic heuristics
                StringTokenizer dyn_Heurs = new StringTokenizer(dyn_Heur);
                while (dyn_Heurs.hasMoreTokens()) {
                    String token = dyn_Heurs.nextToken();
                    String[] parts = token.split("=");
                    if (parts.length != 2) {
                        System.out.println("Illegal dynamic heuristic syntax:"
                                + token);
                        input.close();
                        System.exit(1);
                    }
                    matrix[p][s].addHeuristic(new Heuristics(parts[0], Double
                            .valueOf(parts[1]).doubleValue()));
                }

                line = input.readLine();
            }

            //Always close file
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("#matrix#" + matrix.length + " by "
                    + matrix[0].length);
            System.out.println("#line# " + line);
            System.out.println("#eventPair# " + eventPair);
            System.out.println("#dyn_Heur# " + dyn_Heur);
            System.out.println("#events[0]# " + events[0]);
            System.out.println("#events[1]# " + events[1]);
            System.out.println("#pEvent# " + pEvent);
            System.out.println("#sEvent# " + sEvent);
            System.out.println("#p# " + p);
            System.out.println("#s# " + s);
            System.exit(1);
        }
    }

    public void filter(Vector triangleSet) throws IOException {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                int k;
                for (k = 0; k < matrix.length; k++) {
                    if (matrix[i][j].getEdgeType() <= Property.UNCONNECTED) {
                        break;
                        //                        for (int l = triangleSet.size(); l > 0; l--) {
                        //                            if (((Triangle) triangleSet.get(l - 1)).matches(i,
                        //                                    j, k)) {
                        //                                matrix[i][j]
                        //                                        .setEdgeType(Property.INCOMPLETE_TRIANGLE_EDGE);
                        //                            }
                        //                        }
                    } else {
                        int l;
                        for (l = 0; l < triangleSet.size(); l++) {
                            if (((Triangle) triangleSet.get(l))
                                    .matches(i, j, k)) {
                                matrix[i][j]
                                        .setEdgeType(Property.REDUNDANT_TRIANGLE_EDGE);
                                break;
                            }
                        }
                        if (l < triangleSet.size())
                            break;
                    }
                }
//                if (k == matrix.length) {
//                    Synthesizer.LOG.writeBytes("[" + i + "," + j + "]= "
//                            + matrix[i][j]);
//                    Synthesizer.LOG.writeBytes("\n");
//                    Synthesizer.LOG.flush();
//                }
            }
        }
    }

    public Property get(int p, int s) {
        return matrix[p][s];
    }

    public void print(String type) throws IOException {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (!matrix[i][j].isType(type))
                    continue;
                if (matrix[i][j].isEdgeType(Property.CONNECTED)) {
                    Synthesizer.LOG.writeBytes(matrix[i][j].toString());
                    Synthesizer.LOG.writeBytes("\n");
                }
            }
        }
    }

    // Helper methods
    private void print(int i, int j) throws IOException {
        Synthesizer.LOG.writeBytes((Event) index2event.get(new Integer(i))
                + "->" + (Event) index2event.get(new Integer(j)));
        Synthesizer.LOG.writeBytes("\n");
    }

    public void addHeuristic(StaticCallGraph scg) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                //                System.out.println(i + ", " + j);
                if (!matrix[i][j].isEdgeType(Property.CONNECTED))
                    continue;
                MyMethod from = matrix[i][j].getFrom().getMethod();
                MyMethod to = matrix[i][j].getTo().getMethod();
                int d = scg.getShortestDistance(from, to);
                //                System.out.println(matrix[i][j] + " Static distance is " +
                // d);
                matrix[i][j].addHeuristic(new Heuristics("Static Distance", d));
            }
        }
    }

    public LinkedList sort(String type) {
        LinkedList list = new LinkedList();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (!matrix[i][j].isType(type))
                    continue;
                if (!matrix[i][j].isEdgeType(Property.CONNECTED))
                    continue;
                list.add(matrix[i][j]);
            }
        }
        Collections.sort(list);
        return list;
    }

    public PropertyChainList makePropertyChainList(String type) {
        PropertyChainList pcl = new PropertyChainList();
        Vector roots = findRoots(type);
        byte[] color = new byte[matrix.length];
        Arrays.fill(color, MyVertex.WHITE);
        for (int i = 0; i < roots.size(); i++) {
            PropertyChain pc = makePropertyChain((Event) roots.get(i), type,
                    color);
            if (pc.size() > 0)
                pcl.add(pc);
        }
        return pcl;
    }

    private PropertyChain makePropertyChain(Event root, String type,
            byte[] color) {
        PropertyChain pc = new PropertyChain(preferedHeuristics);
        DFS(((Integer) (event2index.get(root))).intValue(), type, pc, color);
        return pc;
    }

    private void DFS(int from, String type, PropertyChain pc, byte[] color) {
        color[from] = MyVertex.GRAY;
        for (int to = 0; to < matrix[from].length; to++) {
            if (matrix[from][to].getEdgeType() != Property.CONNECTED)
                continue;
            if (!matrix[from][to].isType(type))
                continue;

            pc.add(matrix[from][to]);

            if (color[to] == MyVertex.WHITE)
                DFS(to, type, pc, color);
        }
        color[from] = MyVertex.BLACK;
    }

    private Vector findRoots(String type) {
        Vector roots = new Vector();
        for (int j = 0; j < matrix.length; j++) {
            int i;
            for (i = 0; i < matrix.length; i++)
                if (matrix[i][j].isType(type))
                    break;
            if (i == matrix.length)
                roots.add(index2event.get(new Integer(j)));
        }
        return roots;
    }
}