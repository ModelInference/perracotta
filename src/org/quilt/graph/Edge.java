/* Edge.java */
package org.quilt.graph;

/** 
 * An edge in a Quilt graph.  This is seen as a source/target pair,
 * XXX but in fact the source field may be unnecessary.
 *
 * @author < a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */

public class Edge {

    protected Vertex source_;
    protected Vertex target_;
    protected boolean isLoopEdge = false;
 
    /** 
     * An edge in a directed graph.
     */
    public Edge(final Vertex s, final Vertex t) {
        if ( s == null || t == null ) {
            throw new IllegalArgumentException("null source or target");
        }
        if (s.getGraph() != t.getGraph() 
                && ! (s instanceof Exit || t instanceof Entry ) ) {
            throw new IllegalArgumentException("source " + s 
                    + " and target " + t + 
                    " of edge constructor are not in the same graph");
        }
        source_ = s;
        target_ = t;
    }

    /** Copy constructor. */
    public Edge ( final Edge e ) {
        checkForNull (e, "edge");       
        source_ = e.getSource();
        target_ = e.getTarget();
    }
    // ACCESSOR METHODS /////////////////////////////////////////////
    public Vertex getSource() {
        return source_;
    }
    public void setSource (Vertex v) {
        checkForNull(v, "source");
        if (target_ != null && v.getGraph() != source_.getGraph()) {
            throw new IllegalArgumentException(
                    "source and target must be in same graph");
        }
        source_ = v;
    }
    public Vertex getTarget() {
        return target_;
    }
    /** 
     * Change the target of this edge.  XXX Wasn't public before;
     * made it so to allow cl.SortedBlocks to retarget to existing
     * vertex.
     */
    public void setTarget (Vertex v) {
        checkForNull(v, "target");
        if ( !(source_ instanceof Exit || v instanceof Entry) 
                            && (v.getGraph() != source_.getGraph())) {
            /////////////////////////////////////////////////////////
            // DEBUG -- this is a real problem but needs some thought.
            // Fix it and put the exception back.
            // //////////////////////////////////////////////////////
            System.out.println("* WARNING * Edge {" + toString() 
                + "}\n    being retargeted to vertex " + v);
            // END
            //throw new IllegalArgumentException ("target in different graph");
        }
        target_ = v;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public static void checkForNull(Object o, String what) {
        if (o == null) {
            throw new IllegalArgumentException ("null " + what);
        }
    }
    /** 
     * @return the graph that this edge is in.
     */
    public Directed getGraph() {
        return source_.getGraph();
    }
    /** 
     * Insert a vertex with a UnaryConnector into an edge. 
     *
     * @param v The Vertex being inserted.
     */
    public void insert (Vertex v) {
        checkForNull (v, "vertex");
        if ( ! (source_.getGraph() == v.getGraph()) ) {
            throw new IllegalArgumentException (
                    "vertex is in another graph");
        }
        Connector vConn = v.getConnector();
        if (vConn == null) {
            throw new IllegalArgumentException(
                    "internal error: vertex has null connector");
        }
        Vertex oldTarget = target_;
        target_ = v;             // retarget this edge to v
        vConn.setTarget(oldTarget);
    }

    /** @return A String description of the edge, NOT newline-terminated. */
    public String toString () {
        String s = "\"" + source_ + "\"" + " -> " + "\"" + target_ + "\"";
        return s;
    }
    
    public void setIsLoopEntrance(){
    	isLoopEdge = true;
    }
    
    public void clearIsLoopEntrance(){
    	isLoopEdge = false;
    }
    
    public boolean isLoopEntrance(){
    	return isLoopEdge;
    }
}
