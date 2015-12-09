/* UnaryConnector.java */

package org.quilt.graph;

/** 
 * A Connector holding a single edge. 
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class UnaryConnector extends Connector {
    private Edge edge;

    public UnaryConnector (Edge e) {
        if (e == null) {
            throw new IllegalArgumentException("null edge");
        }
        edge = e;
    }
    /** Get the edge. */
    public Edge getEdge() {
        return edge;
    }
    // XXX Although the next two methods are labeled 'convenience
    // method', it might be better to be less flexible.  It
    // never makes sense to have the source of an edge be anything
    // other than the vertex it is attached to.

    /** Get the target of the connection. Convenience method. */
    public Vertex getTarget () {
        return edge.getTarget();
    }
    /** Set the target of the connection. Convenience method. */
    public void setTarget (Vertex v) {
        edge.setTarget(v);
    }
    public int size () {
        return 1;
    }
    
	/**
	 * 
	 * @author Jinlin Yang
	 *
	 */
	public Edge getIthEdge(int i) {
		if (i!=0) {
			throw new IllegalArgumentException(
					"i must be than 0");
		}
		return edge;
	}
}
