/*
 * File: Triangle_kikj.java
 * Created on 2004-9-16
 */
package edu.virginia.cs.terracotta.triangle;

import edu.virginia.cs.terracotta.property.Property;
import edu.virginia.cs.terracotta.property.PropertyMatrix;

/**
 * @author Jinlin Yang File: Triangle_kikj.java
 */
public class Triangle_kikj extends Triangle {

    /**
     * @param type_i
     * @param type_j
     * @author Jinlin Yang
     */
    public Triangle_kikj(String type_i, String type_j, PropertyMatrix propmatrix) {
        super(type_i, type_j, propmatrix);
    }

    /**
     * @see edu.virginia.cs.terracotta.TriangleInterface#matches(int, int, int)
     */
    public boolean matches(int i, int j, int k) {
        if (!propmatrix.get(k, i).isType(type_i))
            return false;
        if (!propmatrix.get(k, j).isType(type_j))
            return false;
        if ((propmatrix.get(k, i).getEdgeType() >= Property.CONNECTED)
                && (propmatrix.get(k, j).getEdgeType() >= Property.CONNECTED))
            return true;
        else
            return false;
    }

}