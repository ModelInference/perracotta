/* ControlFlowGraph.java */
package org.quilt.cl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.bcel.generic.InstructionList;
import org.quilt.graph.*;

/**
 * Directed graph extended for use in analyzing method instruction
 * lists. 
 * 
 * @author <a href="jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class ControlFlowGraph extends Directed {

    /** Key code vertex, value handle on first instruction. */
    protected Map startHandles      = null;
    /** Key code vertex, value handle on last instruction. */
    protected Map endHandles        = null;
    /** Key source vertex, value target vertex where connecting 
     *  instruction is GotoInstruction. */
    protected Map gotoFixMeUps      = null;
    /** Instruction list built in walking the graph. */
    protected InstructionList ilist = null;
   
    protected HashSet vSet = new HashSet();
    
    protected HashSet eSet = new HashSet();
    
    /** Get a reference to the map of handles on first instructions. */
    public Map getStartHandles() {
        return startHandles;
    }
    /** Get a reference to the map of handles on last instructions. */
    public Map getEndHandles() {
        return endHandles;
    }
    /** Get a reference to the source/target vertex map where the
     * connecting instruction is a GotoInstruction. */
    public Map getGotoFixMeUps() {
        return gotoFixMeUps;
    }
    /** Get a reference to the instruction list built while walking the
     * graph. */
    public InstructionList getInstructionList() {
        return ilist;
    }

    /** Create a new top level control flow graph. */
    public ControlFlowGraph () {
        super();
        startHandles = new HashMap();
        endHandles   = new HashMap();
        gotoFixMeUps = new HashMap();
        ilist        = new InstructionList();
    }
    /** 
     * Create a control flow graph without connecting it to this
     * parent graph but sharing protected data structures.
     * @param parent The cfg one level up.
     */
    protected ControlFlowGraph( ControlFlowGraph parent) {
        super(parent);
        startHandles = parent.startHandles;
        endHandles   = parent.endHandles;
        gotoFixMeUps = parent.gotoFixMeUps;
        ilist        = parent.ilist;
    }
    /**
     * Create a control flow graph, connecting it to this graph as
     * its parent by inserting it along the directed edge e (the
     * Entry first, followed by the subgraph Exit.
     * 
     * @param e Edge along which the subgraph is to be inserted.
     * @param n Number of extra edges on the ComplexConnector in 
     *          the subgraph's Entry vertex (must be at least 1).
     */
    public Directed subgraph (final Edge e, final int n) {
        return super.connectSubgraph( new ControlFlowGraph(this), e, n);
    }

    /**
     * Insert a code vertex along an edge, retargeting the edge to 
     * the vertex inserted.  The vertex is created by this method.
     * 
     * @param e Edge along which the vertex is inserted.
     * @return  The CodeVertex created.
     */
    public CodeVertex insertCodeVertex (Edge e) {
        return (CodeVertex) super.insertVertex ( new CodeVertex(this), e);
    }

    /**
     * Insert a pre-existing CodeVertex along an edge.  The edge
     * and the vertex must be in the same graph.
     *
     * @param v CodeVertex being inserted.
     * @param e Edge in which it is to be inserted.
     */
    public CodeVertex insertCodeVertex (CodeVertex v, Edge e) {
        return (CodeVertex) super.insertVertex (v, e);
    }
    
    public HashSet getVSet(){
    	return vSet;
    }
    
    public HashSet getESet(){
    	return eSet;
    }
}
