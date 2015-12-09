/* ComplexConnector.java */

package org.quilt.graph;

/** 
 * A Connector holding a single edge plus a fixed size array of edges.
 * This is a combination of the UnaryConnector and MultiConnector.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class ComplexConnector extends Connector {

    /** The single edge */
    private Edge edge;

    /** The array of edges. */
    private Edge[] edges = null;

    /** Source of all edges in this connector. */
    private Vertex source = null;

    /**
     * Constructor for a Connector with a single edge plus a fixed-size 
     * array of edges.  The source of the single edge becomes the source
     * of the array of edges.  All edges in the array are set to point
     * to the graph exit.
     *
     * @param e     Becomes preferred edge of the connector.
     * @param n     Number of edges in the array
     * @param graph Graph this appears in.
     */
    public ComplexConnector (final Edge e, int n
                                                //, final Exit exit
                                                 ) {
        if ( e == null || n < 1 
                                // || exit == null
                                                    ) {
            throw new IllegalArgumentException (
                "constructor arguments must be in range and not null");
        }
        edge = new Edge (e);        // the preferred edge 
        edges = new Edge[n];        // fixed-size array of edges
        source = edge.getSource();
        Vertex target = edge.getTarget();
        for (int i = 0; i < n; i++) {
            edges[i] = new Edge (source, target);
        }
    }
    public ComplexConnector (Connector conn, int n
                                //, final Exit exit
                                                        ) {
        // will throw NPE if conn is null 
        this(conn.getEdge(), n
                                // , exit
                                            );
    }
    // INTERFACE CONNECTOR //////////////////////////////////////////
    /** Get the single edge. */
    public Edge getEdge() {
        return edge;
    }
    /** Get the target of the single edge. */
    public Vertex getTarget() {
        return edge.getTarget();
    }
    /** Change the target of the single edge. */
    public void setTarget(Vertex v) {
        checkTarget(v);
        edge.setTarget(v);
    }
    // OTHER METHODS ////////////////////////////////////////////////
    private void checkTarget (final Vertex target) {
        if (target == null) {
            throw new IllegalArgumentException(
                "target may not be null");
        }
        if ( source.getGraph() != target.getGraph() ) {
            throw new IllegalArgumentException(
                "ComplexConnector's target must be in the same graph");
        }
    }
    private void rangeCheck(int n) {
        if ( n < 0 || n >=edges.length) {
            throw new IllegalArgumentException (
                "ComplexConnector index " + n 
                    + " out of range 0.." + (edges.length - 1) );
        }
    }

    /** Get the Nth edge from the array. */
    public Edge getEdge(int n) {
        rangeCheck(n);
        return edges[n];
    }
    
    /** Get the target of the Nth edge. */
    public Vertex getTarget(int n) {
        rangeCheck(n);
        return edges[n].getTarget();
    }
    /** Change the target of the Nth edge. */
    public void setTarget(Vertex v, int n) {
        checkTarget(v);
        rangeCheck(n);
        edges[n].setTarget(v);
    }

    
    /** 
     * Returns the number of edges in the connector EXCLUDING the
     * preferred edge. This is the same number used in the 
     * constructor as the size of the connector.
     *
     * @return Total number of edges in the multiway part of the connector.
     */
    public int size () {
        return edges.length;
    }
    
    /**
     * @author Jinlin Yang
     */
    public Edge getIthEdge(int i){
    	if(i == 0)
    		return edge;
    	else
    		return edges[i-1];
    }
}
