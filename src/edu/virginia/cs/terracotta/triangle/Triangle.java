/*
 * Created on 2004-9-16
 */
package edu.virginia.cs.terracotta.triangle;

import edu.virginia.cs.terracotta.property.PropertyMatrix;

/**
 * 
 * @author Jinlin Yang 
 * File: TriangleBase.java
 */

public abstract class Triangle {
    /**
     * The property type of the edge containing node <code>i</code>.
     */
    protected String type_i;

    /**
     * The property type of the edge containing node <code>j</code>.
     */
    protected String type_j;
    
    protected PropertyMatrix propmatrix;

    /**
     * @param type_i
     * @param type_j
     * @author Jinlin Yang
     */
    public Triangle(String type_i, String type_j, PropertyMatrix propmatrix) {
        this.type_i = type_i;
        this.type_j = type_j;
        this.propmatrix = propmatrix;
    }
    
    public abstract boolean matches(int i, int j, int k);
}