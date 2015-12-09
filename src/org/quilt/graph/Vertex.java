/* Vertex.java */

package org.quilt.graph;

import java.util.HashSet;

/**
 * A vertex in a directed graph.
 * 
 * @author < a href="jddixon@users.sourceforge.net">Jim Dixon </a>
 */
public class Vertex {
	public final static int WHITE = -1;

	public final static int GRAY = 0;

	public final static int BLACK = 1;

	/** Unique non-negative assigned to the Vertex; -1 means 'unassigned' */
	protected int index = -1;

	/** The graph this vertex belongs to. */
	protected Directed graph = null;

	/** Connects this vertex to one or more other vertices. */
	protected Connector connector = null;

	/** Optional label. */
	protected String label_ = null;

	protected boolean onMainPath = false;

	protected boolean isLoopEnter = false;
	
	protected int color = WHITE;
	
	protected HashSet pred = new HashSet();

	/** Creates a vertex without an index and belonging to no graph. */
	protected Vertex() {
	}

	/**
	 * Creates a vertex belonging to a graph, assigns an index unique within
	 * this graph.
	 * 
	 * @param g
	 *            The graph the vertex belongs to.
	 */
	public Vertex(Directed g) {
		checkForNull(g, "graph");
		graph = g;
		index = g.anotherVertex(this);
	}

	// ACCESSOR METHODS /////////////////////////////////////////////
	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector c) {
		checkForNull(c, "connector");
		connector = c;
	}

	public Edge getEdge() {
		if (connector == null) {
			return null;
		} else {
			return connector.getEdge();
		}
	}

	public Vertex getTarget() {
		if (connector == null) {
			return null;
		} else {
			return connector.getTarget();
		}
	}

	/** Get the graph this vertex is in. */
	public Directed getGraph() {
		return graph;
	}

	/** @return Vertex index, a non-negative integer. */
	public int getIndex() {
		return index;
	}

	/** @return String label or null */
	public String getLabel() {
		return label_;
	}

	/** Assign a label to the Vertex. */
	public void setLabel(String s) {
		label_ = s;
	}

	// CONNECTOR CONVERTERS //////////////////////////////////////////
	/**
	 * Convert the existing connector to a BinaryConnector.
	 * 
	 * @return The 'other' edge created.
	 */
	public Edge makeBinary() {
		Edge otherEdge = new Edge(this, graph.getExit());
		connector = new BinaryConnector(connector, otherEdge);
		return otherEdge;
	}

	/**
	 * Convert the exiting connector to a ComplexConnector, using the existing
	 * Edge as seed.
	 */
	public ComplexConnector makeComplex(int n) {
		// rely on range check in constructor;
		connector = new ComplexConnector(connector, n);
		return (ComplexConnector) connector;
	}

	/**
	 * Convert the exiting connector to a MultiConnector, using the existing
	 * Edge as seed.
	 */
	public MultiConnector makeMulti(int n) {
		// rely on range check in constructor;
		connector = new MultiConnector(connector, n);
		return (MultiConnector) connector;
	}

	// UTILITY FUNCTIONS ////////////////////////////////////////////

	/**
	 * Is the graph a parent, grandparent of this vertex?
	 * 
	 * @param g
	 *            Candidate progenitor.
	 * @return True if match is found.
	 */
	public boolean above(final Directed g) {
		// DEBUG
		System.out.println("above: checking whether graph " + g.getIndex()
				+ " is above vertex " + toString() + " whose parent is graph "
				+ getGraph().getParent().getIndex());
		// END
		if (g == null || g == graph) {
			return false;
		}

		// search upward through parent graphs
		for (Directed pop = graph.getParent(); pop != null; pop = pop
				.getParent()) {
			// DEBUG
			System.out.println("  checking whether graph " + g.getIndex()
					+ " is the same as graph " + pop.getIndex());
			// END
			if (pop == g) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Throw an exception if the argument is null.
	 * 
	 * @param o
	 *            Argument being checked
	 * @param what
	 *            What it is - for error message.
	 */
	public static void checkForNull(Object o, String what) {
		if (o == null) {
			throw new IllegalArgumentException("null " + what);
		}
	}

	/**
	 * @return A String in parent-index:my-index form.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer().append(graph.getIndex()).append(
				":").append(index);
		return sb.toString();
	}

	public void setIndex(int i) {
		index = i;
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
	
	public void setColor(int i){
		if((i<WHITE) || (i>BLACK))
			throw new IllegalArgumentException("setColor: "+i);
		color = i;
	}
	
	public int getColor(){
		return color;
	}
	
	public HashSet getPred(){
		return pred;
	}
}