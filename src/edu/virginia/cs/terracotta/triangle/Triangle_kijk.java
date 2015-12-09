/*
 * File: Triangle_kijk.java
 * Created on 2004-9-16
 */
package edu.virginia.cs.terracotta.triangle;

import edu.virginia.cs.terracotta.property.Property;
import edu.virginia.cs.terracotta.property.PropertyMatrix;

/**
 * @author Jinlin Yang File: Triangle_kijk.java
 */
public class Triangle_kijk extends Triangle {

    /**
     * @param type_i
     * @param type_j
     * @author Jinlin Yang
     */
    public Triangle_kijk(String type_i, String type_j, PropertyMatrix propmatrix) {
        super(type_i, type_j, propmatrix);
    }

    /**
     * @see edu.virginia.cs.terracotta.TriangleInterface#matches(int, int, int)
     */
    public boolean matches(int i, int j, int k) {
        if (!propmatrix.get(k, i).isType(type_i))
            return false;
        if (!propmatrix.get(j, k).isType(type_j))
            return false;
        if ((propmatrix.get(k, i).getEdgeType() >= Property.CONNECTED)
                && (propmatrix.get(j, k).getEdgeType() >= Property.CONNECTED))
            return true;
        else
            return false;
    }

}