/* SortedBlocks.java */

package org.quilt.cl;

import java.util.*;
import org.quilt.graph.*;

/**
 * Manages an index into the bytecode vertices in a method's control flow 
 * graph.  The vertices (blocks) are indexed by their position in the 
 * original code.  The blocks may be from different graphs in a set of 
 * nested graphs.
 *
 * @author < a href="jdd@dixons.org">Jim Dixon</a>
 */
public class SortedBlocks {

    private ControlFlowGraph  graph;    // control flow graph for a method
    private SortedMap blox;     // index of vertices by position

    /** Creates an empty map. */
    public SortedBlocks () {
        blox = new TreeMap();
    }
    
    /** 
     * Add a vertex at its bytecode position.  * It is an
     * error if there is already a vertex at this position.
     *
     * XXX SHOULD THROW EXCEPTION IN SUCH A CASE.
     *
     * @param  v    CodeVertex to be inserted
     * @return True if the operation succeeds, false if something is
     *                  already present at that position.
     */
    public boolean add (final CodeVertex v) {
        int pos = -1;
        if (v == null) {
            throw new IllegalArgumentException (
                    "attempt to add null vertex");
        }
        pos = v.getPosition();
        if (pos < 0) {
            throw new IllegalArgumentException (
                    "vertex has invalid position");
        }
        Integer p = new Integer(pos);
        if (blox.containsKey (p) ) {
            return false;               // operation failed
        } else {
            blox.put (p, v);
            return true;
        }
    }

    /** 
     * Find or create a code vertex starting at a given position.
     *
     * @param pos Byte offset in the original bytecode.
     * @param e   If the vertex must be created, Edge into which it
     *              gets inserted.  Otherwise, edge target becomes
     *              the existing vertex.
     */
    public CodeVertex find ( final int pos, ControlFlowGraph currGraph, 
                                                            Edge e) {
        Integer p = new Integer(pos);
        if (blox.containsKey (p) ) {
            CodeVertex v = (CodeVertex) blox.get(p);
            ControlFlowGraph vGraph = (ControlFlowGraph)v.getGraph();
            Entry x;
            if ( vGraph == currGraph ) {
                e.setTarget(v);         // point the edge at this vertex
            } else if ( (x = currGraph.closestEntry(vGraph)) != null) {
                // if v is in a lower level graph, connect the edge
                // to the netwise closest Entry
                e.setTarget(x);
            } else {
                // if v is in any graph which is not this graph or a 
                // child, we get there through the current graph's Exit 
                e.setTarget(currGraph.getExit());
            }
            return v;
        } else {
            return add (pos, e);
        }
    }
    /**
     * Add a vertex at bytecode offset pos along edge e.  No other 
     * vertex with that bytecode offset may exist.
     *
     * @param pos Bytecode offset.
     * @param e   Edge along which the Vertex will be created
     * @return    Reference to the Vertex created.
     */
    public CodeVertex add (final int pos, final Edge e) {
//      // DEBUG
//      System.out.println(
//          "- - - - - - - - - - - - - - - - - - - - - - - - - \n"
//          + "SortedBlocks.add: pos " + pos + " along edge " + e);
//      // END
        Integer p = new Integer(pos);
        Vertex source_ = e.getSource();
        CodeVertex v;
        if ( source_ instanceof Exit) {
            v = ((ControlFlowGraph)e.getTarget().getGraph())
                                                .insertCodeVertex(e);
        } else {
            v = ((ControlFlowGraph)source_.getGraph())
                                                .insertCodeVertex(e);
        }
        v.setPos(pos);
//      v.getConnector().setData ( new ConnData() );
        blox.put (p, v);
//      // DEBUG
//      System.out.println(
//          "    after insertion edge becomes " + e
//          + "\n- - - - - - - - - - - - - - - - - - - - - - - - - ");
//      // END
        return v;
    }
    /** Does a vertex exist with this bytecode offset? */
    public boolean exists (final int pos) {
        Integer p = new Integer(pos);
        return blox.containsKey(p);
    }
    /** 
     * Find the code vertex starting at a given bytecode offset.
     * The vertex must exist.  XXX Should throw an exception if
     * it doesn't.
     *
     * @param pos Bytecode offset of first instruction in the block.
     * @return    The matching vertex.
     */
    public CodeVertex get (final int pos) {
        Integer p = new Integer (pos);
        if (!blox.containsKey (p) ) {
            throw new GraphBuildException ( 
                    "INTERNAL ERROR - no vertex at " + pos);
        }
        return (CodeVertex) blox.get(p);
    }
    /**
     * How many code vertices are currently in the index?
     *
     * @return The number of vertices in the index.
     */
    public int size() {
        return blox.size();
    }

    /** 
     * Standard toString(), XXX needs some work.
     *
     * @return Roughly formatted table of vertices.
     */
    public String toString() {
        // the vertices are keyed by position
        Iterator vertices = blox.keySet().iterator();
        String s = "vertex  position / instructions\n";
        while (vertices.hasNext()) {
            Integer position = (Integer) vertices.next();
            CodeVertex v = (CodeVertex) blox.get( position );
            s += "  " + v.getIndex()
               + "     " + position + "\n"
               + v      // possibly problems here ...
               ;
        }
        return s;
    } 
}
