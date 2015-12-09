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
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class testWhile {
	public void foo(int i) {
		testCFG t = new testCFG();
		t.bar1();
		if (i != 5) {
			while (i > 0) {
				t.bar1();
				int a = 0;
				if(i==6)
					i--;
				else
					i=i-2;
			}
			t.bar2();
		}
	}
}