/* MultiConnector.java */

package org.quilt.graph;

/** 
 * A Connector holding a array of edges, where the array has at least
 * one member.  The first element of the array is preferred.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class MultiConnector extends Connector {

    /** Fixed-size array of directed edges. */
    private Edge [] edges = null;

    /** Source of all edges in this connector. */
    private Vertex source = null;
    
    /**
     * Constructor for fixed-size array of edges.  All edges in
     * the new connector are copies of the seed edge.
     */
    public MultiConnector (Edge seed, int n) {
        if ( seed == null || n < 1) {
            throw new IllegalArgumentException(
                    "constructor arguments null or not in range");
        }
        Vertex source = seed.getSource();
        source = seed.getSource();
        Vertex target = seed.getTarget();
        edges = new Edge[n];

        edges[0] = new Edge (seed);            // copy it
        for (int i = 1; i < n; i++) {
            edges[i] = new Edge (source, target);
        }
    }
    /**
     * Constructor initialized from an existing UnaryConnector.
     * The unary connector is destroyed after constructing the
     * new connector.
     */
    public MultiConnector( Connector conn, int n) {
        // will throw NPE if conn == null
        this( conn.getEdge(), n);
    }
    // INTERFACE CONNECTOR //////////////////////////////////////////
    public Edge getEdge () {
        return edges[0];
    }
    public Vertex getTarget () {
        return edges[0].getTarget();
    }
    public void setTarget (Vertex v) {
        checkTarget(v);
        edges[0].setTarget(v);
    }
    // OTHER METHODS ////////////////////////////////////////////////
    private void checkTarget (final Vertex target) {
        if (target == null) {
            throw new IllegalArgumentException("target may not be null");
        }
        if ( target.getGraph() != source.getGraph() ) {
            throw new IllegalArgumentException(
                    "new target must be in same graph");
        }
    }
    private void rangeCheck (int n) {
        if ( n < 0 || n >= edges.length ) {
            throw new IllegalArgumentException(
                 "MultiConnector index out of range");
        }
    }
    public Edge getEdge (int n) {
        rangeCheck(n);
        return edges[n];
    }
    public Vertex getTarget (int n) {
        rangeCheck(n);
        return edges[n].getTarget();
    }
    public void setTarget (Vertex v, int n) {
        checkTarget(v);
        rangeCheck(n);
        edges[n].setTarget(v);
    }

    
    /** @return The number of edges in the Connector. */
    public int size () {
        return edges.length;
    }
    
    /**
     * @author Jinlin Yang
     */
    public Edge getIthEdge (int n) {
        rangeCheck(n);
        return edges[n];
    }
}
