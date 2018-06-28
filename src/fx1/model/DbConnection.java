package fx1.model;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbConnection {
/**********************************************/
/*      DICHIARAZIONE VARIABILI               */
/**********************************************/
    String DRIVER = "com.mysql.jdbc.Driver";
    String url_db = "jdbc:mysql://localhost:3306/tesina";
    Connection connessione = null;
    public static String query;
    public static Statement statement;
    public static PreparedStatement statementPr;
    public static ResultSet resultSet;
    
    public DbConnection () {
    }
    
    public void connetti () {
        /**********************************************/
    /*          CONNESSIONE DB                    */
    /**********************************************/
        try {
            Class.forName(DRIVER);
            System.out.println("Driver TROVATO");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver NON trovato");
            System.exit(1);
        }
        System.out.println("Connessione al db: " + url_db);

        try {
            connessione = DriverManager.getConnection(url_db, "root", "");
            System.out.println("Connessione avvenuta");
        } catch (Exception e) {
            System.out.println("Errore durante la connessione: " + e);
        }
    }
    
    public Connection getConnection() {
        return connessione;
    }
}
