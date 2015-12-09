/*
 * Created on 2004-8-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.cs.terracotta.CFG;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.MethodGen;
import org.quilt.cl.CodeVertex;
import org.quilt.cl.ControlFlowGraph;
import org.quilt.graph.ComplexConnector;
import org.quilt.graph.Connector;
import org.quilt.graph.Edge;
import org.quilt.graph.Entry;
import org.quilt.graph.Exit;
import org.quilt.graph.Vertex;

/**
 * @author jy6q
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MyCFG {

	private Vector vertices = new Vector();

	private Vector edges = new Vector();

	private ClassGen classGen = null;

	private MethodGen methodGen = null;

	private MyVertex Entry = null;

	private MyVertex Exit = null;

	public MyCFG(ControlFlowGraph cfg, ClassGen classGen, MethodGen methodGen) {
		this.classGen = classGen;
		this.methodGen = methodGen;
		vertices.clear();

		HashMap old2new = new HashMap();
		HashMap new2old = new HashMap();
		// Start collecting all vertices. Store them in 'visited'
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

			// Create new vertex
			MyVertex v2 = new MyVertex();
			v2.setIndex(v.getIndex());
			v2.setGraphIndex(v.getGraph().getIndex());
			if (v instanceof CodeVertex)
				v2.setInst(((CodeVertex) v).getConnInst());
			vertices.add(v2);
			if ((v2.getGraphIndex() == 0) && (v2.getIndex() == 0))
				Entry = v2;
			if ((v2.getGraphIndex() == 0) && (v2.getIndex() == 1))
				Exit = v2;
			// Create the map between the old vertex and new vertex, which will
			// be used shortly to create the edges
			old2new.put(v, v2);
			new2old.put(v2, v);

			Connector conn = v.getConnector();
			int size = conn.size();
			if (conn instanceof ComplexConnector)
				size++;
			for (int i = 0; i < size; i++) {
				if (v instanceof CodeVertex)
					if (((CodeVertex) v).getConnInst() != null)
						if (((CodeVertex) v).getConnInst() instanceof GotoInstruction)
							if (i == 0)
								continue;

				Edge e = conn.getIthEdge(i);
				work.add(e.getTarget());
			}
		}
		// End collecting all vertices

		// Build the post/pred list for each vertex
		for (int i = 0; i < vertices.size(); i++) {
			MyVertex v2 = (MyVertex) vertices.get(i);
			Vertex v = (Vertex) new2old.get(v2);

			if (v instanceof Exit)
				if (v.getGraph().getIndex() == 0)
					continue;

			Connector conn = v.getConnector();
			int size = conn.size();
			if (conn instanceof ComplexConnector)
				size++;
			for (int j = 0; j < size; j++) {
				if (v instanceof CodeVertex)
					if (((CodeVertex) v).getConnInst() != null)
						if (((CodeVertex) v).getConnInst() instanceof GotoInstruction)
							if (j == 0)
								continue;

				Vertex t = conn.getIthEdge(j).getTarget();
				MyVertex t2 = (MyVertex) old2new.get(t);
				v2.getPost().add(t2);
				t2.getPred().add(v2);
			}
		}

		/*
		 * for (int i = 0; i < vertices.size(); i++) { MyVertex v2 = (MyVertex)
		 * vertices.get(i); if(v2.getPred().isEmpty()){
		 *  } }
		 */
	}

	/**
	 * Copy-constructor
	 * 
	 * @param cfg
	 */
	public MyCFG(MyCFG cfg) {
		classGen = cfg.classGen;
		methodGen = cfg.methodGen;
		vertices.clear();
		edges.clear();

		// Construct new MyVertex
		HashMap old2new = new HashMap();
		HashMap new2old = new HashMap();
		for (int i = 0; i < cfg.vertices.size(); i++) {
			MyVertex v = (MyVertex) cfg.vertices.get(i);
			MyVertex v2 = new MyVertex(v);
			old2new.put(v, v2);
			new2old.put(v2, v);
			vertices.add(v2);

			if ((v.getIndex() == 0) && (v.getGraphIndex() == 0))
				Entry = v2;
			if ((v.getIndex() == 1) && (v.getGraphIndex() == 0))
				Exit = v2;
		}

		for (int i = 0; i < vertices.size(); i++) {
			MyVertex v2 = (MyVertex) vertices.get(i);
			MyVertex v = (MyVertex) new2old.get(v2);

			// Copy the predecessor list
			Vector pred = v.getPred();
			Vector pred2 = v2.getPred();

			for (int j = 0; j < pred.size(); j++) {
				MyVertex s = (MyVertex) pred.get(j);
				MyVertex s2 = (MyVertex) old2new.get(s);
				pred2.add(s2);
			}

			// Copy the successor list
			Vector post = v.getPost();
			Vector post2 = v2.getPost();

			for (int j = 0; j < post.size(); j++) {
				MyVertex t = (MyVertex) post.get(j);
				MyVertex t2 = (MyVertex) old2new.get(t);
				post2.add(t2);
			}
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < vertices.size(); i++) {
			MyVertex v = (MyVertex) vertices.get(i);
			Vector post = v.getPost();
			for (int j = 0; j < post.size(); j++) {
				MyVertex t = (MyVertex) post.get(j);
				buf.append("\"");
				buf.append(v.toString(
						classGen.getConstantPool().getConstantPool())
						.replaceAll("\"", "'"));
				buf.append("\"");
				buf.append("->");
				buf.append("\"");
				buf.append(t.toString(
						classGen.getConstantPool().getConstantPool())
						.replaceAll("\"", "'"));
				buf.append("\"");
				buf.append("\n");
			}
		}
		return buf.toString();
	}

	public void toPS() {
		String prefix = classGen.getClassName() + "." + methodGen.getName();
		File dot = new File(prefix + ".dot");
		try {
			if (dot.exists())
				dot.delete();
			RandomAccessFile dotRAF = new RandomAccessFile(dot, "rw");
			dotRAF.writeBytes("digraph{\n");
			dotRAF.writeBytes("ratio=fill\n");
			dotRAF.writeBytes("size=\"8,9\"\n");
			dotRAF.writeBytes(this.toString());
			dotRAF.writeBytes("}");
			dotRAF.close();
			String cmd = "dot -Tps " + prefix + ".dot " + "-o " + prefix
					+ ".ps";
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			System.err.println(e);
		}

		try {
			String cmd = "rm " + prefix + ".dot";
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public void removeVertex(MyVertex v) {
		// TODO check argument's validity
		Vector pred = v.getPred();
		int size = pred.size();
		for (int i = 0; i < size; i++) {
			MyVertex s = (MyVertex) pred.get(0);
			s.getPost().removeElement(v);
			pred.removeElement(s);
		}

		Vector post = v.getPost();
		size = post.size();
		for (int i = 0; i < size; i++) {
			MyVertex t = (MyVertex) post.get(0);
			t.getPred().removeElement(v);
			post.removeElement(t);
		}

		vertices.removeElement(v);
	}

	public boolean isConnected() {
		boolean retval = true;

		// First round
		Entry.setColor(MyVertex.WHITE);
		Exit.setColor(MyVertex.WHITE);
		for (int i = 0; i < vertices.size(); i++)
			((MyVertex) vertices.get(i)).setColor(MyVertex.WHITE);

		DFS_visit(Entry);

		if (Exit.getColor() == MyVertex.WHITE)
			retval = false;

		return retval;
	}

	public void DFS_visit(MyVertex v) {
		// Below is the typical process of DFS copied from [Cormen]
		v.setColor(MyVertex.GRAY);
		Vector post = v.getPost();
		for (int i = 0; i < post.size(); i++) {
			MyVertex t = (MyVertex) post.get(i);
			switch (t.getColor()) {
			case MyVertex.WHITE:
				DFS_visit(t);
				break;
			case MyVertex.GRAY:
				// OK. Found a back edge. So the target must be an entrance
				// point to the loop. And in this case, we don't add v to the
				// predecessor list of t.
				//t.setIsLoopEntrance();
				//System.out.println(t + ": is a loop entrance");
				break;
			case MyVertex.BLACK:
				//t.getPred().add(v);
				break;
			}
			// if we found a loop starting at vertex v when exploring edge i,
			// then edge i is the edge that leads to loop
			//if (v.isLoopEntrance()) {
			//	conn.getIthEdge(i).setIsLoopEntrance();
			//	System.out.println(conn.getIthEdge(i) + ": is a loop edge");
			//	v.clearIsLoopEntrance();
			//}
		}
		v.setColor(MyVertex.BLACK);
	}

	public void DFS_visit_reverse(MyVertex v) {
		// Below is the typical process of DFS copied from [Cormen]
		v.setColor(MyVertex.GRAY);
		Vector pred = v.getPred();
		for (int i = 0; i < pred.size(); i++) {
			MyVertex t = (MyVertex) pred.get(i);
			switch (t.getColor()) {
			case MyVertex.WHITE:
				DFS_visit_reverse(t);
				break;
			case MyVertex.GRAY:
				// OK. Found a back edge. So the target must be an entrance
				// point to the loop. And in this case, we don't add v to the
				// predecessor list of t.
				//t.setIsLoopEntrance();
				//System.out.println(t + ": is a loop entrance");
				break;
			case MyVertex.BLACK:
				//t.getPred().add(v);
				break;
			}
			// if we found a loop starting at vertex v when exploring edge i,
			// then edge i is the edge that leads to loop
			//if (v.isLoopEntrance()) {
			//	conn.getIthEdge(i).setIsLoopEntrance();
			//	System.out.println(conn.getIthEdge(i) + ": is a loop edge");
			//	v.clearIsLoopEntrance();
			//}
		}
		v.setColor(MyVertex.BLACK);
	}

	public MyVertex getEntry() {
		return Entry;
	}

	public Vector getVertices() {
		return vertices;
	}

	public MyVertex getExit() {
		return Exit;
	}
	
	public ClassGen getClassGen(){
		return classGen;
	}
	
	public MethodGen getMethodGen(){
		return methodGen;
	}
}