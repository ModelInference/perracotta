/*
 * Triangle_ikkj.java
 * Created on 2004-9-16
 */
package edu.virginia.cs.terracotta.triangle;

import edu.virginia.cs.terracotta.property.Property;
import edu.virginia.cs.terracotta.property.PropertyMatrix;

/**
 * @author Jinlin Yang
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Triangle_ikkj extends Triangle {

    /**
     * @param type_i
     * @param type_j
     * @author Jinlin Yang
     */
    public Triangle_ikkj(String type_i, String type_j, PropertyMatrix propmatrix) {
        super(type_i, type_j, propmatrix);
    }

    public boolean matches(int i, int j, int k) {
        if (!propmatrix.get(i, k).isType(type_i))
            return false;
        if (!propmatrix.get(k, j).isType(type_j))
            return false;
        if ((propmatrix.get(i, k).getEdgeType() >= Property.CONNECTED)
                && (propmatrix.get(k, j).getEdgeType() >= Property.CONNECTED))
            return true;
        else
            return false;
    }
}