/*
 * Created on 2004-8-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.cs.terracotta.test;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author jy6q
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class testTry {
	public boolean foo() {
		boolean ok = true;
		try {
			RandomAccessFile temp = new RandomAccessFile("abcd", "rw");
			temp.close();
		} catch (IOException e) {
			System.err.println(e);
			ok = false;
		}
		return (ok);
	}
}
