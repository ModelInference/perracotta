/* Visitor.java */

package org.quilt.graph;

/** 
 * Methods for visiting a Quilt directed graph.  Implementations
 * can assume that a walk across the graph using Walker will touch every 
 * vertex and edge in the target graph and in every subgraph once 
 * and only once.  Entry and exit points are in their graphs.
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public interface Visitor {
    /** Called at beginning of visiting a graph or subgraph. */
    public void discoverGraph (Directed graph);
    
    /** Called at end of visiting a graph or subgraph. */
    public void finishGraph   (Directed graph);
    
    /** 
     * Called when beginning visit to vertex. If the vertex is
     * the entry point for a subgraph, discoverGraph for that 
     * subgraph must be called during the visit.
     */
    public void discoverVertex(Vertex vertex);
    /** 
     * Called at end of vertex visit. If the vertex is an exit
     * point for a subgraph, finishGraph for the subgraph must
     * be called during the visit.
     */
    public void finishVertex  (Vertex vertex);
    
    /** Called when initially visiting edge. */
    public void discoverEdge  (Edge edge);
    
    /** Called at end of visit to edge. */
    public void finishEdge    (Edge edge);
}
