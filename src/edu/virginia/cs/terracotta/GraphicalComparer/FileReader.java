/*
 * 
 */

package edu.virginia.cs.terracotta.GraphicalComparer;
import java.io.*;
import java.lang.String;
import java.util.Vector;


/**
 * 
 * 
 */
public class FileReader {

	// constants 
	// A catagory is marked CAT_MARKERcatagory:(int)
	static String CAT_MARKER = "**"; 	// ex: **Alternating
	static String CAT_END = ":";		// ex: **Alternating:
	// the patterns are marked eventLINKERevent
	// each on its own line proceded by tab
	static String LINKER = "->"; 		// ex: A->B where A and B are events.
	int HashSize;
	Database eventdb;
	


	public void FileToDatabase(File infile, int version ) {
		File inputFile = infile;
		
		eventdb.addfile( infile.getPath() );
		
		FileInputStream in = null;
		try {
			in = new FileInputStream(inputFile);
		} catch (FileNotFoundException e) {
			System.err.println( "bad file" + e );
		}
		
		Reader r = new BufferedReader(new InputStreamReader(in));
		StreamTokenizer inStream = new StreamTokenizer(r);
		
		inStream.eolIsSignificant(false);
		inStream.slashSlashComments(true);
		inStream.slashStarComments(true);
		inStream.wordChars(35,45);
		inStream.whitespaceChars(':',':');
		inStream.wordChars('_','_');
		inStream.wordChars('-','-');
		inStream.wordChars('>','>');
		inStream.quoteChar('\"');
		
		int c;
		String s;
		try {
			
			// get total number of event names
			while ( !( "events".equals(inStream.sval) ) ) {
				inStream.nextToken();
			}

			if ( inStream.nextToken() != StreamTokenizer.TT_NUMBER ) {
				System.err.println( "bad file format: " + infile.getName() );
			}
			
			// initiallize eventdb
			HashSize = (int) inStream.nval; // total number of event names
			System.out.println( "HashSize " + inStream.nval);
			// add events
			inStream.ordinaryChars(':',':');
			inStream.nextToken();
			for( int i = 0; i < HashSize; i++ ) {
				
				s = inStream.sval;
				inStream.nextToken();
				c = (int) inStream.nval;
				System.out.println( "i: " + i + ": " + s + ": " + c );
				if( s == null ) {
					System.err.println( "error on line" + inStream.lineno());
				}
				eventdb.addEvent( version, s, c );
				
			}
			// add patterns

			inStream.whitespaceChars(':',':');
			inStream.nextToken();
			//System.out.println(inStream.sval);
			while( "*".equals(inStream.sval) ) {
				inStream.nextToken();
				s = inStream.sval;
				inStream.nextToken();
				c = (int)inStream.nval;
				Vector eventStrings = new Vector();
				for( int i = 0; i < c; i++ ) {
					eventStrings = new Vector();
					inStream.nextToken();
					eventStrings.add( inStream.sval );
					inStream.nextToken();
					inStream.nextToken();
					inStream.nextToken();
					eventStrings.add( inStream.sval );
					eventdb.addPattern( version, eventStrings, s );
				}
				while( inStream.ttype != StreamTokenizer.TT_EOF &&
						!"*".equals(inStream.sval) ) {
					inStream.nextToken();
				}
			}
			
			// done makeing hashtable for this version
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public FileReader( Database db) {
		this.eventdb = db;
	}
	
}


