/*
 * Created on Jul 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
//package database;
package edu.virginia.cs.terracotta.GraphicalComparer;
import java.sql.*;
import java.util.Properties;
import java.util.Vector;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Enumeration;
import java.lang.Math;

class Cnt {
	int cnt;
	Cnt( int cnt ) {
		this.cnt = cnt;
	}
}

/**
 * 
 */
public class Database {
	
	Connection con = null;
	int versions;
	Vector files = new Vector();
	Properties Patterns = null;
	
	String Patterns_TABLE = null;
	String Events_TABLE = "create table Events (" + 
			"Name varchar (128), Version INTEGER, Count INTEGER );";
	String REMOVE_Patterns_TABLE = "DROP table Patterns;";
	String REMOVE_Events_TABLE = "DROP table Events;";
	
	
	public Database( int versions ) {
		this.versions = versions;
		if( !this.open() ) {
			System.err.println( "can not connect to MySQL server" );
			return;
		}
	}
	
	public boolean open( ) {
		String dbURL;
		String driverPrefixURL = "jdbc:mysql:";
	    String dbuser = null;
	    String dbpass = null;
	    String dataSource = null;

	    loadPatterns();
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			System.err.println("Failed to load JDBC/mysql driver.");
			System.err.println("Exception: " + e.getMessage());
			return false;
	    }

		try {
			// Look for resource file 'mysql.datasource'
			InputStream is = ClassLoader.getSystemResourceAsStream ("mysql.datasource");
			Properties p = new Properties();
			p.load (is);
			dataSource = p.getProperty("datasource.name");
			if (dataSource == null)
			throw new Exception ();
			dbuser = p.getProperty("datasource.username", "");
			dbpass = p.getProperty("datasource.password", "");
		} catch (Exception e) {
			System.out.println("Unable to read resource file to get data source");
			return false;
		}
		
		try {
			// make connection
			dbURL = driverPrefixURL + dataSource;
			con = DriverManager.getConnection(dbURL, dbuser, dbpass);

			if (!con.isClosed())
				System.out.println("Successfully connected to MySQL server...");

		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean createTable( ) {
		String Versions_TABLE = "create table Versions (" +
			"Number INTEGER NOT NULL AUTO_INCREMENT, Name varchar(128), " +
			"PRIMARY KEY( Number ) ) ";
		String REMOVE_Versions_TABLE = "Drop table Versions";
		
		Patterns_TABLE = "create table Patterns (" + 
			"EventA varchar (128) NOT NULL, EventB varchar (128) NOT NULL";
		for ( int i = 1; i <= versions; i++ ) {
			Patterns_TABLE += ", Version" + i + " varchar (128)";
		}
		Patterns_TABLE += ", CatCnt INTEGER, Strictness FLOAT, Deviation FLOAT, Mode varchar (128), " + 
			"PRIMARY KEY( EventA, EventB )  )";
		
		Statement st = null;
		try {
			st = con.createStatement();
		} catch (SQLException e) {
			System.err.println("Exception: " + e.getMessage());
			return false;
		}
		
		try {
			st.executeUpdate( REMOVE_Versions_TABLE );
			st.executeUpdate( REMOVE_Events_TABLE );
			st.executeUpdate( REMOVE_Patterns_TABLE );
		} catch (SQLException e2) {
		}
		
		try {
			st.executeUpdate( Versions_TABLE );
			st.executeUpdate( Events_TABLE );
			st.executeUpdate( Patterns_TABLE );
			st.close();
		} catch (SQLException e1) {
			System.err.println("Exception: " + e1.getMessage());
			return false;
		}
		
		return true;
	}
	
	public boolean close( ) {
		try {
			Statement st = con.createStatement();
			st.executeUpdate( REMOVE_Events_TABLE );
			st.executeUpdate( REMOVE_Patterns_TABLE );
			st.close();
			if (con != null)
				con.close();
		} catch (SQLException e) {
			System.err.println("Exception: " + e.getMessage());
			return false;
		}
		return true;
	}


	/**
	 * @param version
	 * @param s
	 * @param c
	 */
	public boolean addEvent(int version, String s, int c) {
		PreparedStatement st = null;
		try {
			st = con.prepareStatement("INSERT INTO Events( Name, Version, Count ) VALUES" +
					"( ?, ? , ? );");
			st.setString( 1, s );
			st.setInt( 2, version );
			st.setInt( 3, c );
			st.executeUpdate( );
			st.close();
		} catch (SQLException e) {
			System.err.println("Exception in addEvent: " + e.getMessage());
			return false;
		}

		return true;
	}


	/**
	 * @param version
	 * @param eventStrings
	 * @param s
	 */
	public boolean addPattern(int version, Vector eventStrings, String s) {
		Statement st = null;
		//	sets the correct version column to s
		String newrow = "INSERT INTO Patterns " +
			" SET EventA = " + "\'" + (String)eventStrings.get(0) + "\'" +
			", EventB = " + "\'" + (String)eventStrings.get(1) + "\'" +
			", Version" + version + " = " + "\'" + s + "\'" + ";";
		//	 sets the correct version column to s
		String updaterow = "UPDATE Patterns " + 
			" SET Version" + version + " = " + "\'" + s + "\'" +
			" WHERE EventA = " + "\'" + (String)eventStrings.get(0) + "\'" +
			" && EventB = " + "\'" + (String)eventStrings.get(1) + "\'" + ";";
			
			
		
		try {
			st = con.createStatement();
			//System.out.println( updaterow );
			int updateCnt = st.executeUpdate( updaterow );
			if (updateCnt == 0 ) {
				st.executeUpdate( newrow );
			}
			st.close();
		} catch (SQLException e) {
			System.err.println("Exception addPattern: " + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public void SetPriority() {
		Statement st;
		Statement st2;
		try {
			st = con.createStatement();
			st2 = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM Patterns");
			while( rs.next() ) {
				String eventA = rs.getString(1);
				String eventB = rs.getString(2);
				float strictness = 0; // used for Strictness
				int cnt = 0; // used for CatCnt
				String mode = null; // used for Mode
				float variance = 0;
				double stdDev = 0;
				Hashtable catCnts = new Hashtable(versions); // used for CatCnt
				for( int v = 1 + 2; v <= versions + 2; v++ ) {
					String currentCat = rs.getString(v);
					if (currentCat == null) currentCat = "null";
					// used for CatCnt
					if( !catCnts.containsKey( currentCat ) )
						catCnts.put( currentCat, new Cnt(0) );
					else
						(( Cnt ) catCnts.get( currentCat )).cnt++;
					// used for mode
					int max = 0;
					String next = null;
					for( Enumeration enm = catCnts.keys();
							enm.hasMoreElements(); ) {
						next = (String)enm.nextElement();
						if( ((Cnt)catCnts.get(next)).cnt > max ) {
							mode = next;
							max = ((Cnt)catCnts.get(next)).cnt;
						}
					}
					// used for Strictness
					strictness += GetCatStrictness( currentCat );
				}

				strictness = (float)strictness/(float)versions;
				for( int v = 1 + 2; v <= versions + 2; v++ ) {
					String currentCat = rs.getString(v);
					int currStrict = GetCatStrictness( currentCat );
					variance += currStrict*currStrict - strictness*strictness;
				}
				stdDev = Math.sqrt(variance / (float) versions);
				st2.executeUpdate( "UPDATE Patterns" +
									" SET CatCnt = \'" + catCnts.size() + "\'" +
									" , Strictness = \'" + strictness + "\'" +
									" , Deviation = \'" + stdDev + "\'" +
									" , Mode = \'" + mode + "\'" +
									" WHERE EventA = \'" + eventA + "\'" +
									" AND EventB = \'" + eventB + "\'");
			}
			st.close();
		} catch (SQLException e) {
			System.err.println("Exception SetPriority: " + e.getMessage());
		}
	}

	private void loadPatterns() {
		InputStream is = 
			ClassLoader.getSystemResourceAsStream ("patterns.datasource");
		Patterns = new Properties();
		try {
			Patterns.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param string
	 * @return
	 */
	private int GetCatStrictness(String cat) {
		if( cat == null ) cat = "null";
		int strictness = 0;
		try {
			strictness = Integer.valueOf(Patterns.getProperty(cat))
							.intValue();
		} catch (java.lang.NumberFormatException e) {
			System.err.println("mising " + cat + " from patterns.datasource");
		}
		return strictness;
		
	}

	public String printPatterns( ) {
		SetPriority();
		String s = "";
		Statement st;
		try {
			st = con.createStatement();
			s += "EventA" + '\t' + "EventB" + '\t';
			ResultSet versionname = st.executeQuery("SELECT Name FROM Versions " +
					"ORDER BY Number");
			for ( int i = 1; i <= versions; i++ ) {
				if ( versionname.next() )
				s += versionname.getString(1) + '\t'; //"v" + i + '\t';
			}
			s += "CatCnt" + '\t'+ "Avg" + '\t'+ "StdDev" + '\t'+ "Mode" + '\t';
			s += '\n';
			
			/*
			 *
			 * the following orders the database:
			**/
			ResultSet rs = st.executeQuery("SELECT * FROM Patterns " +
					
					"ORDER BY Deviation DESC, Mode ASC" );
			while( rs.next() ) {
				for ( int i = 1; i <= versions + 6; i++ ) {
					s += rs.getString(i) + '\t';
				}
				s += '\n';
			}
			st.close();
		} catch (SQLException e) {
			System.err.println("Exception printPatterns: " + e.getMessage());
			return "";
		}
		
		return s;
	}

	public String printPatternsHTML( ) {
		SetPriority();
		String s = "";
		Statement st;
		try {
			st = con.createStatement();
			
			

			/*
			 * <table border="2" width="90%" bgcolor="#888888" cellpadding="2" cellspacing="1">
          <tr>
            <th>SAN ANTONIO SPURS</th>
            <th>Record</th>
          </tr>
          <tr>
            <td align="center" width="50%">1999<br>
              2003<br>
            </td>
            <td align="center">World Champions<br>
              World Champions<br>
            </td>
          </tr>
        </table>
			 */
			
			s += "<table border=\"2\" width=\"90%\" bgcolor=\"#888888\" cellpadding=\"2\" cellspacing=\"1\">";
			s += "<tr>";
			s += "<th>EventA</th>" +
				"<th>EventB</th>";
			ResultSet versionname = st.executeQuery("SELECT Name FROM Versions " +
			"ORDER BY Number");
			for ( int i = 1; i <= versions; i++ ) {
				if ( versionname.next() )
				s += "<th>" + versionname.getString(1) + "</th>";
			}
			s += "<th>CatCnt</th><th>Avg</th><th>StdDev</th><th>Mode</th>";
			s += "</tr>";
			// the following query sorts the data by the colomns made in SetPriority()
			ResultSet rs = st.executeQuery("SELECT * FROM Patterns " +
					
					"ORDER BY Deviation DESC, Mode ASC" );
			
			while( rs.next() ) {
				s += "<tr>";
				for ( int i = 1; i <= versions + 6; i++ ) {
					if( 2 < i && i <= versions + 2 ) {
						if( rs.getString(i) == null )
							s += "<th bgcolor=\"#CC0000\">";
						else
							s += "<th bgcolor=\"#BBBBBB\">";
					}
					else
						s += "<th bgcolor=\"#999999\">";
					s += rs.getString(i) + "</th>";
				}
				s += "</tr>";
			}
			s += "</table>";
			st.close();
		} catch (SQLException e) {
			System.err.println("Exception printPatterns: " + e.getMessage());
			return "";
		}
		
		return s;
	}

	/**
	 * @param path
	 */
	public void addfile(String path) {
		files.add( path );
		Statement st;
		try {
			st = con.createStatement();st.executeUpdate( "INSERT INTO Versions (Name)" +
					" VALUES (\'" + path + "\')" );
		} catch( Exception e ) {
			System.err.println( e );
		}
			
	}

}