/* Directed.java */
package org.quilt.graph;

/**
 * A graph consisting of vertices connected by directed, weighted edges.  
 * The graph is guaranteed to have at least an entry and an exit point 
 * and to always be well-formed, in the sense that 
 * <ul> 
 * <li>there will be a path from the entry vertex to any other vertex 
 *      in the graph, and </li>
 * <li>there will be a path from any vertex in the graph to the exit
 *      vertex</li>
 * </ul>
 *
 * These graphs may be nested.  In such a case 
 * <ul>
 * <li>both graphs will be well-formed
 * <li>the entry and exit points of the subgraph are in the graph
 * <li>all paths from the entry point of the parent to a point in the
 *      subgraph will pass through the entry point of the subgraph
 * <li>all paths from a vertex within the subgraph to a vertex in the 
 *      parent graph will pass through the exit vertex of the subgraph
 * </ul>
 *
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class Directed {

    /** Entry vertex. */
    private Entry entry = null;
    /** Exit vertex. */
    private Exit  exit  = null;

    /** Index of most recently built graph. */
    protected static int graphIndex = 0;
    /** Index of this graph. */
    private int index = 0;

    /** Parent graph, if any */
    private Directed parent_ = null;
    private int depth  = 0;
    private int vCount = 0;             // number of vertices in graph
    private int eCount = 0;
    
    // CONSTRUCTORS /////////////////////////////////////////////////
    /** 
     * Builds a root directed graph with two vertices and two edges.  The
     * two vertices are Entry and Exit types.  There is an edge from
     * entry to exit and another from exit back to entry.  Each is 
     * constained in a UnaryConnector. Vertices are added to the graph 
     * by inserting them along the entry-to-exit edge.
     *
     * @see Connector
     * @see Edge
     */
    public Directed ( ) {
        index = graphIndex = 0;
        entry = new Entry(this);        // creates Exit
        exit  = (Exit) entry.getTarget();
    }
    /** @return The parent graph to this graph, or null if there is none. */
    public Directed getParent() {
        return parent_;
    }
    /** @return The zero-based index of this graph. */
    public int getIndex() {
        return index;
    }
    // SUBGRAPHS //////////////////////////////////////////
    /** 
     * Subgraph constructor; will have depth one more than parent. 
     *
     * @param parent Graph in which this is a subgraph.
     * @param n      Number of extra edges
     */
    protected Directed (Directed parent) {
        index = ++ graphIndex;
        entry = new Entry(this);        // creates Exit
        exit  = (Exit) entry.getTarget();
        checkForNull (parent, "parent");
        depth   = parent.getDepth() + 1;
        parent_ = parent;
    }
    /**
     * Inserts a subgraph into an edge, putting the entry and exit points
     * on the edge presented.  On exit the original edge has been 
     * retargeted to the Entry of the subgraph.
     *
     * @param e  An edge in the parent graph.
     * @return A reference to the subgraph.
     */
    final static protected Directed connectSubgraph (final Directed subgraph, 
                                        final Edge e, final int n) {
        checkForNull(e, "edge");
        if ( n < 1 ) {
            throw new IllegalArgumentException (
                    "out of range argument");
        }
        // Directed sub = new Directed(this);
        Entry subEntry = subgraph.getEntry();
        subEntry.setConnector(
            new ComplexConnector ( subEntry.getConnector(), n) );

        // connect graph to parent - first the entry
        Vertex target = e.getTarget();      // current target
        e.setTarget(subEntry);              // retarget e to subgraph entry
        
        // then the exit edge is retargeted to the original target
        subgraph.getExit().getEdge().setTarget(target);
        return subgraph;
    }
    /**
     * Constructs a subgraph and inserts it into the parent graph
     * on the edge presented.  This is a wrapper around the method
     * that does the connecting; when extending the class, override
     * the wrapper.
     *
     * @param e  An edge in the parent graph.
     * @return A reference to the subgraph.
     */
    public Directed subgraph (final Edge e, final int n) {
        return connectSubgraph (new Directed(this), e, n);
    }
    // ACCESSOR METHODS /////////////////////////////////////////////
    /** @return The depth of this graph. */
    public int getDepth() {
        return depth;
    }
    /** @return The entry vertex of this graph. */
    public Entry getEntry () {
        return entry;
    }
    /** @return The exit vertex of this graph. */
    public Exit getExit() {
        return exit;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    public static void checkForNull (Object o, String what) {
        if (o == null) {
            throw new IllegalArgumentException ("null " + what);
        }
    }
    /**
     * Step edge count.
     * @param e Edge being added.  Ignored at the moment.
     */
    public int anotherEdge ( Edge e ) {
        return (eCount++);
    }
    /** 
     * Step count of vertices .
     * @param v Vertex being added.  Being ignored at the moment.
     */
    public int anotherVertex( Vertex v ) {
        return (vCount++);
    }
    /** 
     * Insert a (new) Vertex into the graph along the edge provided.
     * After this operation the target of the edge will be the new
     * vertex.
     *
     * @param v Vertex to be inserted.
     * @param e Edge it is to be inserted along.
     */
    final protected Vertex insertVertex (Vertex v, Edge e) {
        checkForNull (e, "edge");
        Vertex source = e.getSource();
        if ( ! (source instanceof Exit) && source.getGraph() != this) {
            // DEBUG
            System.out.println("Directed.insertVertex:"
                + "\n    vertex:  " + v
                + "\n    edge:    " + e);
            // END
            throw new IllegalArgumentException ("edge not in this graph");
        }
        Vertex target = e.getTarget();
        e.setTarget(v);
        v.setConnector ( new UnaryConnector (new Edge(v, target)));
        return v;
    }
    /** 
     * Create a new Vertex with a Unary connector and insert into
     * this graph's edge e.
     */
    public Vertex insertVertex (Edge e) {
        return insertVertex (new Vertex(this), e);
    }
    /** */
    private class Sizer implements Visitor {
        private int graphCount  = 0;
        private int maxDepth    = -1;
        private int vertexCount = 0;
        private int edgeCount   = 0;

        public Sizer() { }
        
        public void discoverGraph (Directed g) {
            checkForNull (g, "graph");
            int depth = g.getDepth() + 1;
            graphCount++;
            if (depth > maxDepth) {
                maxDepth = depth;
            }
        }
        public void finishGraph   (Directed g) { }
        public void discoverVertex(Vertex v) {
            checkForNull(v, "vertex");
            vertexCount++;
        }
        public void finishVertex  (Vertex v) { }
        public void discoverEdge  (Edge e) {
            checkForNull (e, "edge");
            edgeCount++;
        }
        public void finishEdge    (Edge e) { }
        // ACCESSOR METHODS ///////////////////////////////
        public int getGraphCount  () { return graphCount;  }
        public int getMaxDepth    () { return maxDepth;    }
        public int getVertexCount () { return vertexCount; }
        public int getEdgeCount   () { return edgeCount;   }
    }
    public int size() {
        Walker johnny  = new Walker();
        Sizer  counter = new Sizer();
        // Exit ignored = 
                         johnny.visit (this, counter);
        return counter.getVertexCount();
    }

    /**
     * If the edge points towards a vertex in a graph which is enclosed
     * within the current graph, return a reference to the closest Entry.
     * The vertex might be within a nested subgraph.  If it is not in a
     * descendent graph, return null.
     * 
     * @param e Edge towards vertex in lower-level graph.
     * @param g Candidate lower-level graph.
     * @return  A reference to the nearest Entry point or null.
     */
    public Entry closestEntry (final Directed g) {
        // DEBUG
        //System.out.println ("Directed.closestEntry for " + g.getIndex() 
        //    + " from " + getIndex() );
        // END
        if (g == null) {
            throw new IllegalArgumentException ("null argument");
        }
        if ( g == this ) {
            return null;
        }
        Directed hisGraph;
        for (hisGraph = g; hisGraph != null; 
                                    hisGraph = hisGraph.getParent() ) {
//          // DEBUG
//          if (hisGraph.getParent() != null) {
//              System.out.println("    checking graph " 
//                                  + hisGraph.getParent().getIndex() );
//          } else {
//              System.out.println("    checking null graph ;-) ");
//          }
//          // END
            if (hisGraph.getParent() == this) {
//              // DEBUG
//              System.out.println(
//                      "    match at graph " + hisGraph.getIndex());
//              
//              // END
                return hisGraph.getEntry();
            }
        }
        return null;
    } 
}
