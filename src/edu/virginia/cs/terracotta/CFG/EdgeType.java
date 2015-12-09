/*
 * Created on 2004-8-21
 *
 * EdgeType.java
 * ${project_name}
 */
package edu.virginia.cs.terracotta.CFG;

import java.util.Arrays;

/**
 * @author jy6q
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class EdgeType {
	private int[] types = null;

	private boolean init = false;

	public EdgeType() {
		init = false;
	}

	public void initialize(int n) {
		if (n <= 0) {
			System.err.println("Invalid argument");
			new Exception().printStackTrace();
			System.exit(1);
		}
		init = true;
		types = new int[n + 1];
		Arrays.fill(types, 0);
	}

	public int getTypeCount(int i) {
		if (!init) {
			System.err.println("Have not initialized");
			new Exception().printStackTrace();
			System.exit(1);
		}
		if (i < 1 || i >= types.length) {
			System.err.println("Invalid argument");
			new Exception().printStackTrace();
			System.exit(1);
		}
		return types[i];
	}

	public void setTypeCount(int i, int n) {
		if (!init) {
			System.err.println("Have not initialized");
			new Exception().printStackTrace();
			System.exit(1);
		}
		if (i < 1 || i >= types.length) {
			System.err.println("Invalid argument");
			new Exception().printStackTrace();
			System.exit(1);
		}
		types[i] = n;
	}
	
	public void increaseTypeCount(int i){
		if (!init) {
			System.err.println("Have not initialized");
			new Exception().printStackTrace();
			System.exit(1);
		}
		if (i < 1 || i >= types.length) {
			System.err.println("Invalid argument");
			new Exception().printStackTrace();
			System.exit(1);
		}
		types[i]++;
	}
	
	public String toString(){
		StringBuffer buf = new StringBuffer();
		for(int i=1; i< types.length; i++)
			buf.append(types[i] + "\t");
		return buf.toString();
	}
}