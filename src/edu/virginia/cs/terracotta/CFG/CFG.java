/*
 * Created on 2004-8-11
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.cs.terracotta.CFG;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ExceptionThrower;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.JSR;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.RET;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.quilt.cl.CodeVertex;
import org.quilt.cl.ControlFlowGraph;
import org.quilt.cl.SortedBlocks;
import org.quilt.cl.TryStacks;
import org.quilt.graph.ComplexConnector;
import org.quilt.graph.Edge;
import org.quilt.graph.Entry;
import org.quilt.graph.Exit;
import org.quilt.graph.UnaryConnector;
import org.quilt.graph.Vertex;
import org.quilt.graph.Connector;

/**
 * @author jy6q
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class CFG {
	private TryStacks ts = null;

	public static void main(String[] args) throws IOException, ClassFormatError {
		if (args.length != 1) {
			System.out.println("Usage: java CFG classfilename");
			System.exit(1);
		}

		JavaClass javaClass = null;
		ClassParser classParser = null;

		classParser = new ClassParser(args[0]);
		javaClass = classParser.parse();
		Method[] methods = javaClass.getMethods();
		String className = javaClass.getClassName();
		ConstantPoolGen cpGen = new ConstantPoolGen(javaClass.getConstantPool());
		ClassGen classGen = new ClassGen(javaClass);

		CFG t = new CFG();
		ControlFlowGraph cfg = null;
		MyCFG mycfg = null;
		for (int i = 0; i < methods.length; i++) {
			// Ignore Abstract methods
			if (methods[i].isAbstract())
				continue;
			// Ignore Constructors
			if (methods[i].getName().matches("<.*>"))
				continue;
			// Ignore resourcesStart()
			//if (methods[i].getName().matches("resourcesStart"))
			//	continue;
			// Ignore resourcesStop()
			//if (!methods[i].getName().matches("resourcesStop"))
			//	continue;
			//if (!methods[i].getName().matches("storeContext"))
			//	continue;
			
			MethodGen methodGen = new MethodGen(methods[i], className, cpGen);
			//System.out.println("Instruction list of method: "
			//		+ methods[i].getCode().toString(true));
			cfg = t.makeGraph(classGen, methodGen);
			//t.DFS(cfg);
			
			// Old Debug
//			String cfgStr = t.print(cfg, cpGen);
//			t.cfg2ps(cfgStr, className + "." + methods[i].getName());

			mycfg = new MyCFG(cfg, classGen, methodGen);
			mycfg.toPS();
			/*System.out.println("CFG of method: " + methods[i]);
			System.out.println(mycfg);
			System.out.println();
			*/

			/*System.out.println("Vertices that have no outgoing edges");
			Vector vertices = mycfg.getVertices();
			for (int k=0; k<vertices.size(); k++) {
				MyVertex v = (MyVertex) vertices.get(k);
				if(v.getPost().size() == 0)
					System.out.println(v.toString(classGen.getConstantPool().getConstantPool()));
			}*/
			
			/* FIXME this is a temporary work-around of the problem
			 * I Suspect that there is fault in the makeGraph method
			 */
			
			MyVertex Exit = mycfg.getExit();
			if(Exit == null){
				System.err.println(methods[i] + " does not have an exit point. So by-pass");
				continue;
			}
			System.out.println("cut points of method: " + methods[i]);
			Vector vertices = mycfg.getVertices();
			for (int k=0; k<vertices.size(); k++) {
				MyCFG mycfg2 = new MyCFG(mycfg);
				MyVertex v = (MyVertex) mycfg2.getVertices().get(k);
				/*if (mycfg2.isConnected())
					System.out.println("mycfg2 is originally connected");
				else
					System.out.println("mycfg2 is originally not connected");*/
				mycfg2.removeVertex(v);
				if (!mycfg2.isConnected()){
					System.out.println(v.toString(classGen.getConstantPool().getConstantPool()));
					System.out.println();
				}
				/*if (mycfg2.isConnected())
					System.out.println("mycfg2 is still connected after removing "+v);
				else
					System.out.println("mycfg2 is not connected after removing "+v);
				System.out.println();*/
				//System.out.println("CFG of method: " + methods[i]);
				//System.out.println(mycfg2);
				//System.out.println();
			}
			
		}
	}

	public ControlFlowGraph myMakeGraph(ClassGen clazz, MethodGen method) {
		//First call the original makeGraph method to create a CFG
		ControlFlowGraph cfg = makeGraph(clazz, method);

		//post-process the cfg
		removeGotoVirtualEdge(cfg);
		initializeVandESet(cfg);
		sortIndex(cfg);
		DFS(cfg);

		//DEBUG
		Iterator itr = null;
		System.out.println("\nVertices in method: " + method);
		itr = cfg.getVSet().iterator();
		while (itr.hasNext()) {
			Vertex currV = (Vertex) itr.next();
			if (currV instanceof CodeVertex)
				System.out.println(((CodeVertex) currV).toStringInst(clazz
						.getConstantPool().getConstantPool()));
			else
				System.out.println(currV);
			HashSet pred = currV.getPred();
			System.out.println("Its predecessors are:");
			Iterator i = pred.iterator();
			while (i.hasNext()) {
				Vertex predV = (Vertex) i.next();
				if (predV instanceof CodeVertex)
					System.out.println(((CodeVertex) predV).toStringInst(clazz
							.getConstantPool().getConstantPool()));
				else
					System.out.println(predV);
			}
			System.out.println();
		}
		System.out.println("\nEdges in method: " + method);
		itr = cfg.getESet().iterator();
		while (itr.hasNext())
			System.out.println((Edge) (itr.next()));
		//		System.out.println(print(cfg, clazz.getConstantPool()));

		return cfg;
	}

	public ControlFlowGraph makeGraph(ClassGen clazz, MethodGen method) {

		SortedBlocks blox = new SortedBlocks();
		CodeExceptionGen[] handlers = method.getExceptionHandlers();
		ControlFlowGraph cfg = new ControlFlowGraph();
		ControlFlowGraph currGraph = cfg;
		Edge e = cfg.getEntry().getEdge();
		ts = null;
		boolean startBlock = false;
		CodeVertex currV = null;
		LineNumberTable lineTab = method.getLineNumberTable(clazz
				.getConstantPool());
		if (handlers.length > 0) {
			// NEED TO ADJUST EDGE HERE
			ts = new TryStacks(handlers, blox, cfg);
		}
		if (blox.exists(0)) {
			// we must have a try block starting at 0
			currV = blox.get(0);
		} else {
			currV = blox.find(0, currGraph, e);
		}
		if (lineTab != null) {
			currV.setStartLine(lineTab.getSourceLine(0));
		}
		e = currV.getEdge();
		currGraph = (ControlFlowGraph) currV.getGraph();

		// Walk through the method's bytecode, appending it to the
		// current vertex, creating new vertices where necessary.

		InstructionList iList = method.getInstructionList();
		InstructionHandle currHandle = iList.getStart();
		Instruction inst = currHandle.getInstruction();
		int pos = currHandle.getPosition();
		// current vertex's InstructionList
		InstructionList vIList = currV.getInstructionList();

		while (currHandle != null) {
			if (startBlock) {
				startBlock = false;
				if (e == null) {
					if (!blox.exists(pos)) {
						// XXX this was formerly regarded as an
						// error; handling it like this makes it
						// clearer what the problem is, but we now get
						//     Falling off the end of the code
						currV = new CodeVertex(currGraph, pos);
					} else {
						currV = blox.get(pos);
					}
				} else {
					currV = blox.find(pos, currGraph, e);
				}
				if (lineTab != null) {
					currV.setStartLine(lineTab.getSourceLine(pos));
				}
				e = currV.getEdge();
				currGraph = (ControlFlowGraph) currV.getGraph();
				// DEBUG
				//System.out.println("makeGraph while; e = " + e);
				// END
				vIList = currV.getInstructionList();
				//currV.setConnInst(inst); //Jinlin Yang added this
			}
			if (inst instanceof GotoInstruction) {
				// to make the layout code (BytecodeCollector) work,
				// introduce the notion of a 'virtual' edge; there is
				// no flow of control, but the code along the virtual
				// edge will be laid out first
				Edge otherEdge = currV.makeBinary();
				currV.setConnInst(inst);
				int tpos = ((GotoInstruction) inst).getTarget().getPosition();
				int endTry;
				if (ts == null) {
					endTry = -1;
				} else {
					endTry = ts.getEndTry(currGraph);
				}
				if (endTry >= 0 && tpos > endTry) {
					// tpos is beyond end of try block and should be the
					// first code vertex following the subgraph Exit; in
					// any case the edge target becomes the Exit
					Exit currExit = currGraph.getExit();
					otherEdge.setTarget(currExit);
					if (!blox.exists(tpos)) {
						Vertex vFinal;
						for (vFinal = currExit; vFinal.getTarget() instanceof Entry; vFinal = vFinal
								.getTarget()) {
							;
						}
						blox.add(tpos, vFinal.getEdge());
					}
				} else {
					// tpos is within try block; make v target of e
					blox.find(tpos, currGraph, otherEdge);
				}
				// continue to use this 'virtual' edge
				startBlock = true;
				//// DEBUG
				//System.out.println("GraphTransformer: goto at end of "
				//+ currV);
				//// END

			} else if (inst instanceof IfInstruction || inst instanceof JSR) {
				Edge otherEdge = currV.makeBinary();
				currV.setConnInst(inst);
				// handle 'then' branch or target of JSR
				int tpos = ((BranchInstruction) inst).getTarget().getPosition();
				//  edge for 'then' vertex
				blox.find(tpos, currGraph, otherEdge);
				// continue to use the current edge
				startBlock = true; // ... but start a new block

			} else if (inst instanceof ReturnInstruction || inst instanceof RET) {
				currV.setConnInst(inst);
				e = null;
				startBlock = true;

			} else if (inst instanceof InvokeInstruction) {
				currV.setConnInst(inst);
				// continue to use the current edge
				startBlock = true; // ... but start a new block

			} else if (inst instanceof Select) {
				InstructionHandle[] targets = ((Select) inst).getTargets();
				//MultiConnector conn = currV.makeMulti(targets.length);
				ComplexConnector conn = currV.makeComplex(targets.length);
				currV.setConnInst(inst);
				for (int i = 0; i < targets.length; i++) {
					int tpos = targets[i].getPosition();
					blox.find(tpos, currGraph, conn.getEdge(i));
				}
				// EXPERIMENT IN HANDLING THE DEFAULT - seems to work ...
				InstructionHandle theDefault = ((Select) inst).getTarget();
				if (theDefault != null) {
					blox.find(theDefault.getPosition(), currGraph, conn
							.getEdge());
				}
				e = null; // it's an n-way goto
				startBlock = true;

			} else if (inst instanceof ExceptionThrower) {
				// Instructions which might or do (ATHROW) cause
				// an exception. XXX This needs to be looked at
				// more carefully! There are 22 such instructions
				// or groups; these include NEW, LDIV, and
				// ReturnInstruction. Splitting blocks here causes
				// a very large increase in the number of vertices;
				// the benefit is unclear.

				currV.setConnInst(inst);
				// continue along same edge
				startBlock = true;
			} else {
				vIList.append(inst); // add the instruction
			}
			InstructionHandle nextHandle = currHandle.getNext();
			if (nextHandle != null) {
				if (hasInbound(nextHandle)) {
					// This instruction is the target of a jump; start
					// a new block. Connector is set to flow by default.
					startBlock = true;
				}
			}
			if (startBlock == true) {
				if (lineTab != null) {
					currV.setEndLine(lineTab.getSourceLine(0));
				}
			}
			currHandle = nextHandle;
			if (currHandle != null) {
				pos = currHandle.getPosition();
				inst = currHandle.getInstruction();
			}
		}
		return cfg;
	}

	final public static boolean hasInbound(InstructionHandle ih) {
		if (ih.hasTargeters()) {
			InstructionTargeter targeters[] = ih.getTargeters();
			for (int j = 0; j < targeters.length; j++) {
				if (targeters[j] instanceof BranchInstruction) {
					return true;
				}
				if (targeters[j] instanceof CodeExceptionGen) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Traverse and print out the control flow graph
	 * 
	 * @author Jinlin Yang
	 * 
	 * TODO To change the template for this generated type comment go to Window -
	 * Preferences - Java - Code Style - Code Templates
	 */
	public String print(ControlFlowGraph cfg, ConstantPoolGen cpGen) {
		StringBuffer buf = new StringBuffer();
		Entry entry = cfg.getEntry();
		HashSet visited = new HashSet();
		LinkedList work = new LinkedList();
		work.add(entry);
		while (work.size() > 0) {
			Vertex v = (Vertex) work.removeFirst();
			if (visited.contains(v))
				continue;
			visited.add(v);
			//if (v instanceof Exit)
			//	continue;
			//System.out.println(v);
			Connector conn = v.getConnector();
			int size = conn.size();
			if (conn instanceof ComplexConnector)
				size++;
			for (int i = 0; i < size; i++) {

				Edge e = conn.getIthEdge(i);
				Vertex s = e.getSource();
				Vertex t = e.getTarget();
				if (s instanceof CodeVertex) {
					buf.append("\"" + s.getIndex() + ":");
					if (((CodeVertex) s).getConnInst() != null)
						buf.append(((CodeVertex) s).getConnInst().toString(
								cpGen.getConstantPool()).replaceAll("\"", "'"));
					//else
					//	buf.append(((CodeVertex) s).getInstructionList());
					buf.append("\"");
				} else
					buf.append("\"" + s + "\"");
				buf.append("->");
				if (t instanceof CodeVertex) {
					buf.append("\"" + t.getIndex() + ":");
					if (((CodeVertex) t).getConnInst() != null)
						buf.append(((CodeVertex) t).getConnInst().toString(
								cpGen.getConstantPool()).replaceAll("\"", "'"));
					//else
					//	buf.append(((CodeVertex) t).getInstructionList());
					buf.append("\"");
				} else
					buf.append("\"" + t + "\"");
				buf.append("\n");
				work.add(e.getTarget());
			}
		}
		return buf.toString();
	}

	public String printDebug(ControlFlowGraph cfg, ConstantPoolGen cpGen) {
		StringBuffer buf = new StringBuffer();
		Entry entry = cfg.getEntry();
		HashSet visited = new HashSet();
		LinkedList work = new LinkedList();
		work.add(entry);
		while (work.size() > 0) {
			Vertex v = (Vertex) work.removeFirst();
			if (visited.contains(v))
				continue;
			visited.add(v);
			if (v instanceof Exit)
				continue;
			//System.out.println(v);
			Connector conn = v.getConnector();
			for (int i = 0; i < conn.size(); i++) {
				Edge e = conn.getIthEdge(i);
				Vertex s = e.getSource();
				Vertex t = e.getTarget();
				buf.append("\"" + s + "\"");
				buf.append("->");
				buf.append("\"" + t + "\"");
				buf.append("\n");
				work.add(e.getTarget());
			}
		}
		return buf.toString();
	}

	public void cfg2ps(String cfg, String prefix) {
		File dot = new File(prefix + ".dot");
		try {
			if (dot.exists())
				dot.delete();
			RandomAccessFile dotRAF = new RandomAccessFile(dot, "rw");
			dotRAF.writeBytes("digraph{\n");
			dotRAF.writeBytes("ratio=fill\n");
			dotRAF.writeBytes("size=\"8,9\"\n");
			dotRAF.writeBytes(cfg);
			dotRAF.writeBytes("}");
			dotRAF.close();
			String cmd = "dot -Tps " + prefix + ".dot " + "-o " + prefix
					+ ".ps";
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	//
	public void sortIndex(ControlFlowGraph cfg) {
		int index = 0;
		Entry entry = cfg.getEntry();
		HashSet visited = new HashSet();
		LinkedList work = new LinkedList();
		work.add(entry);
		while (work.size() > 0) {
			Vertex v = (Vertex) work.removeFirst();
			if (visited.contains(v)) {
				continue;
			}
			visited.add(v);
			v.setIndex(index++);
			if (v instanceof Exit)
				continue;
			Connector conn = v.getConnector();
			for (int i = 0; i < conn.size(); i++) {
				Edge e = conn.getIthEdge(i);
				work.add(e.getTarget());
			}
		}
	}

	public void findMainPath(ControlFlowGraph cfg) {
		Entry entry = cfg.getEntry();
		HashSet visited = new HashSet();
		LinkedList work = new LinkedList();
		work.add(entry);
		while (work.size() > 0) {
			Vertex v = (Vertex) work.removeFirst();
			if (visited.contains(v))
				continue;
			v.setOnMainPath();
			visited.add(v);
			//Ignore the back edge from EXIT to ENTER
			if (v instanceof Exit)
				continue;
			Vertex nextV = findNextV(visited, v);
			//nextV.setOnMainPath();
			work.add(nextV);
		}
	}

	public Vertex findNextV(HashSet visited, Vertex currV) {
		// Check error state
		// currV should never be EXIT
		if (currV instanceof Exit) {
			(new Exception()).printStackTrace();
			System.exit(1);
		}

		// Check error state
		// currV should already in the visited vertex set
		if (!visited.contains(currV)) {
			(new Exception()).printStackTrace();
			System.exit(1);
		}

		Vertex nextV = null;
		Connector conn = currV.getConnector();
		LinkedList vlist = new LinkedList();

		// Found all none-loop-entrance edges
		for (int i = 0; i < conn.size(); i++) {
			Edge e = conn.getIthEdge(i);
			if (!e.isLoopEntrance())
				vlist.add(e.getTarget());
		}

		// Check error state
		// Any none-EXIT vertex should have at least one none-loop-entrance edge
		if (vlist.size() == 0) {
			System.err.println(currV);
			(new Exception()).printStackTrace();
			System.exit(1);
		}

		// Only one outgoing edge
		if (vlist.size() == 1)
			nextV = (Vertex) vlist.removeFirst();
		// Multiple outgoing edges
		else {
			boolean found = true;

			// First, check if the targets of those edges are same vertex
			// if they are, then we have found the aggregation point
			for (int i = 0; i < vlist.size() - 1; i++) {
				if (((Vertex) vlist.get(i)).getIndex() != ((Vertex) vlist
						.get(i + 1)).getIndex()) {
					found = false;
					break;
				}
			}

			// OK. if not found
			while (!found) {
				// We will recursively find the target of each vertex whose
				// predecessors have all been visited
				// until we found the aggregating point
				for (int i = 0; i < vlist.size() && !found; i++) {
					// process is used to indicate whether this vertex's
					// predecessors have all been visited
					boolean process = true;
					Vertex postV = (Vertex) vlist.get(i);
					Iterator itr = postV.getPred().iterator();
					while (itr.hasNext()) {
						Vertex preV = (Vertex) itr.next();
						if (!visited.contains(preV)) {
							process = false;
							break;
						}
					}

					if (process) {
						visited.add(postV);
						Vertex newPostV = findNextV(visited, postV);
						vlist.set(i, newPostV);

						found = true;
						for (int j = 0; i < vlist.size() - 1; i++) {
							if (((Vertex) vlist.get(j)).getIndex() != ((Vertex) vlist
									.get(j + 1)).getIndex()) {
								found = false;
								break;
							}
						}
					}
				}
			}
			nextV = (Vertex) vlist.get(0);
		}

		return nextV;
	}

	public String printMainPath(ControlFlowGraph cfg, ConstantPoolGen cpGen) {
		StringBuffer buf = new StringBuffer();
		HashSet vSet = cfg.getVSet();
		Iterator itr = vSet.iterator();
		while (itr.hasNext()) {
			Vertex currV = (Vertex) itr.next();
			if (!currV.isOnMainPath())
				continue;
			if (currV instanceof CodeVertex)
				buf.append(((CodeVertex) currV).toStringInst(cpGen
						.getConstantPool()));
			else
				buf.append(currV);
			buf.append("\n");
		}
		return buf.toString();
	}

	/**
	 * Perform a standard DFS search through the CFG to find all back edges
	 * [Cormen pp546]. The target vertex of a back edge is then an entrance node
	 * to loop. The corresponding forward edge [Cormen pp546] is then the edge
	 * that leads to the loop.
	 * 
	 * Whether an edge is an loop-leading edge is used when finding the main
	 * path of a CFG. That is, such edge is never tried. This essentially
	 * transforms an CFG into a DAG. This information is also used
	 * 
	 * @param cfg
	 */
	public void DFS(ControlFlowGraph cfg) {
		// Initialize all vertex's color to WHITE
		HashSet vSet = cfg.getVSet();
		Iterator itr = vSet.iterator();
		while (itr.hasNext())
			((Vertex) (itr.next())).setColor(Vertex.WHITE);

		Entry entry = cfg.getEntry();
		DFS_visit(entry);
	}

	public void DFS_visit(Vertex v) {
		// Need to handle the speical back edge from EXIT to ENTER
		if (v instanceof Exit) {
			v.setColor(Vertex.BLACK);
			return;
		}

		// Below is the typical process of DFS copied from [Cormen]
		v.setColor(Vertex.GRAY);
		Connector conn = v.getConnector();
		for (int i = 0; i < conn.size(); i++) {
			Edge e = conn.getIthEdge(i);
			Vertex t = e.getTarget();
			switch (t.getColor()) {
			case Vertex.WHITE:
				DFS_visit(t);
				t.getPred().add(v);
				break;
			case Vertex.GRAY:
				// OK. Found a back edge. So the target must be an entrance
				// point to the loop. And in this case, we don't add v to the
				// predecessor list of t.
				t.setIsLoopEntrance();
				System.out.println(t + ": is a loop entrance");
				break;
			case Vertex.BLACK:
				t.getPred().add(v);
				break;
			}
			// if we found a loop starting at vertex v when exploring edge i,
			// then edge i is the edge that leads to loop
			if (v.isLoopEntrance()) {
				conn.getIthEdge(i).setIsLoopEntrance();
				System.out.println(conn.getIthEdge(i) + ": is a loop edge");
				v.clearIsLoopEntrance();
			}
		}
		v.setColor(Vertex.BLACK);
	}

	public void printColor(ControlFlowGraph cfg) {
		Entry entry = cfg.getEntry();
		HashSet visited = new HashSet();
		LinkedList work = new LinkedList();
		work.add(entry);
		while (work.size() > 0) {
			Vertex v = (Vertex) work.removeFirst();
			if (visited.contains(v))
				continue;
			switch (v.getColor()) {
			case Vertex.WHITE:
				System.out.println(v + ": WHITE");
				break;
			case Vertex.BLACK:
				System.out.println(v + ": BLACK");
				break;
			case Vertex.GRAY:
				System.out.println(v + ": GRAY");
				break;
			}
			visited.add(v);
			if (v instanceof Exit)
				continue;
			Connector conn = v.getConnector();
			for (int i = 0; i < conn.size(); i++) {
				Edge e = conn.getIthEdge(i);
				Vertex t = e.getTarget();
				work.add(t);
			}
		}
	}

	private void removeGotoVirtualEdge(ControlFlowGraph cfg) {
		Entry entry = cfg.getEntry();
		HashSet visited = new HashSet();
		LinkedList work = new LinkedList();
		work.add(entry);
		while (work.size() > 0) {
			Vertex v = (Vertex) work.removeFirst();
			if (visited.contains(v))
				continue;
			visited.add(v);
			if (v instanceof Exit)
				continue;

			if (v instanceof CodeVertex)
				if (((CodeVertex) v).getConnInst() != null)
					if (((CodeVertex) v).getConnInst() instanceof GotoInstruction) {
						UnaryConnector newconn = new UnaryConnector(v
								.getConnector().getIthEdge(1));
						v.setConnector(newconn);
					}

			Connector conn = v.getConnector();
			for (int i = 0; i < conn.size(); i++) {
				Edge e = conn.getIthEdge(i);
				work.add(e.getTarget());
			}
		}
	}

	private void initializeVandESet(ControlFlowGraph cfg) {
		Entry entry = cfg.getEntry();
		HashSet visited = cfg.getVSet();
		HashSet eSet = cfg.getESet();
		LinkedList work = new LinkedList();
		work.add(entry);
		while (work.size() > 0) {
			Vertex v = (Vertex) work.removeFirst();
			if (visited.contains(v))
				continue;
			visited.add(v);
			if (v instanceof Exit)
				continue;
			Connector conn = v.getConnector();
			for (int i = 0; i < conn.size(); i++) {
				Edge e = conn.getIthEdge(i);
				eSet.add(e);
				work.add(e.getTarget());
			}
		}
	}
}