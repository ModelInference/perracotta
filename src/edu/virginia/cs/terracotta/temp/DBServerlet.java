package edu.virginia.cs.terracotta.temp;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;


public class DBServerlet extends HttpServlet {

	public void doGet( HttpServletRequest req, HttpServletResponse res )
			throws ServletException, IOException {

	res.setContentType("test/html");
	PrintWriter out = res.getWriter();

	out.println( "<HTML> hi asdfas </HTML>" );

	}
}
