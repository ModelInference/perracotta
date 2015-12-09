/* CodeVertex.java */
package org.quilt.cl;

import org.quilt.graph.*;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

/**
 * A Vertex extended to carry the initial bytecode offset, line
 * number, and an instruction list.
 *
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon</a>
 */
public class CodeVertex extends Vertex {
    /** initial offset of first instruction in bytecode */
    protected int pos = -1;

    /** the bytecode iteself */
    protected InstructionList ilist = new InstructionList();

    /**
     * Line number in source code corresponding to first instruction,
     * or if there is no such instruction, of the connecting instruction.
     */
    protected int startLine_ = -1;

    /**
     * Line number in source code corresponding to the connecting 
     * instruction, or if there is no such instruction, to the last
     * instruction in the block
     */
    protected int endLine_   = -1;

    /** Instruction connecting this vertex to other(s). */
    protected Instruction connInst_ = null;

    /**
     * Create a code vertex with default bytecode offset, line number,
     * empty instruction list, and no label.
     *
     * @param g Graph which the vertex belongs to.
     */
    public CodeVertex (ControlFlowGraph g) {
        super(g);
    }
    /** Create a code vertex, specifying a non-negative bytecode offset.
     *
     * @param g        Graph which the vertex belongs to.
     * @param position Offset of the first instruction in the bytecode.
     */
    public CodeVertex (ControlFlowGraph g, int position) {
        super(g);
        if (position < 0) {
            throw new IllegalArgumentException(
                    "position cannot be negative");
        }
        pos   = position;
    }
    /**
     * Create a code vertex, specifying a label
     *
     * @param g Graph which the vertex belongs to.
     * @param l The String label applied to the vertex.
     */
    public CodeVertex (ControlFlowGraph g, String l) {
        super(g);
        pos    = -1;
        label_ = l;
    }
    // GET/SET METHODS //////////////////////////////////////////////
    /** Get connecting instruction. */
    public Instruction getConnInst() {
        return connInst_;
    }
    /** Set the connecting instruction for this vertex. */
    public void setConnInst (Instruction i) {
        if (i == null) {
            throw new IllegalArgumentException ("null instruction");
        }
        connInst_ = i;
    }
//  /** Set the connecting instruction to null. */
//  public void clearConnInst () {
//      connInst_ = null;
//  }
    /**
     * Get a reference to the InstructionList carried by the vertex.
     * This is a doubly indirect reference to the first instruction
     * in the list.
     *
     * @return Instruction list.
     */
    public InstructionList getInstructionList() {
        return ilist;
    }
    /**
     * Get the source code line number of the first instruction in a 
     * code vertex.
     *
     * @return Non-negative integer or -1, meaning no line number assigned.
     */
    public int getStartLine () {
        return startLine_;
    }
    /**
     * Set the source code line number.
     * @param n Source code line number.
     */
    public void setStartLine(int n) {
        startLine_ = n;
    }
    /** 
     * Get the line number in source code corresponding to the 
     * connecting instruction or last instruction in the block.
     */
    public int getEndLine() {
        return endLine_;
    }
    /** 
     * Set the source line number of the connecting instruction, or of
     * the last line number in the block if there is no connecting
     * instruction.  
     * 
     * @param n Source code end line number. 
     */
    public void setEndLine(int n) {
        endLine_ = n;
    }
    /**
     * Get the bytecode offset of the first instruction.
     * 
     * @return The initial bytecode offset of the first instruction
     *         carried by the vertex (excluding any connection instruction.
     */
    public int  getPosition () {
        return pos;
    }
    /**
     * Set the bytecode offset for the first instruction.
     *
     * XXX Should rename this to <code>setPosition</code> to match the
     * <code>get</code> method.
     *
     * @param position A non-negative integer representing the bytecode
     *                  position of the first instruction.
     */
    public void setPos (int position) {
        if (position < 0) {
            throw new IllegalArgumentException(
                    "position cannot be negative");
        }
        pos = position;
    }
    // OTHER METHODS ////////////////////////////////////////////////
    /** 
     * Move this code vertex's Goto to another code vertex.  The 
     * second vertex will be the target on the otherEdge from this
     * vertex.  This vertex has a BinaryConnector.  The second vertex
     * has a UnaryConnector.
     *
     * The goto instruction does NOT point to the target.  The target
     * is some sort of instrumentation being inserted into the graph.
     */
    public void moveGoto (final CodeVertex target) {
        if (target == null) {
            throw new IllegalArgumentException("null target vertex");
        }
        // this vertex's binary connector
        BinaryConnector biConnector = (BinaryConnector)getConnector();
        Edge flowEdge = biConnector.getEdge();
        Edge otherEdge = biConnector.getOtherEdge();    // used by goto
        
        if (otherEdge.getTarget() != target) {
            throw new IllegalArgumentException("not target of otherEdge");
        }
        if (! (connInst_ instanceof GotoInstruction) ) {
            throw new IllegalArgumentException(
                                        "connecting instruction not goto");
        }
        // the target vertex's unary connector
        UnaryConnector uConnector = (UnaryConnector)target.getConnector();
        Edge uEdge = uConnector.getEdge();
        Vertex tgtTarget = uEdge.getTarget();

//      // DEBUG
//      System.out.println("CodeVertex.moveGoto:"
//              + "\n    source:       " + toString()
//              + "\n      edge:       " + flowEdge
//              + "\n      other edge: " + otherEdge
//              + "\n    target:       " + target
//              + "\n      edge:       " + uEdge
//      );
//      // END
        
        // change the unary connector and move it to this vertex
        uEdge.setSource(this);
        uEdge.setTarget(target);
        setConnector(uConnector);

        // change the binary connector and attach it to the target
        flowEdge.setSource(target);   
        // flow target is unchanged
        otherEdge.setSource(target);
        otherEdge.setTarget(tgtTarget);
        target.setConnector(biConnector);

        // move the connecting instruction, a goto
        target.setConnInst (connInst_); // move it to the target
        connInst_ = null;               // erase from this vertex

//      // DEBUG
//      System.out.println("CodeVertex.moveGoto:"
//              + "\n    source: " + toString() 
//              + "\n        edge " + getEdge()
//              + "\n    target: " + target     
//              + "\n        edge "       + flowEdge
//              + "\n        other edge " + otherEdge );
//      // END
    }
    /** 
     * Less verbose <code>toString.</code>
     *
     * @return Graph index and Vertex index in a neatly formatted String,
     *         *not* newline-terminated.
     */
    public String toString () {
        StringBuffer sb = new StringBuffer().append("Code ")
            .append(super.toString()).append(" pos ") .append(pos);

        // may look a bit strange if there is an end line but no start line
        if (startLine_ > -1) {
            sb.append(" line ").append(startLine_);
        }
        if (endLine_ > -1) {
            sb.append("/").append(endLine_);
        }
        return sb.toString();
    }
    /**
     * Optionally more verbose method.
     * 
     * @param  b If true, add label (if any) and instruction list.
     * @return A neatly formatted String. 
     */
    public String toString (boolean b) {
        
        StringBuffer sb = new StringBuffer().append(toString());
        if (b) {
            if (label_ != null) {
                sb.append ("\n    label ") .append(label_);
            }
            sb.append("\n    ilist: ");
            InstructionHandle ih = ilist.getStart();
            while ( ih != null) {
                sb.append(ih.getInstruction());
            }
        }
        return sb.toString();
    }
    
    public String toStringInst (ConstantPool cp) {
    	String retval = String.valueOf(index);
    	if(connInst_ != null)
    		retval += ":" + connInst_.toString(cp);
    	return retval;
    }
}
