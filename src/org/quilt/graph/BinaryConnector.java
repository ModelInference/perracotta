/* BinaryConnector.java */

package org.quilt.graph;

/**
 * A Connector holding two edges. Implementation detail: we assume that in
 * laying out bytecode the 'else' code appears immediately after the if but the
 * 'then' code is accessed using a goto.
 * 
 * @author <a href="mailto:jddixon@users.sourceforge.net">Jim Dixon </a>
 */
public class BinaryConnector extends Connector {
	/** The preferred edge, the 'else' edge. */
	private Edge edge;

	/** The other edge, the 'then' edge. */
	private Edge otherEdge;

	/** Source of both edges. */
	private Vertex source = null;

	private void doSetUp(Edge e, Edge otherE) {
		if (e == null || otherE == null) {
			throw new IllegalArgumentException("arguments cannot be null");
		}
		edge = e;
		source = edge.getSource();
		otherEdge = otherE;
		if (source != otherEdge.getSource()) {
			throw new IllegalArgumentException(
					"edges in a BinaryConnector must have the same source");
		}
	}

	public BinaryConnector(Edge e, Edge otherE) {
		doSetUp(e, otherE);
	}

	public BinaryConnector(Connector conn, Edge otherE) {
		if (conn == null) {
			throw new IllegalArgumentException("connector cannot be null");
		}
		doSetUp(conn.getEdge(), otherE);
	}

	// INTERFACE CONNECTOR //////////////////////////////////////////
	/** Get the preferred edge. */
	public Edge getEdge() {
		return edge;
	}

	/** Get the target of the preferred edge. */
	public Vertex getTarget() {
		return edge.getTarget();
	}

	/** Change the target of the preferred edge. */
	public void setTarget(Vertex v) {
		checkTarget(v);
		edge.setTarget(v);
	}

	public int size() {
		return 2;
	}

	// OTHER METHODS ////////////////////////////////////////////////
	private void checkTarget(final Vertex target) {
		if (target == null) {
			throw new IllegalArgumentException("target may not be null");
		}
		// DEBUG
		if (source == null)
			System.out.println("BinaryConnector.checkTarget INTERNAL ERROR: "
					+ " source is null");
		else if (source.getGraph() == null)
			System.out
					.println("BinaryConnector.checkTarget: source has no graph!");
		if (target.getGraph() == null)
			System.out
					.println("BinaryConnector.checkTarget: target has no graph!");
		// END
		if (target.getGraph() != source.getGraph()) {
			throw new IllegalArgumentException(
					"new target must be in same graph");
		}
	}

	/** Get the other edge. */
	public Edge getOtherEdge() {
		return otherEdge;
	}

	/** Get the target of the other edge */
	public Vertex getOtherTarget() {
		return otherEdge.getTarget();
	}

	/** Set the target of the other edge. */
	public void setOtherTarget(Vertex v) {
		checkTarget(v);
		otherEdge.setTarget(v);
	}

	/**
	 * 
	 * @author Jinlin Yang
	 * 
	 * TODO To change the template for this generated type comment go to Window -
	 * Preferences - Java - Code Style - Code Templates
	 */
	public Edge getIthEdge(int i) {
		if ((i > this.size()) || (i < 0)) {
			throw new IllegalArgumentException(
					"i must be greater than 0 and less than " + this.size());
		}
		Edge retval = null;
		if (i == 0)
			retval = edge;
		else if (i == 1)
			retval = otherEdge;
		return retval;
	}
}