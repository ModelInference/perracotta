/* Entry.java */

package org.quilt.graph;

/** 
 * The entry vertex in a directed graph. 
 *
 * @author < a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class Entry extends Vertex {

    public Entry (Directed g) {
        if (g == null) {
            throw new IllegalArgumentException ("null graph");
        }
        graph = g;
        index = g.anotherVertex(this);
        Edge edge  = new Edge(this, this) ;
        connector = new UnaryConnector (edge);
        edge.insert (new Exit(g));
    }

    // ACCESSOR METHODS /////////////////////////////////////////////
    public Connector getConnector() {
        return connector;
    }
    public Edge getEdge() {
        return connector.getEdge();
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public Vertex getTarget () {
        return connector.getTarget();
    }
    public void setTarget(Vertex v) {
        checkForNull (v, "target");
        if ( v.getGraph() != graph) {
            throw new IllegalArgumentException (
                                    "target must be in same graph");
        }
        ((UnaryConnector)connector).setTarget(v);
    }
    public String toString () {
        return "Entry " + super.toString();
    }
}
