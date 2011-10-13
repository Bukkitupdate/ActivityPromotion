/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.Chumper.ActivityPromotion;

import de.Chumper.ActivityPromotion.APPlayer;
import de.Chumper.ActivityPromotion.ActivityPromotion;
import de.Chumper.ActivityPromotion.CFileHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author nplaschk
 */
public class FlatFileHandler implements CFileHandler{
    private final String file;
    private final ActivityPromotion plugin;
    private final Configuration LIST;
    

    FlatFileHandler(ActivityPromotion plugin, String file) {
        this.file = file;
        this.plugin = plugin;
        
        //Initialize the flatfile
        LIST = new Configuration(new File(plugin.getDataFolder(),this.file));
    }

    @Override
    public Map<String, APPlayer> load() {
        
        Map<String, APPlayer> temp = new HashMap<String, APPlayer>();
        
        //load the LIST
        LIST.load();
        
        List<String> test = LIST.getKeys("players");
        
        if(test.isEmpty())
            return temp;
        
        for(String name : test)
        {
            APPlayer tmpPlayer = new APPlayer();
            
            //timePlayed
            String tmpS = LIST.getString("players."+name+".timePlayed");
            if(tmpS == null || tmpS.isEmpty())
                tmpS = "0";
            
            tmpPlayer.setTimePlayed(Long.valueOf(tmpS));
            //----
            
            
            //lastLogout
            tmpS = LIST.getString("players."+name+".lastLogout");
            if(tmpS == null || tmpS.isEmpty())
                tmpS = "0";
            
            tmpPlayer.setLastLogout(Long.valueOf(tmpS));
            //---
            
            
            //lastLogout
            tmpS = LIST.getString("players."+name+".passivePeriod");
            if(tmpS == null || tmpS.isEmpty())
                tmpS = "0";
            
            tmpPlayer.setPassivePeriod(Long.valueOf(tmpS));
            //---
            
            //totalTime
            tmpS = LIST.getString("players."+name+".totalTime");
            if(tmpS == null || tmpS.isEmpty())
                tmpS = "0";

            tmpPlayer.setTotalTime(Long.valueOf(tmpS));
            //---
            
            tmpPlayer.setTimeLastAction(Long.valueOf("0"));
            
            //add to HashMap
            temp.put(name, tmpPlayer);
        }
        
        return temp;
        
    }

    @Override
    public void save(Map<String, APPlayer> PLAYER) {
        
        //save PLAYER
        for( Map.Entry<String, APPlayer> entry : PLAYER.entrySet() )
        {
          String name = entry.getKey();
          APPlayer pl = entry.getValue();
          
          LIST.setProperty("players."+name+".timePlayed", pl.getTimePlayed());
          LIST.setProperty("players."+name+".passivePeriod", pl.getPassivePeriod());
          LIST.setProperty("players."+name+".lastLogout", pl.getLastLogout());
          LIST.setProperty("players."+name+".totalTime", pl.getTotalTime());
          
        }
        
        LIST.save();
    }

    @Override
    public APPlayer loadPlayer(String name) {
        
        //load a Single Player and update it
        
        APPlayer tmpPlayer = new APPlayer();
            
        //timePlayed
        String tmpS = LIST.getString("players."+name+".timePlayed");
        if(tmpS == null || tmpS.isEmpty())
            tmpS = "0";

        tmpPlayer.setTimePlayed(Long.valueOf(tmpS));
        //----

        //lastLogout
        tmpS = LIST.getString("players."+name+".lastLogout");
        if(tmpS == null || tmpS.isEmpty())
            tmpS = "0";

        tmpPlayer.setLastLogout(Long.valueOf(tmpS));
        //---

        //passivePeriod
        tmpS = LIST.getString("players."+name+".passivePeriod");
        if(tmpS == null || tmpS.isEmpty())
            tmpS = "0";

        tmpPlayer.setPassivePeriod(Long.valueOf(tmpS));
        //---
        
        //totalTime
        tmpS = LIST.getString("players."+name+".totalTime");
        if(tmpS == null || tmpS.isEmpty())
            tmpS = "0";

        tmpPlayer.setTotalTime(Long.valueOf(tmpS));
        //---

        tmpPlayer.setTimeLastAction(Long.valueOf("0"));
        
        return tmpPlayer;
    }

    @Override
    public void savePlayer(String name, APPlayer player) {
        
        //save to Disk
        LIST.setProperty("players."+name+".timePlayed", player.getTimePlayed());
        LIST.setProperty("players."+name+".passivePeriod", player.getPassivePeriod());
        LIST.setProperty("players."+name+".lastLogout", player.getLastLogout());
        LIST.setProperty("players."+name+".totalTime", player.getTotalTime());
        
        LIST.save();
    }
    
    @Override
    public void close()
    {
        
    }
    
}
