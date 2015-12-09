/*
 * Heuristics.java
 * Created on 2004-9-16
 */
package edu.virginia.cs.terracotta.property;

/**
 * @author Jinlin Yang
 */
public class Heuristics {

    public static final String LENGTH = "Length";

    public static final String STATICDIST = "Static Distance";

    private String name;

    private double value;

    public Heuristics(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(name);
        buf.append("=");
        buf.append(value);
        return buf.toString();
    }
}