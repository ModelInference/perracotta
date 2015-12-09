/* Walker.java */

package org.quilt.graph;

import java.util.HashSet;

/**
 * Walks a Visitor through a Quilt directed graph, visiting each 
 * vertex and edge once and only once.  Any subgraphs are visited
 * with the same guarantee.
 *
 * @author <a href="jdd@dixons.org">Jim Dixon</a>
 */

public class Walker {

    private Directed graph    = null;
    private Entry    entry    = null;
    private Exit     exit     = null;
    private Visitor  visitor  = null;
    private HashSet  vertices = new HashSet();
    private HashSet  edges    = new HashSet();

    /** No-arg constructor.  */
    public Walker() { }

    // METHODS //////////////////////////////////////////////////////
    /**
     * Walk through the entire graph. 
     *
     * @param graph   The graph we are walking.
     * @param guest   Agent which does something as we walk the graph.
     * @return        Reference to the exit Vertex of the graph.
     */
    public Exit visit (Directed g, Visitor guest) {
        Directed.checkForNull(g, "graph");
        Directed.checkForNull(guest, "Visitor");
        graph   = g;
        entry   = g.getEntry();
        exit    = g.getExit();
        visitor = guest;
        visitor.discoverGraph(graph);
        visitVertex( (Vertex) graph.getEntry() );
        visitor.finishGraph(graph);
        return exit;
    }
    
    /**
     * Visit a vertex, and in so doing visit all attached edges.
     *
     * @param v       A vertex in that graph.
     */
    private void visitVertex (Vertex v) {
        Directed.checkForNull(v, "vertex");
        if (vertices.contains(v)) {
            // we have already been here 
            return;
        }
        vertices.add(v);                // mark this node as visited
      
        if ( v == exit) {
            visitor.discoverVertex(v);  // let guest do his business

            // Allow Walker to visit Edge to graph Entry.  Can have no 
            // practical benefit; added primarily for aesthetic reasons.  
            // Used to cause an infinite loop.
            Edge e = v.getEdge();
            if (e.getTarget().getGraph() == graph) {
                visitEdge (v.getEdge()); 
            }
            visitor.finishVertex(v);
        } else {
            if (v != entry && v instanceof Entry) {
                // it's the entry point into a subgraph
                Walker subWalker = new Walker();
                Exit subExit = subWalker.visit (v.getGraph(), visitor);
                visitVertex ( 
                    ((UnaryConnector)subExit.getConnector())
                        .getEdge().getTarget() );    
            } else {
                // it's a vertex in this graph
                visitor.discoverVertex(v);  // let guest do his business
                // get outbound edges and visit each in turn; the
                // preferred edge is always visited first
                Connector conn = v.getConnector();
                // visit the preferred edge
                visitEdge ( conn.getEdge() );
                if (conn instanceof BinaryConnector) {
                    visitEdge ( ((BinaryConnector)conn).getOtherEdge());
                } else if (conn instanceof ComplexConnector) {
                    int size = conn.size();
                    for (int i = 0; i < size; i++) {
                        visitEdge ( ((ComplexConnector)conn).getEdge(i));
                    }
                } else if (conn instanceof UnaryConnector) {
                    // do nothing
                } else if (conn instanceof MultiConnector) {
                    // preferred edge 0 already visited
                    for (int i = 1; i < conn.size(); i++) {
                        visitEdge ( ((MultiConnector)conn).getEdge(i));
                    }
                } else {
                    System.out.println (
                        "Walker.visitVertex: INTERNAL ERROR\n" +
            "    don't know how to handle this kind of connection");
                }
                visitor.finishVertex(v);
            }
        } 
    } 
    /**
     * @param e       An edge in this graph.
     */
    private void visitEdge(Edge e) {
        Directed.checkForNull(e, "edge");
        if (edges.contains(e)) {
            return;                 // already been here
        }
        edges.add(e);               // mark as visited
        
        visitor.discoverEdge(e);
        Vertex target = e.getTarget(); 
        Directed.checkForNull(target, "edge target");

        // XXX CODE NEEDS REWORKING /////////////////////////////////
        if ( target instanceof Entry ) {
            // if the target is an Entry vertex, visit it only if it is
            // in a different graph which is not the parent of this graph
            Vertex source = e.getSource();
            Directed sourceGraph = source.getGraph();
            Directed targetGraph = e.getTarget().getGraph();
            if (sourceGraph != targetGraph 
                            && targetGraph != sourceGraph.getParent() ) {
                visitVertex(target);
            }
        } else if ( target instanceof Exit ) {
            // if the target is an Exit vertex, visit it only if it is
            // in the same graph 
            Directed sourceGraph = e.getSource().getGraph();
            Directed targetGraph = e.getTarget().getGraph();
            if (sourceGraph == targetGraph ) {
                visitVertex(target);
            }
        } else {
            visitVertex(target);
        }
        visitor.finishEdge(e);
    }
}

