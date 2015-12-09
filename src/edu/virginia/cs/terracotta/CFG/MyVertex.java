/*
 * Created on 2004-8-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.cs.terracotta.CFG;

import java.util.Vector;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;

/**
 * @author jy6q
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MyVertex {
	public final static byte WHITE = -1;

	public final static byte GRAY = 0;

	public final static byte BLACK = 1;

	/** Unique non-negative assigned to the Vertex; -1 means 'unassigned' */
	private int index = -1;

	private int graphindex = -1;
	
	private byte color = WHITE;

	private Instruction inst = null;

	private boolean onMainPath = false;

	private boolean isLoopEnter = false;

	private Vector pred = null;

	private Vector post = null;

	public MyVertex() {
		index = -1;
		graphindex = -1;
		onMainPath = false;
		isLoopEnter = false;
		color = WHITE;
		pred = new Vector();
		post = new Vector();
		pred.clear();
		post.clear();
	}

	public MyVertex(MyVertex v) {
		index = v.index;
		graphindex = v.graphindex;
		color = v.color;
		inst = v.inst;
		onMainPath = v.onMainPath;
		isLoopEnter = v.isLoopEnter;
		pred = new Vector();
		post = new Vector();
		pred.clear();
		post.clear();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer().append(graphindex);
		sb.append(":");
		sb.append(index);
		if (inst != null) {
			sb.append(": ");
			sb.append(inst);
		}
		return sb.toString();
	}

	public String toString(ConstantPool cp) {
		StringBuffer sb = new StringBuffer().append(graphindex);
		sb.append(":");
		sb.append(index);
		if (inst != null) {
			sb.append(": ");
			sb.append(inst.toString(cp));
		}
		return sb.toString();
	}

	public void setIndex(int i) {
		index = i;
	}

	public int getIndex() {
		return index;
	}
	
	public void setGraphIndex(int i) {
		graphindex = i;
	}

	public int getGraphIndex() {
		return graphindex;
	}
	
	public void setInst(Instruction inst) {
		this.inst = inst;
	}

	public Instruction getInst() {
		return inst;
	}

	public void setOnMainPath() {
		onMainPath = true;
	}

	public void clearOnMainPath() {
		onMainPath = false;
	}

	public boolean isOnMainPath() {
		return onMainPath;
	}

	public void setIsLoopEntrance() {
		isLoopEnter = true;
	}

	public void clearIsLoopEntrance() {
		isLoopEnter = false;
	}

	public boolean isLoopEntrance() {
		return isLoopEnter;
	}

	public void setColor(byte i) {
		if ((i < WHITE) || (i > BLACK))
			throw new IllegalArgumentException("setColor: " + i);
		color = i;
	}

	public int getColor() {
		return color;
	}

	public void setPred(Vector pred) {
		this.pred = pred;
	}

	public Vector getPred() {
		return pred;
	}

	public void setPost(Vector post) {
		this.post = post;
	}

	public Vector getPost() {
		return post;
	}
	
	public boolean isInvoke(){
		if (!isInst())
			return false;

		if (!(inst instanceof InvokeInstruction))
			return false;
		
		return true;
	}
	
	public boolean isInst(){
		if (inst == null)
			return false;
		return true;
	}
	
	public boolean isConstructor(ConstantPoolGen cpGen){
		if (!isInvoke())
			return false;
		String methodName = ((InvokeInstruction) inst).getMethodName(cpGen); 
		if(methodName.matches("<.*>"))
			return true;
		return false;
	}
}