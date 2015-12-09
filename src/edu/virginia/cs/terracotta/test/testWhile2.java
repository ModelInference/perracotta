/*
 * Created on 2004-8-14
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.virginia.cs.terracotta.test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author jy6q
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class testWhile2 {
	public void cfg2ps(String cfg, String prefix) {
		File dot = new File(prefix + ".dot");
		try {
			if (dot.exists())
				dot.delete();
			RandomAccessFile dotRAF = new RandomAccessFile(dot, "rw");
			dotRAF.writeBytes("digraph{\n");
			dotRAF.writeBytes("ratio=fill\n");
			dotRAF.writeBytes("size=\"8,9\"\n");
			dotRAF.writeBytes(cfg);
			dotRAF.writeBytes("}");
			dotRAF.close();
			String cmd = "dot -Tps " + prefix + ".dot " + "-o " + prefix
					+ ".ps";
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}