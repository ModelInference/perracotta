/*
 * Created on 2004-8-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.cs.terracotta.CFG;


/**
 * @author jy6q
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MyMethodWithCount {
	private MyMethod method = null;

	private int predCount = 0;

	private int postCount = 0;

	/**
	 * @return Returns the method.
	 */
	public MyMethod getMethod() {
		return method;
	}

	/**
	 * @param method
	 *            The method to set.
	 */
	public void setMethod(MyMethod method) {
		this.method = method;
	}

	/**
	 * @return Returns the postCount.
	 */
	public int getPostCount() {
		return postCount;
	}

	/**
	 * @param postCount
	 *            The postCount to set.
	 */
	public void setPostCount(int postCount) {
		this.postCount = postCount;
	}

	/**
	 * @return Returns the predCount.
	 */
	public int getPredCount() {
		return predCount;
	}

	/**
	 * @param predCount
	 *            The predCount to set.
	 */
	public void setPredCount(int predCount) {
		this.predCount = predCount;
	}
}