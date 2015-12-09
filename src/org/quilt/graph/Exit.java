/* Exit.java */

package org.quilt.graph;

/** 
 * An exit vertex in a directed graph.  This vertex always has an
 * index of 1. 
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class Exit extends Vertex {

    /** 
     * Constructor for the exit vertex for a Quilt directed graph.
     * This may be a subgraph.  Only the entry vertex should use
     * this constructor.
     *
     * @param g Reference to the parent Directed graph.
     */
    protected Exit (Directed g) {
        checkForNull(g, "graph");
        graph = g;
        index = g.anotherVertex(this);
        connector = new UnaryConnector (new Edge(this,this) );
    }

    // ACCESSOR METHODS /////////////////////////////////////////////
    /** 
     * Get the connection, back to the entry vertex in a top-level
     * graph. XXX Perhaps we don't want to implement this.
     */
    public Connector getConnector () {
        return connector;
    }
    // CONVENIENCE METHODS //////////////////////////////////////////
    /** Get the outgoing edge. */
    public Edge getEdge() {
        return ((UnaryConnector)connector).getEdge();
    }
    /** Get its target.  */
    public Vertex getTarget() {
        return ((UnaryConnector)connector).getTarget();
    }
    /** Set its target. */
    public void setTarget(Vertex v) {
        checkForNull (v, "target");
        if ( graph == v.getGraph() ) {
            throw new IllegalArgumentException(
                    "target of exit must be in different graph");
        }
        ((UnaryConnector)connector).setTarget(v);
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public String toString () {
        return "Exit " + super.toString();
    }
}
