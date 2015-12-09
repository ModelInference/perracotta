/*
 * Created on 2004-8-12
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.cs.terracotta.test;

import java.io.IOException;

/**
 * @author jy6q
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class testIF {
	public void foo(int i) throws IOException{
		testCFG t = new testCFG();
		t.bar1();
		if(i != 0){
			//if(i==1)
				t.bar1();
			//else
			//	t.bar2();
		}/*else{
			t.bar2();
		}
		t.bar2();
		if(i == 0){
			t.bar1();
		}else{
			t.bar2();
		}*/
		bar();
	}
	
	void bar() throws IOException{
		foo(0);
		bar();
	}
}
