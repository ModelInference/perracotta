/*
 * Created on 2004-8-12
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.cs.terracotta.test;

/**
 * @author jy6q
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class testRecursive {

	public int fac(int i){
		int retval=1;
		if(i!=0)
			retval = i*fac(i-1);
		return retval;
	}
}
