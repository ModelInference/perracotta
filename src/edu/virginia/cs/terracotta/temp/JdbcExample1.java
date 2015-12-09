
package edu.virginia.cs.terracotta.temp;
import java.sql.*;

public class JdbcExample1 {

  public static void main(String args[]) {
    Connection con = null;
    String dbURL = "jdbc:mysql://mysql.cs.virginia.edu/ipa";
    String dbuser = "ipauser";
    String dbpass = "dave";

    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection( dbURL , dbuser, dbpass );

      if(!con.isClosed())
        System.out.println("Successfully connected to MySQL server...");

    } catch(Exception e) {
      System.err.println("Exception: " + e.getMessage());
    } finally {
      try {
        if(con != null)
          con.close();
      } catch(SQLException e) {}
    }
  }
}

