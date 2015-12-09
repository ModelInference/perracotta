package edu.virginia.cs.terracotta.GraphicalComparer;
import java.io.File;
import java.util.Vector;

/*
 * Created on Aug 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author ejm5p
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class FileOpener {
	public static void main( String args[] ) {
		Database db;
		Vector files = new Vector();
		
		File fileArgs[] = new File[args.length];
		for( int i = 0; i < args.length; i++ )
			fileArgs[i] = new File(args[i]);
		try {
			files = open( fileArgs );
		} catch( Exception e ) {
			System.err.println( e );
			return;
		}
		
		// create database
		db = new Database( files.size() );
		db.createTable( );
		FileReader fr = new FileReader( db );
		
		// fill database
		System.out.println( "reading files: " );
		for( int i = 0; i < files.size(); i++ ) {
			File f = (File) ( files.get(i) );
			System.out.println( f.getPath() );
			fr.FileToDatabase( (File) files.get(i), i+1 );
		}
		
		System.out.println ( db.printPatternsHTML() );
	}

	public static Vector open( File args[] ) throws Exception {
		File file;
		Vector files = new Vector();
		for( int i = 0; i < args.length; i++ ) {
			try {
				file = args[i];
			} catch( Exception e ) {
				throw( new Exception( "bad file name: " + args[i] ) );
			}
			
			if( file.isDirectory() ) {
				files.addAll( open( file.listFiles() ) );
			} else {
				files.add( file );
			}
		}
		return files;
	}
	
}
