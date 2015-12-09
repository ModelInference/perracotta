/*
 * BuildAltChains.java
 * Created on Aug 8, 2006
 */
package edu.virginia.cs.terracotta;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import edu.virginia.cs.terracotta.event.Event;

public class BuildAltChains implements Processor {

	private Hashtable event2index = new Hashtable();

	private Hashtable index2event = new Hashtable();

	private String altFile = "E:\\Research\\jboss-result\\raw_traces_new\\freqlimit\\appro.simple";

	private String eventsFile = "E:\\Research\\jboss-result\\raw_traces_new\\freqlimit\\events.simple";

	// private String altFile = "al.txt";
	//
	// private String eventsFile = "events.txt";

	private byte state[][];

	private int total = 0;

	// private final static int UNKNOWN = -1;

	private final static int BLACK = 3;

	private final static int GRAY = 2;

	private final static int WHITE = 1;

	private int color[];

	// private int group[];
	//
	private int topo[];

	private int topcounter;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BuildAltChains bac = new BuildAltChains();
		bac.processOpts(args);
		bac.process();
	}

	/**
	 * @see edu.virginia.cs.terracotta.Processor#process()
	 */
	public void process() {
		parseEvents(eventsFile);
		state = new byte[total][total];
		for (int i = 0; i < state.length; i++) {
			Arrays.fill(state[i], (byte) 0);
		}
		parseAlt(altFile);
		// for (int i = 0; i < state.length; i++) {
		// for (int j = 0; j < state.length; j++) {
		// System.out.print(state[i][j] + " ");
		// }
		// System.out.println();
		// }
		color = new int[total];
		Arrays.fill(color, WHITE);
		topo = new int[total];
		topcounter = 0;
		for (int i = 0; i < total; i++) {
			if (color[i] == WHITE) {
				topsort(i);
			}
		}
		if (topcounter != total) {
			System.err
					.println("Fatal Error: the number of nodes top-sorted is not same as total");
			System.exit(1);
		}
//		for (int i = 0; i < total; i++) {
//			System.out.println(index2event.get(new Integer(topo[i])));
//		}
//		System.out.println();

		Arrays.fill(color, WHITE);
		Vector CCs = new Vector();
		for (int i = 0; i < total; i++) {
			if (color[i] == WHITE) {
				Vector visited = new Vector();
				DFS(i, visited);
				CCs.add(visited);
				System.out.println(visited);
			}
		}

		Iterator itr = CCs.iterator();
		while (itr.hasNext()) {
			Vector group = (Vector) itr.next();
			Collections.sort(group);
			chaining(group);
		}
	}

	void topsort(int i) {
		color[i] = GRAY;
		for (int j = 0; j < total; j++) {
			if (isConnectedDirected(i, j)) {
				if (color[j] == WHITE)
					topsort(j);
				else if (color[j] == GRAY) {
					System.err.println("Fatal Error: Encounter a gray node");
					System.exit(1);
				}
			}
		}
		topo[total - topcounter - 1] = i;
		topcounter++;
		color[i] = BLACK;
	}

	private void chaining(Vector group) {
		int nodes[] = new int[group.size()];
		for (int i = 0; i < nodes.length; i++)
			nodes[i] = ((Integer) group.get(i)).intValue();

		Vector worklist = new Vector();

		for (int i = 0; i < nodes.length; i++) {
			for (int j = i + 1; j < nodes.length; j++) {
				if (isConnectedUndirected(nodes[i], nodes[j]))
					worklist.add(new clique(nodes[i], nodes[j]));
			}
		}

		Vector newworklist = new Vector();
		while (!worklist.isEmpty() || !newworklist.isEmpty()) {
			if (worklist.isEmpty()) {
				Vector temp = worklist;
				worklist = newworklist;
				newworklist = temp;
			}
			clique c = (clique) worklist.remove(0);
//			System.out.println("Processing: " + c);
			boolean cannotExpand = true;
			for (int i = 0; i < nodes.length; i++) {
				if (c.hasNode(nodes[i]))
					continue;
				int j;
				for (j = 0; j < c.getSize(); j++) {
					if (!isConnectedUndirected(nodes[i], c.getNode(j)))
						break;
				}
				if (j == c.getSize()) {
					cannotExpand = false;
					c.insert(nodes[i]);
					newworklist.add(c);
//					System.out.println("Growing: " + c);
					for (int k = 0; k < worklist.size();) {
						clique ck = (clique) worklist.get(k);
						if (ck.isSubsetOf(c)) {
							clique temp = (clique) worklist.remove(k);
//							System.out.println("Removing: " + temp);
						} else
							k++;
					}
					break;
				}
			}
			if (cannotExpand) {
				int cSorted[] = new int[c.getSize()];
				int k = 0;
				System.out.println("+++ Alternating Chain with length "
						+ c.getSize());
				for (int i = 0; i < total; i++) {
					if (c.hasNode(topo[i])) {
						cSorted[k++] = topo[i];
						System.out.println(index2event
								.get(new Integer(topo[i]))
								+ " -> ");
					}
				}
				System.out.println();
				if (k != c.getSize()) {
					System.err.println("Fatal error: some nodes are not found");
					System.exit(1);
				}
				// Double check this is indeed an Alternating Chain
				for (int i = 0; i < cSorted.length; i++) {
					for (int j = i + 1; j < cSorted.length; j++) {
						if (!isConnectedDirected(cSorted[i], cSorted[j])) {
							System.err
									.println("Fatal error: this is not an Alternating Chain");
							System.exit(1);
						}
					}
				}
			}
		}

	}

	private boolean isConnectedUndirected(int i, int j) {
		return ((state[i][j] == 1) || (state[j][i] == 1));
	}

	private boolean isConnectedDirected(int i, int j) {
		return (state[i][j] == 1);
	}

	private void DFS(int i, Vector visited) {
		visited.add(new Integer(i));
		color[i] = GRAY;
		for (int j = 0; j < total; j++) {
			// treat the graph as undirected so that we can identify connected
			// components
			if (isConnectedUndirected(i, j)) {
				if (color[j] == WHITE)
					DFS(j, visited);
			}
		}
		color[i] = BLACK;
	}

	/**
	 * @see edu.virginia.cs.terracotta.Processor#processOpts(java.lang.String[])
	 */
	public void processOpts(String[] args) {

	}

	/**
	 * @see edu.virginia.cs.terracotta.Processor#usage()
	 */
	public void usage() {

	}

	private void parseAlt(String fname) {
		String line = null;
		String[] events = null;
		Event pEvent = null;
		Event sEvent = null;
		int p = 0;
		int s = 0;
		try {
			RandomAccessFile input = new RandomAccessFile(fname, "r");
			line = input.readLine();
			while (line != null) {
				if (line.matches("^//.*")) {
					line = input.readLine();
					continue;
				}

				// parse the event pair
				events = line.split("->");
				// Check that events.length == 2
				if (events.length != 2) {
					System.out.println("Illegal line:" + line);
					input.close();
					System.exit(1);
				}

				pEvent = new Event(events[0]);
				sEvent = new Event(events[1]);
				p = ((Integer) (event2index.get(pEvent))).intValue();
				s = ((Integer) (event2index.get(sEvent))).intValue();
				state[p][s] = 1;

				line = input.readLine();
			}

			// Always close file
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method parse the file that contains Events and return a Vector
	 * contains all the Events.
	 * 
	 * As a side-effect, it also construct the mapping between an Event to an
	 * Index number and store the relationships in two hashtables event2index
	 * and index2event.
	 * 
	 * @param fname
	 * @return
	 */
	private void parseEvents(String fname) {
		int i = 0;
		try {
			RandomAccessFile input = new RandomAccessFile(fname, "r");
			String line;
			while ((line = input.readLine()) != null) {
				// filter comments
				if (line.matches("^//.*"))
					continue;
				Event event = new Event(line);
				if (!event2index.containsKey(event)) {
					event2index.put(event, new Integer(i));
					index2event.put(new Integer(i), event);
					i++;
				}
			}
			// Always close file
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		total = i;
	}
}

class clique {
	private Vector elements;

	public clique(int node1, int node2) {
		if (elements == null)
			elements = new Vector();
		elements.add(new Integer(node1));
		elements.add(new Integer(node2));
	}

	public void insert(int node) {
		int i;
		for (i = 0; i < elements.size(); i++) {
			if (((Integer) elements.get(i)).intValue() > node)
				break;
		}
		elements.add(i, new Integer(node));
	}

	public Iterator getNodes() {
		return elements.iterator();
	}

	public int getSize() {
		return elements.size();
	}

	public int getNode(int index) {
		return ((Integer) elements.get(index)).intValue();
	}

	public boolean hasNode(int node) {
		return elements.contains(new Integer(node));
	}

	public boolean isSubsetOf(clique c) {
		boolean result = true;

		int i, j;
		for (i = 0, j = 0; i < getSize() && j < c.getSize();) {
			if (getNode(i) == c.getNode(j)) {
				i++;
				j++;
			} else if ((i < j) || (j < i)) {
				result = false;
				break;
			} else {
				if (getNode(i) < c.getNode(j))
					i++;
				else
					j++;
			}
		}
		return result;
	}

	public String toString() {
		return elements.toString();
	}
}
