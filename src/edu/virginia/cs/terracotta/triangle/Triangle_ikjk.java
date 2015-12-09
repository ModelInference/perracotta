/*
 * File: Triangle_ikjk.java
 * Created on 2004-9-16
 */
package edu.virginia.cs.terracotta.triangle;

import edu.virginia.cs.terracotta.property.Property;
import edu.virginia.cs.terracotta.property.PropertyMatrix;

/**
 * @author Jinlin Yang File: Triangle_ikjk.java
 */
public class Triangle_ikjk extends Triangle {

    public Triangle_ikjk(String type_i, String type_j, PropertyMatrix propmatrix) {
        super(type_i, type_j, propmatrix);
    }

    /**
     * 
     * @see edu.virginia.cs.terracotta.TriangleInterface#matches(int, int, int)
     */
    public boolean matches(int i, int j, int k) {
        if (!propmatrix.get(i, k).isType(type_i))
            return false;
        if (!propmatrix.get(j, k).isType(type_j))
            return false;
        if ((propmatrix.get(i, k).getEdgeType() >= Property.CONNECTED)
                && (propmatrix.get(j, k).getEdgeType() >= Property.CONNECTED))
            return true;
        else
            return false;
    }

}