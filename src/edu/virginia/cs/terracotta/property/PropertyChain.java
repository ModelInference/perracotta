/*
 * PropertyChain.java
 * Created on 2004-9-20
 */
package edu.virginia.cs.terracotta.property;

import java.io.IOException;
import java.util.Vector;

import edu.virginia.cs.terracotta.Synthesizer;

/**
 * @author Jinlin Yang
 */
public class PropertyChain implements Comparable {
    private Vector list;

    private Vector preferedHeuristics;

    private Vector heuristics;

    public PropertyChain(Vector preferedHeuristics) {
        this.preferedHeuristics = preferedHeuristics;
        list = new Vector();
        heuristics = new Vector();
    }

    public void add(Property p) {
        if (list.size() == 0) {
            // initialize the Heuristics vector when we add the first element to
            // the property chain.
            for (int i = 0; i < p.getHeuristics().size(); i++) {
                Heuristics h = (Heuristics) p.getHeuristics().get(i);
                heuristics.add(new Heuristics(h.getName(), h.getValue()));
            }
        } else {
            for (int i = 0; i < heuristics.size(); i++) {
                Heuristics h = (Heuristics) heuristics.get(i);
                h.setValue(h.getValue()
                        + p.getHeuristic(h.getName()).getValue());
            }
        }

        list.add(p);
    }

    public Property get(int i) {
        return (Property) list.get(i);
    }

    public Heuristics getHeuristic(String type) {
        Heuristics h = null;
        for (int i = 0; i < heuristics.size(); i++) {
            if (((Heuristics) heuristics.get(i)).getName().equals(type))
                return (Heuristics) heuristics.get(i);
        }
        return h;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (!(o instanceof PropertyChain))
            throw new ClassCastException();
        // First compare two chains based the prefered heuristics
        for (int i = 0; i < preferedHeuristics.size(); i++) {
            String type = (String) preferedHeuristics.get(i);
            double valueOfThis = getHeuristic(type).getValue();
            double valueOfThat = ((PropertyChain) o).getHeuristic(type)
                    .getValue();
            if (valueOfThis < valueOfThat)
                return -1;
            else if (valueOfThis > valueOfThat)
                return 1;
        }

        // Second compare two chains by lengths
        int temp = size() - ((PropertyChain) o).size();
        if (temp < 0)
            return -1;
        else if (temp > 0)
            return 1;

        // Third compare two chains alphabetically
        int i = 0;
        while ((i < size()) && (i < ((PropertyChain) o).size())) {
            temp = get(i).compareTo(((PropertyChain) o).get(i));
            if (temp < 0)
                return -1;
            else if (temp > 0)
                return 1;
            i++;
        }

        return 0;
    }

    public int size() {
        return list.size();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Length: " + list.size() + "\n");
        for (int i = 0; i < list.size(); i++)
            buf.append(((Property) list.get(i)).toString() + "\n");
        return buf.toString();
    }

    public void print() throws IOException {
        Synthesizer.LOG.writeBytes("Length: " + list.size());
        Synthesizer.LOG.writeBytes("\n");
        for (int i = 0; i < list.size(); i++) {
            Synthesizer.LOG.writeBytes(((Property) list.get(i)).toString());
            Synthesizer.LOG.writeBytes("\n");
        }
    }

    public void print(String type) throws IOException {
        Synthesizer.LOG.writeBytes("Length: " + list.size() + ", "
                + getHeuristic(type));
        Synthesizer.LOG.writeBytes("\n");
        for (int i = 0; i < list.size(); i++) {
            Synthesizer.LOG.writeBytes(((Property) list.get(i)).toString(type));
            Synthesizer.LOG.writeBytes("\n");
        }
    }
}