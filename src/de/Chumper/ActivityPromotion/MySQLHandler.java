/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.Chumper.ActivityPromotion;


import java.util.HashMap;
import java.util.Map;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final ActivityPromotion plugin;
    private Connection con = null;
    
    MySQLHandler(ActivityPromotion plugin, String adress, String port, String database, String user, String pass) {
        
        //Muss noch überprüft werden
        
        this.plugin = plugin;
        
        this.adress = adress;
        this.port = port;
        this.database = database;
        this.user = user;
        this.pass = pass;
        
//        Statement stmt = null;
//        ResultSet rs = null;
        
        //load the classes
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            plugin.log.warning(plugin.AP+e.getMessage());
        }
        
        String url = "jdbc:mysql://"+this.adress+":"+this.port+"/"+this.database;
        //plugin.log.info(plugin.AP+url);
        
        //try to connect
        try 
        {
            
          this.con = DriverManager.getConnection(url,this.user,this.pass);
            
        } catch (SQLException e)
        {
            plugin.log.warning(plugin.AP+"Can't connect to the database, are the infos right?");
            plugin.log.warning(plugin.AP+e.getMessage());
        }
        
    }

    @Override
    public Map<String, APPlayer> load() {
        
        
        ResultSet rs = null;
        Map<String, APPlayer> total = new HashMap<String, APPlayer>();
        
        
        try
        {
            Statement stmt = this.con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM ap_player");
        
        } catch(SQLException e)
        {
            plugin.log.warning(plugin.AP+e.getMessage());
        }
        
        //we want to have only one result, so set the pointer
        try 
        {
            while(rs.next())
            {
                 APPlayer tmp = new APPlayer();
                 tmp.setLastLogout(rs.getLong("lastLogout"));
                 tmp.setPassivePeriod(rs.getLong("passivePeriod"));
                 tmp.setTimeLastAction(Long.valueOf("0"));
                 tmp.setTimePlayed(rs.getLong("timePlayed"));
                 tmp.setTotalTime(rs.getLong("totalTime"));
                 
                 total.put(rs.getString("name"), tmp);
            }
        } catch(SQLException e)
        {
            plugin.log.warning(plugin.AP+e.getMessage());
        }
        //pointer set, now get the data
        
        return total;
    }

    @Override
    public void save(Map<String, APPlayer> PLAYER) {
        
        ResultSet rs = null;
        
        for( Map.Entry<String, APPlayer> entry : PLAYER.entrySet() )
        {
          String name = entry.getKey();
          APPlayer pl = entry.getValue();
          
          //prepare the statement
          String sql = "INSERT INTO "
                        + "ap_player "
                        + "(name,lastLogout,passivePeriod,timePlayed,totalTime) "
                        + "VALUES "
                        + "('"+name+"',"+pl.getLastLogout()+","+pl.getPassivePeriod()+","+pl.getTimePlayed()+","+pl.getTotalTime()+")"
                        + "ON DUPLICATE KEY UPDATE "
                        + "lastLogout = '" + pl.getLastLogout() + "', "
                        + "passivePeriod = '" + pl.getPassivePeriod() + "', "
                        + "timePlayed = '" + pl.getTimePlayed() + "', "
                        + "totalTime = '" + pl.getTotalTime() + "'";
            try 
            {
                //sql ready
                Statement stmt = this.con.createStatement();
                stmt.execute(sql);
            } catch (SQLException ex) {
                plugin.log.warning(plugin.AP+ex.getMessage());
            }
            
            //save was success
        }
        
    }

    @Override
    public APPlayer loadPlayer(String name) {
        
        ResultSet rs = null;
        APPlayer tmp = new APPlayer();
        
        
        try
        {
            Statement stmt = this.con.createStatement();
            rs = stmt.executeQuery("SELECT name,timePlayed,lastLogout,passivePeriod,totalTime FROM ap_player WHERE name = '"+name+"'");
        } catch(SQLException e)
        {
            plugin.log.warning(plugin.AP+e.getMessage());
        }
        
        //we want to have only one result, so set the pointer
        try 
        {
            while(rs.next())
            {
                 tmp.setLastLogout(rs.getLong("lastLogout"));
                 tmp.setPassivePeriod(rs.getLong("passivePeriod"));
                 tmp.setTimeLastAction(Long.valueOf("0"));
                 tmp.setTimePlayed(rs.getLong("timePlayed"));
                 tmp.setTotalTime(rs.getLong("totalTime"));
            }
        } catch(SQLException e)
        {
            plugin.log.warning(plugin.AP+e.getMessage());
        }
        //pointer set, now get the data
        
        //Hier sitzt der Fehler... die Daten kommen nicht rein
        
        return tmp;
    }

    @Override
    public void savePlayer(String name, APPlayer pl) {
        
        
        ResultSet rs = null;
        
        //prepare the statement
        String sql = "INSERT INTO "
                        + "ap_player "
                        + "(name,lastLogout,passivePeriod,timePlayed,totalTime) "
                        + "VALUES "
                        + "('"+name+"',"+pl.getLastLogout()+","+pl.getPassivePeriod()+","+pl.getTimePlayed()+","+pl.getTotalTime()+")"
                        + "ON DUPLICATE KEY UPDATE "
                        + "lastLogout = '" + pl.getLastLogout() + "', "
                        + "passivePeriod = '" + pl.getPassivePeriod() + "', "
                        + "timePlayed = '" + pl.getTimePlayed() + "', "
                        + "totalTime = '" + pl.getTotalTime() + "'";
          try 
          {
              //sql ready
              Statement stmt = this.con.createStatement();
              stmt.execute(sql);
          } catch (SQLException ex) {
              plugin.log.warning(plugin.AP+ex.getMessage());
          }
        
        //save succeed
    }
    
    @Override
    public void close()
    {
        try {
            this.con.close();
        } catch (SQLException ex) {
            plugin.log.warning(plugin.AP+ex.getMessage());
        }
    }
    
}
