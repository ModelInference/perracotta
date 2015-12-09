/* TryStacks.java */

package org.quilt.cl;

import java.util.*;
import org.apache.bcel.generic.*;

import org.quilt.graph.*;

/** 
 * Manages try/catch blocks.  Adds subgraphs to the method 
 * graph for each exception handler, building the graph that 
 * GraphTransformer hangs bytecode off.  
 *
 * This module must cope with the fact that the compiler allocates exception 
 * handlers in no particular order.
 * 
 * Hacked from earlier 0.5-compatible code.  
 *
 * @author < a href="jdd@dixons.org">Jim Dixon</a>
 */
public class TryStacks {

    private ControlFlowGraph     graph = null;
    private SortedBlocks blox  = null;

    private int handlerCount = 0;
   
    private int index;                  // index into the arrays that follow
    private int tryStart[];             // bytecount position ...
    private int tryEnd[];               //      inclusive
    private int handlerPC[];            // start of catch blocks
    private ObjectType exception[];    
    private boolean done [];            // debugging only?

    /** Hash of bytecode offsets of end of try blocks. */
    private Map tryEndNdx = new HashMap();

    /** 
     * Comparator for exception handlers.  These need to be sorted
     * by tryStart (offset of beginning of try block) in ascending
     * order, then by tryEnd (offset of end of try block) in
     * descending order, then by handlerPC in ascending order.  
     */
    private class CmpHandlers implements Comparator {
        /** Implementation of compare.
         * @param o1 first handler in comparison
         * @param o2 second
         * @return -1 if o1 < o2, 0 if 01 == o2, 1 if o1 > o2
         */
        public int compare (Object o1, Object o2) {
            CodeExceptionGen a = (CodeExceptionGen) o1;
            CodeExceptionGen b = (CodeExceptionGen) o2;

            // -1 if a < b, 0 if a = b, 1 if a > b, in some sense
            int aStart = a.getStartPC().getPosition();
            int bStart = b.getStartPC().getPosition();
            // ascending order of start offset
            if (aStart < bStart) {
                return -1;
            } else if (aStart > bStart) {
                return 1; 
            }
            // descending order of end offset
            int aEnd   = a.getEndPC().getPosition();
            int bEnd   = b.getEndPC().getPosition();
            if (aEnd < bEnd) {
                return 1;
            } else if (aEnd > bEnd) {
                return -1;
            }  
            // ascending order of handler offset
            int aHandler = a.getHandlerPC().getPosition();
            int bHandler = b.getHandlerPC().getPosition();
            if (aHandler < bHandler) {
                return -1;
            } else if (aHandler > bHandler) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    
    // CONSTRUCTOR //////////////////////////////////////////////////
    /** 
     * Constructor setting up try/catch arrays.  Sorts the exception
     * handlers and then builds a nested control flow graph, including
     * the first code vertex in each try block who first vertex is a
     * code vertex.
     *
     * @param handlers Array of exception handlers for a method.
     * @param blocks   Vertices indexed by position in bytecode (?).
     * @param g        Graph for the method.
     */
    public TryStacks (
            final CodeExceptionGen[] handlers, 
            SortedBlocks blocks, 
            ControlFlowGraph g) {
        if (handlers == null || blocks == null || g == null) {
            throw new IllegalArgumentException("null constructor argument");
        }
        blox    = blocks;
        graph   = g;

        handlerCount = handlers.length;
        if (handlerCount > 0) {
            tryStart  = new int[handlerCount];
            tryEnd    = new int[handlerCount];
            handlerPC = new int[handlerCount];
            exception = new ObjectType[handlerCount];
            done      = new boolean [handlerCount];
            
            // sort the handlers first by position of beginning of
            // try block, then by position of end of try block, then
            // by handler address
            SortedMap sm = new TreeMap(new CmpHandlers());
            for (int i=0; i<handlerCount; i++) {
                sm.put ( handlers[i], new Integer(i) );
            }
            Iterator it = sm.keySet().iterator();
            for (int j = 0; it.hasNext(); j++ ) {
                Integer iInt = (Integer) sm.get ( 
                                            (CodeExceptionGen) it.next() );
                int i = iInt.intValue();
                tryStart[j]   = handlers[i].getStartPC().getPosition();
                tryEnd[j]     = handlers[i].getEndPC().getPosition();
                handlerPC[j]  = handlers[i].getHandlerPC().getPosition();
                exception[j]  = handlers[i].getCatchType();

                done[j]       = false;
            }
            Edge edge = graph.getEntry().getEdge();
            for (int i = 0; i < handlerCount && !done[i]; /* */ ) {
                ControlFlowGraph sub = handleTry(graph, edge) ;
                edge = sub.getExit().getEdge();
            }
        } // if handlerCount > 0
    } 

    /** 
     * Initialize the graph and set up catch blocks for the i-th 
     * try block. 
     *
     * @param index Index into table of exception handlers, updated by this
     *                method.
     * @param g     Graph which will be parent of the graph created.
     * @param e     On entry, edge along which graph is created
     * @return      Subgraph created
     */
    private ControlFlowGraph handleTry (
                    final ControlFlowGraph g, final Edge parentEdge ) {
        int start = tryStart[index];
        int end   = tryEnd[index];
        if ( parentEdge == null) {
            throw new IllegalArgumentException("null edge");
        }
        // deal with tries with multiple exception handlers
        ControlFlowGraph subgraph   = handleTryGroup (g, parentEdge);
        Vertex   subEntry   = subgraph.getEntry();
        Edge     currEdge  = subEntry.getEdge();
        
        // deal with trys starting at the same bytecode offset
        ControlFlowGraph subsub;
        if ((index < handlerCount) && (tryStart[index] == start)) {
            subsub = handleTry (subgraph, currEdge);
            currEdge = subsub.getExit().getEdge();
        } else {
            // this was the most deeply nested try block starting at 
            // this offset, so bytecode gets assigned to vertex
            // hanging off the Entry's preferred edge.  Create that
            // vertex along currEdge.
            currEdge = blox.add (tryStart[index - 1], currEdge ).getEdge();
        }
        // other tries nested within this try block
        int nested = 0;
        while ((index < handlerCount) && (tryStart[index] < end)) {
            subsub = handleTry (subgraph, currEdge);
            currEdge = subsub.getExit().getEdge();
        }
        // set up tryEnd index by graph
        tryEndNdx.put(subgraph, new Integer(start));
        return subgraph;
    } 

    /**
     * Deal with a try block with one or more catch blocks. 
     *
     * @param i          Index into handler table, updated by this method.
     * @param parent     Parent graph.
     * @param parentEdge Edge along which subgraph is created.
     * @returns          Subgraph created.
     */
    private ControlFlowGraph handleTryGroup (final ControlFlowGraph parent, 
                                            final Edge parentEdge) {
       
        int k = 1;                  // number of catch blocks
        int pos = tryStart[index];
        int end = tryEnd[index];
        for (int j = index + 1; j < handlerCount 
                         && tryStart[j] == pos && tryEnd[j] == end;
                                                                j++) {
            // try blocks are identical
              k++;
        }
        // create a subgraph with a k-sized connector
        ControlFlowGraph subgraph 
                    = (ControlFlowGraph) parent.subgraph (parentEdge, k);
        Edge currentEdge  = subgraph.getExit().getEdge();

        // connect to catch blocks
        ComplexConnector conn 
                    = (ComplexConnector)subgraph.getEntry().getConnector();
        for (int j = 0; j < k; j++) {
            done[index + j] = true;
            Edge edge = conn.getEdge(j);
            CodeVertex v = subgraph.insertCodeVertex (edge);
            v.setPos (handlerPC[index + j] );
            // v.getConnector().setData ( new ConnData() );
            blox.add (v);
        }
        index += k; 
        return subgraph;
    }
    /** 
     * Return an array of CatchData, with vertices for the beginning
     * and end of the try block, a vertex for the handler, and the 
     * exception handled.
     *
     * @return Catch handler descriptions for the graph.
     */
    public CatchData [] getCatchData() {
        CatchData [] cd = new CatchData[tryStart.length];
        for (int i = 0; i < tryStart.length; i++) {
            cd [i] = new CatchData (blox.get(tryStart[i]), blox.get(tryEnd[i]),
                                    blox.get(handlerPC[i]), exception[i] );
        }
        return cd;
    }
    /**
     * Return the class TryStack uses to sort exception handlers.
     *
     * @return The comparator used to sort handlers. 
     */
    public Comparator getComparator() {
        return new CmpHandlers();
    }
    /**
     * Get the bytecode offset of end of the try block for this graph.
     *
     * @param graph A subgraph created by this package.
     * @return      The bytecode offset of the end of the try block for this
     *                  or -1 if there is no such graph.
     */
    public int getEndTry (final ControlFlowGraph graph) {
        if (tryEndNdx.containsKey(graph)) {
            Integer i = (Integer)tryEndNdx.get(graph);
            return i.intValue();
        }
        return -1;
    }
    /** 
     * @return Newline-terminated string description of try blocks and 
     * handlers. 
     */
    public String toString() {
        String s = "";
        if (handlerCount > 0) {
            // columns will not align properly for non-trivial cases
            s = "  index start end handler pc\n";
            for (int i = 0; i < handlerCount; i++) {
                s += "    " + i + "    [" + tryStart[i] 
                   + ".." + tryEnd[i] 
                   + "] --> " + handlerPC[i] + "\n";
            }
        }
        return s;
    }
}
