/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.Chumper.ActivityPromotion;

import com.mysql.jdbc.Statement;
import java.sql.ResultSet;

/**
 *
 * @author nplaschk
 */
public class MySQLHandler implements CFileHandler{

    String adress;
    String port;
    String database;
    String user;
    String pass;
    
    MySQLHandler(String adress, String port, String database, String user, String pass) {
        
        //Überprüfen
        this.adress = adress;
        this.port = port;
        this.database = database;
        this.user = user;
        this.pass = pass;
        
        Statement stmt = null;
        ResultSet rs = null;
        try {
                    Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
                    e.printStackTrace();
        }
    }
    
}
