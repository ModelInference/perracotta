/*
 * Property.java
 * Created on 2004-9-20
 */
package edu.virginia.cs.terracotta.property;

import java.util.Vector;

import edu.virginia.cs.terracotta.event.Event;

/**
 * @author Jinlin Yang
 */
public class Property implements Comparable {

    public final static String UNKNOWN = "unknown";

    public final static String ALT = "alternating";

    public final static String ME = "multieffect";

    public final static String MC = "multicause";

    public final static String EF = "effectfirst";

    public final static String OC = "onecause";

    public final static String OE = "oneeffect";

    public final static String CF = "causefirst";

    public static final int CONNECTED = 1;

    public static final int UNCONNECTED = -1;

    public static final int INCOMPLETE_TRIANGLE_EDGE = -2;

    public static final int REDUNDANT_TRIANGLE_EDGE = 2;

    private Event to;

    private Event from;

    private Vector heuristics;

    private String type;

    private int edgeType;

    private Vector preferedHeuristics;

    /**
     * @param to
     * @param from
     * @param name
     */
    public Property(Event from, Event to, String type, int edgeType,
            Vector preferedHeuristics) {
        this.to = to;
        this.from = from;
        this.type = type;
        this.edgeType = edgeType;
        this.preferedHeuristics = preferedHeuristics;
        heuristics = new Vector();
    }

    public Property(Event from, Event to, Vector preferedHeuristics) {
        this(from, to, Property.UNKNOWN, Property.UNCONNECTED,
                preferedHeuristics);
    }

    /**
     * @return Returns the from.
     */
    public Event getFrom() {
        return from;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return Returns the edgeType.
     */
    public int getEdgeType() {
        return edgeType;
    }

    /**
     * @param edgeType
     *            The edgeType to set.
     */
    public void setEdgeType(int edgeType) {
        this.edgeType = edgeType;
    }

    /**
     * @return Returns the to.
     */
    public Event getTo() {
        return to;
    }

    public Heuristics getHeuristic(String type) {
        Heuristics h = null;
        for (int i = 0; i < heuristics.size(); i++) {
            if (((Heuristics) heuristics.get(i)).getName().equals(type))
                return (Heuristics) heuristics.get(i);
        }
        return h;
    }

    public Vector getHeuristics() {
        return heuristics;
    }

    public boolean hasHeuristic(String type) {
        for (int i = 0; i < heuristics.size(); i++) {
            if (((Heuristics) heuristics.get(i)).getName().equals(type))
                return true;
        }
        return false;
    }

    public void addHeuristic(Heuristics h) {
        if (!hasHeuristic(h.getName()))
            heuristics.add(h);
    }

    public boolean isEdgeType(int edgeType) {
        return this.edgeType == edgeType;
    }

    public boolean isType(String type) {
        return this.type.equals(type);
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (!(o instanceof Property))
            throw new ClassCastException(
                    "target object is not an object of class Property");

        // First compare two properties based on the prefered heuristics
        for (int i = 0; i < preferedHeuristics.size(); i++) {
            String type = (String) preferedHeuristics.get(i);
            double valueOfThis = getHeuristic(type).getValue();
            double valueOfThat = ((Property) o).getHeuristic(type).getValue();
            if (valueOfThis < valueOfThat)
                return -1;
            else if (valueOfThis > valueOfThat)
                return 1;
        }

        // Second compare two properties alphabetically
        int temp = toString().compareTo((String) ((Property) o).toString());
        if (temp < 0)
            return -1;
        else if (temp > 0)
            return 1;
        return 0;
    }

    public String toString() {
        return from.toString() + "->" + to.toString();
    }

    public String toString(String type) {
        return from.toString() + "->" + to.toString() + " "
                + getHeuristic(type);
    }
}