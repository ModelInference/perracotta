/*
 * Created on Jul 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
//package database;

package edu.virginia.cs.terracotta.GraphicalComparer;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ejm5p
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Test extends HttpServlet {

	public void doGet( HttpServletRequest req, HttpServletResponse res )
	throws ServletException, IOException {
		
		res.setContentType("test/html");
		PrintWriter out = res.getWriter();

		out.println( "hi" );
		
		Database db;
		db = new Database(6);
		db.createTable();
		FileReader fr = new FileReader( db );
		File f = null;
		int v;
		f = new File( "infer.all.0.9.6" );
		v = 1;
		fr.FileToDatabase( f, v );
		f = new File( "infer.all.0.9.7" );
		v = 2;
		fr.FileToDatabase( f, v );
		f = new File( "infer.all.0.9.7a" );
		v = 3;
		fr.FileToDatabase( f, v );
		f = new File( "infer.all.0.9.7b" );
		v = 4;
		fr.FileToDatabase( f, v );
		f = new File( "infer.all.0.9.7c" );
		v = 5;
		fr.FileToDatabase( f, v );
		f = new File( "infer.all.0.9.7d" );
		v = 6;
		fr.FileToDatabase( f, v );
		out.println( db.printPatternsHTML() );
		db.close();
	}
	
}
