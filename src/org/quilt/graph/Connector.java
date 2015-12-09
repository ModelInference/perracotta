/* Connector.java */

package org.quilt.graph;

/** 
 * Connector holding one or more edges.  Used to connect a vertex
 * to the rest of a graph.  There is always a preferred edge which
 * is visited first when walking the graph.
 *
 * @author < a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public abstract class Connector {

    /** 
     * Get the outgoing edge.  If this is not a UnaryConnector, 
     * this will be the preferred edge.  What 'preferred' means
     * depends upon the type of connector.
     */
    public abstract Edge getEdge (); 
    
    /** 
     * Get the ith outgoing edge.
     */
    public abstract Edge getIthEdge (int i); 
    
    /** Get the target of the preferred edge. */
    public abstract Vertex getTarget ();
    
    /** Set the target of the connector's preferred edge. */
    public abstract void setTarget (Vertex v);

    /** Returns total number of edges in the connector. */
    public abstract int size ();

    /*
     * Create a Vertex with a connector of this type and insert it
     * into edge e.  Any other edges in the Connector will point 
     * back to the Vertex created.  The preferred edge will point
     * to the original target of the edge e; the new Vertex will 
     * become the target of edge e. 
     *
     * This can't be an official part of the interface  - MultiConnector 
     * and ComplexConnector need size.
     */
    // public static makeVertex (Directed graph, Edge e);

    // possibly add these and make this an abstract class
    //
}
