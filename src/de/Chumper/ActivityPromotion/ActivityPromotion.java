package de.Chumper.ActivityPromotion;

import de.bananaco.permissions.Permissions;
import de.bananaco.permissions.worlds.WorldPermissionsManager;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
/**
 *
 * @author Nils Plaschke
 */
public class ActivityPromotion extends JavaPlugin{
    
    ActivityPromotionPlayerListener playerListener = new ActivityPromotionPlayerListener(this);
    private Map<String, Long> TimePlayed;
    private Map<String, Long> TimeLastAction;
    private Map<Long, String> PromotionGroups;
    private Map<String, Boolean> passivePeriod;
    private Map<String, Long> lastLogout;
    
    private Logger log; 
    private PluginManager pm;
    
    protected static Configuration CONFIG;
    protected static Configuration LIST;
    
    private Long idleTime;
    
    @Override
    public void onEnable(){
 
        this.log = this.getServer().getLogger();
        this.pm = this.getServer().getPluginManager();
        this.TimePlayed = new HashMap<String, Long>();
        this.TimeLastAction = new HashMap<String, Long>();
        this.PromotionGroups = new HashMap<Long, String>();
        this.passivePeriod = new HashMap<String, Boolean>();
        this.lastLogout = new HashMap<String, Long>();
        //register Events
        pm.registerEvent(Event.Type.PLAYER_ANIMATION, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
        
        //config
        this.checkConfig();
        
        this.idleTime = Long.parseLong(CONFIG.getString("idleTime"));
        
        for(Player player: getServer().getOnlinePlayers()) {
            initiatePlayer(player);
        }
        
        
        
        log.log(Level.INFO, "[ActivityPromotion "+this.getDescription().getVersion()+"] enabled");
    }
    
    @Override
    public void onDisable(){
        
        //CONFIG.save();
        saveList();
        log.log(Level.INFO, "[ActivityPromotion "+this.getDescription().getVersion()+"] disabled");
        
    }
    private String formatSek(String time)
    {
        Long ts = Long.parseLong(time);
        Long days = Long.parseLong("0");
        Long min = Long.parseLong("0");
        Long hours = Long.parseLong("0");
        
        
        if (ts > 60*60*24)
        {
            days = ts / (60*60*24);
            ts = ts - (60*60*24*days);
        }
        if (ts > 60*60)
        {
            hours = ts / (60*60);
            ts = ts - (60*60*hours);
        }
        if (ts > 60)
        {
            min = ts / 60;
            ts = ts - 60*min;
        }
        
        
        String result = "";
        
        if (days > 0)
            result += days+ " days ";
        if (hours > 0)
            result += hours+ " hours ";
        if (min > 0)
            result += min+ " min ";
        if (ts > 0)
            result += ts+ " sec ";
        
        return result;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	Player player = null;
	if (sender instanceof Player) {
		player = (Player) sender;
	}
 
	if (command.getName().equalsIgnoreCase("ap")) {
            if (player == null) {
                    sender.sendMessage("this command can only be run by a player");
            } else {
                if (args.length > 0)
                {    
                    if(args[0].equals("reload"))
                    {
                        if (!player.hasPermission("activitypromotion.reload"))
                        {
                            sender.sendMessage(ChatColor.DARK_GREEN+"You are not allowed to do that");
                            return true;
                        }
                        
                        saveList();
                        log.info("[ActivityPromotion "+this.getDescription().getVersion()+"] reload");
                        //config
                        this.checkConfig();

                        this.idleTime = Long.parseLong(CONFIG.getString("idleTime"));

                        for(Player pplayer: getServer().getOnlinePlayers()) {
                            initiatePlayer(pplayer);
                        }
                        sender.sendMessage(ChatColor.DARK_GREEN+"Activity Promotion reloaded");
                    }
                    else if(args[0].equals("info"))
                    {
                        if (!player.hasPermission("activitypromotion.info"))
                        {
                            sender.sendMessage(ChatColor.DARK_GREEN+"You are not allowed to do that");
                            return true;
                        }
                        saveList();
                        
                        String actime;
                        
                        try
                        {
                            actime = LIST.getString("players."+args[1]+".activityTime");
                        } catch (Exception e)
                        {
                            actime = null;
                        }
                        
                        if(actime == null)
                        {
                            sender.sendMessage(ChatColor.DARK_GREEN+"Player not found, try again");
                            return true;
                        }
                        //check if Player exists
                        if (actime != null)
                        {
                            for (int i = 0; i < 14; i++)
                            {
                                sender.sendMessage("");
                            }
                            sender.sendMessage(ChatColor.DARK_GREEN+"Informations: "+ChatColor.DARK_RED+args[1]);
                            sender.sendMessage(ChatColor.DARK_GREEN+"----------------------------------------");
                            sender.sendMessage(ChatColor.DARK_GREEN+"ActivityTime: "+ChatColor.DARK_RED+formatSek(actime) +""+ ChatColor.DARK_GREEN+ "in " + ChatColor.DARK_RED + formatSek(Long.toString(Long.parseLong(CONFIG.getString("timePeriod"))*60))+" ("+Double.toString((Double)(Double.valueOf(actime)/60) / Double.valueOf(CONFIG.getString("timePeriod")) * 100 )+"%)");
                            
                            if (CONFIG.getBoolean("saveTotalTime", false))
                            {
                                String tT = null;
                                try
                                {
                                    tT = Long.toString(Long.valueOf(LIST.getString("players."+args[1]+".totalTime")) + TimePlayed.get(args[1]));
                                }catch(Exception e)
                                {}
                                
                                if ( tT != null && !tT.isEmpty())
                                {
                                    sender.sendMessage(ChatColor.DARK_GREEN + "TotalTime: "+ChatColor.DARK_RED+formatSek(tT));
                                }
                                else
                                    sender.sendMessage(ChatColor.DARK_GREEN + "TotalTime: "+ChatColor.DARK_RED+"Can't read totalTime");
                            }
                            if (CONFIG.getBoolean("saveLastLogout", false))
                            {
                                String lastlogout = LIST.getString("players."+args[1]+".lastLogout");

                                if ( lastlogout != null && !lastlogout.isEmpty())
                                {

                                    Calendar calendar = new GregorianCalendar();

                                    calendar.setTimeInMillis(Long.valueOf(lastlogout)*1000);

                                    Date time = calendar.getTime();

                                    SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

                                    sender.sendMessage(ChatColor.DARK_GREEN + "LastLogout: "+ChatColor.DARK_RED+df.format(time));
                                    

                                }
                                else
                                {
                                    sender.sendMessage(ChatColor.DARK_GREEN + "LastLogout: "+ChatColor.DARK_RED+"disabled");
                                }


                            }
                            else
                                sender.sendMessage(ChatColor.DARK_GREEN + "LastLogout: "+ChatColor.DARK_RED+"disabled");
                           
                            //promoted Groups
                            List<String> groups = new ArrayList<String>();
                            
                            for( Map.Entry<Long, String> entry : PromotionGroups.entrySet() )
                            {
                                Long time = entry.getKey();
                                String group = entry.getValue(); 
                                String worlds = CONFIG.getString("groups." + Long.toString(time) + ".world");
                                
                                if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx"))
                                {
                                    PermissionManager permissions = PermissionsEx.getPermissionManager();
                                    
                                    
                                    
                                    for(String world : worlds.split(","))
                                    {
                                        if (permissions.getUser(args[1]).inGroup(group,world))
                                            if(!groups.contains(group))
                                                groups.add(group);
                                        
                                    }
                                }
                                else if(Bukkit.getServer().getPluginManager().isPluginEnabled("bPermissions"))
                                {

                                    WorldPermissionsManager wpm = null;
                                    try {
                                        wpm = Permissions.getWorldPermissionsManager();
                                    } catch (Exception e) {
                                        log.warning("bPermissions not detected!");
                                        this.getPluginLoader().disablePlugin(this);
                                    }
                                    
                                    
                                    
                                    for (World world : getServer().getWorlds())
                                    {
                                       for(String cworld : worlds.split(","))
                                       {
                                           if(world.getName().equals(cworld.trim()))
                                           {
                                               for(String gname : wpm.getPermissionSet(world).getGroups(args[1]))
                                               {

                                                   if (gname.equals(group))
                                                       if(!groups.contains(group))
                                                           groups.add(gname);
                                               }
                                           }
                                       }
                                       
                                    }
                                    
                                }
                                
                            }
                            if(groups.size() > 0 )
                            {
                                String gr = "";
                                for(int i = 0; i < groups.size();i++)
                                {
                                    gr += " "+groups.get(i);
                                }

                                sender.sendMessage(ChatColor.DARK_GREEN + "Promoted groups:"+ChatColor.DARK_RED+gr);
                            }
                            else
                                sender.sendMessage(ChatColor.DARK_GREEN + "Promoted groups:"+ChatColor.DARK_RED+" None yet");
                            
                            SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
                            
                            Date date = null;
                            
                            try
                            {
                                date = df.parse(CONFIG.getString("nextReset"));
                            } catch(Exception e)
                            {
                                
                            }
                            Long zeit = (Long) Calendar.getInstance().getTimeInMillis()/1000;
                            
                            if (date != null)
                            {
                                Long sek = date.getTime()/1000-zeit;
                                sender.sendMessage(ChatColor.DARK_GREEN + "Next Reset in "+ChatColor.DARK_RED+formatSek(sek.toString()));
                            }
                        }
                        
                        
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.DARK_GREEN + "/ap info <player> - "+ChatColor.DARK_RED + "get informations about other players");
                        sender.sendMessage(ChatColor.DARK_GREEN + "/ap reload - "+ChatColor.DARK_RED + "reload ActivityPermission");
                    }
                    
                } 
                else
                {    
                    sender.sendMessage(ChatColor.DARK_GREEN + "/ap info <player> - "+ChatColor.DARK_RED + "get informations about other players");
                    sender.sendMessage(ChatColor.DARK_GREEN + "/ap reload - "+ChatColor.DARK_RED + "reload ActivityPermission");
               
                }
                // do something else...
            }
            return true;
	}
	return false;
    }
    
    private void checkConfig() {
        
        
        if (!this.getDataFolder().exists())
            this.getDataFolder().mkdir();
        
        File configFile = new File(this.getDataFolder(), "config.yml");
            
        
        
        if(configFile.exists() == false)
        {
            CONFIG = new Configuration (configFile);
            
            CONFIG.setProperty("groups.60.world", "world,einsacht");
            CONFIG.setProperty("groups.60.startGroup", "");
            CONFIG.setProperty("groups.60.promotionGroup", "examplegroup");
            
            CONFIG.setProperty("groups.120.world", "world,einsacht");
            CONFIG.setProperty("groups.120.startGroup", "Member");
            CONFIG.setProperty("groups.120.promotionGroup", "examplegroup2");
            
            CONFIG.setProperty("idleTime", 10);
            CONFIG.setProperty("resetActivity", Boolean.TRUE);
            CONFIG.setProperty("saveTotalTime", Boolean.TRUE);
            CONFIG.setProperty("saveLastLogout", Boolean.TRUE);
            CONFIG.setProperty("maxAway", 10080);
            CONFIG.setProperty("timePeriod", 43200);
            CONFIG.setProperty("nextReset", "2010-12-21 12:00:00");
            CONFIG.save();
        }
        else
            CONFIG = new Configuration (configFile);
        
        LIST = new Configuration(new File(this.getDataFolder(),"list.yml"));
        
        
        
        //check if we have passed a resetdate to reset activity
        //log.log(Level.INFO, "[ActivityPromotion "+this.getDescription().getVersion()+"] Config loaded");
        
        CONFIG.load();
        LIST.load();
        
        checkReset();
        
        List<String> groups = CONFIG.getKeys("groups");

        log.log(Level.INFO,"[ActivityPromotion "+this.getDescription().getVersion()+"] "+String.valueOf(groups.size())+" groups found. parsing...");

        for(int i=0; i < groups.size(); i++){
            String time = String.valueOf(groups.get(i));
            
            String groupname = CONFIG.getString("groups."+time+".promotionGroup");
                    //CONFIG.getString("groups."+ String.valueOf(time) + ".group");
            
            PromotionGroups.put(Long.valueOf(time), groupname);
        }
        
    }

    public void updatePlayer(String name) {
        
        if (this.TimePlayed.containsKey(name))
        {
            Long time = this.TimePlayed.get(name);
            //Update Time
            
            Long aktime = (long) Calendar.getInstance().getTimeInMillis()/1000;
            
            Long test = aktime - this.TimeLastAction.get(name);
            
            
            if(aktime - this.TimeLastAction.get(name) <= idleTime)
            {
                this.TimePlayed.put(name, (Long) time + (aktime - this.TimeLastAction.get(name)));
            }
            
            this.TimeLastAction.put(name, (Long) (Calendar.getInstance().getTimeInMillis()/1000));
        }
        else
        {
            this.TimePlayed.put(name, Long.parseLong("0"));
            
            this.TimeLastAction.put(name, (Long) (Calendar.getInstance().getTimeInMillis()/1000));
            
        }
        
        this.lastLogout.put(name,(long) Calendar.getInstance().getTimeInMillis()/1000);
       
    }

    void initiatePlayer(Player player) {
        
        String name = player.getName();
        
        if (!this.TimePlayed.containsKey(name))
        {
            //check the LIST
            String test = LIST.getString("players." + name+".activityTime");
            
            if (test != null)
            {
                this.TimePlayed.put(name, Long.parseLong(LIST.getString("players." + name+".activityTime")));
                this.passivePeriod.put(name, LIST.getBoolean("players." + name+".passivePeriod", false));
            }
            else
            {    
                this.TimePlayed.put(name, Long.parseLong("0"));
                this.passivePeriod.put(name, Boolean.FALSE);
            }
        }
        if(!CONFIG.getBoolean("saveTotalTime", false))
        {
            LIST.setProperty("players."+name+".totalTime", 0);
        }
        
        
        
        this.TimeLastAction.put(name, Calendar.getInstance().getTimeInMillis()/1000);
        
        checkReset();
        checkPromotion(player);
    }

    void finishPlayer(PlayerQuitEvent event) {
        //whenever a player quits we will save the map
        
        Long time = this.TimePlayed.get(event.getPlayer().getName());
        //Update Time
        
        Long aktime = (Calendar.getInstance().getTimeInMillis()/1000);
        this.lastLogout.put(event.getPlayer().getName(), aktime);
        
        if(aktime - this.TimeLastAction.get(event.getPlayer().getName()) <= idleTime)
        {
            this.TimePlayed.put(event.getPlayer().getName(), time +  (aktime - this.TimeLastAction.get(event.getPlayer().getName())));
        }
        
        
        
        
        this.saveList();
        checkReset();
    }

    private void saveList() {
       
        for( Map.Entry<String, Long> entry : TimePlayed.entrySet() )
        {
          String pl = "players." + entry.getKey();
          Long time = entry.getValue();
          
          LIST.setProperty(pl+".passivePeriod", passivePeriod.get(entry.getKey()));
          
          
          if (LIST.getString(pl+".activityTime") != null)
              {
                Long totaltime = Long.parseLong(LIST.getString(pl+".activityTime"));
                if (time > totaltime)
                    LIST.setProperty(pl+".activityTime", time);
                else
                    LIST.setProperty(pl+".activityTime", totaltime);
              }
          else
          {
              LIST.setProperty(pl+".activityTime", time);
          }
          
          if(CONFIG.getBoolean("saveTotalTime", true))
          {
          //    if (LIST.getString(pl+".totalTime") != null)
          //   {
          //        Long totaltime = Long.parseLong(LIST.getString(pl+".totalTime"));
          //        LIST.setProperty(pl+".totalTime", totaltime + time);
          //    }
          //    else   
          //    {
          //        LIST.setProperty(pl+".totalTime", time);
          //    }
          }
          if(CONFIG.getBoolean("saveLastLogout", true))
          {
              LIST.setProperty(pl+".lastLogout", this.lastLogout.get(entry.getKey()));
          }
                  
        }
        
        //save
        LIST.save();
        LIST.load();
        
    }
    public void checkPromotion(Player player)
    {
        List<String> promotedGroups = new ArrayList<String>();
        List<String> removedGroups = new ArrayList<String>();
            
        for( Map.Entry<Long, String> entry : PromotionGroups.entrySet() )
        {
            Long time = entry.getKey();
            String group = entry.getValue();
           
            
            
            String world = CONFIG.getString("groups."+String.valueOf(time)+".world");
            
            String[] worlds = world.split(",");
            
            String startGroup = CONFIG.getString("groups."+String.valueOf(time)+".startGroup");
            
            if (TimePlayed.get(player.getName()) > time)
            {
                
                //player.sendMessage("You will be promoted...");
                //promote Player if not promoted yet
                
                if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
                    PermissionManager permissions = PermissionsEx.getPermissionManager();
                    
                    //PermissionsEx
                    //permissions.getUser(player.getName()).getGroupsNames()
                   boolean found = false;
                   boolean promotion = false;
                   
                   //checken, ob starGroup leer ist, wenn ja, dann immer befördern
                   if(startGroup.isEmpty())
                       promotion = true;
                   
                   for(String name: permissions.getUser(player).getGroupsNames()) {
                        if(group.equals(name))
                        {
                            //found a group
                            //User is already in that group
                            found = true;
                        }
                        if (group.equals(startGroup))
                            promotion = true;
                    }
                   if(!found && promotion)
                   {
                       //promote Player to the group
                       for(int i = 0; i < worlds.length; i++)
                       {
                           //check if world exists
                            if (getServer().getWorld(worlds[i]) == null)
                            {
                                log.warning("[ActivityPromotion "+this.getDescription().getVersion()+"] Wolrd "+worlds[i]+" not found. Check your Configfile");
                                continue;
                            }
                           
                           permissions.getUser(player).addGroup(group, worlds[i]);
                           if(!promotedGroups.contains(group))
                           promotedGroups.add(group);
                           //player.sendMessage(ChatColor.DARK_GREEN +"You have been promoted to group: "+ChatColor.DARK_RED + group); 
                           //player.sendMessage(ChatColor.DARK_GREEN +"Group: "+ChatColor.DARK_RED + group);
                           //player.sendMessage(ChatColor.DARK_GREEN +"World: "+ChatColor.DARK_RED + worlds[i]);
                       }
                       saveList();
                       checkReset();
                   }

                } else if(Bukkit.getServer().getPluginManager().isPluginEnabled("bPermissions"))
                {
                    
                    WorldPermissionsManager wpm = null;
                    try {
                        wpm = Permissions.getWorldPermissionsManager();
                    } catch (Exception e) {
                        log.warning("bPermissions not detected!");
                        this.getPluginLoader().disablePlugin(this);
                    }
                    for(int i = 0; i < worlds.length; i++)
                    {
                        
                        //check if world exists
                        if (getServer().getWorld(worlds[i]) == null)
                        {
                            log.warning("[ActivityPromotion "+this.getDescription().getVersion()+"] World "+worlds[i]+" not found. Check your Configfile");
                            continue;
                        }
                        boolean promotion = false;

                        //checken, ob starGroup leer ist, wenn ja, dann immer befördern
                       if(startGroup.isEmpty())
                           promotion = true;

                        boolean found = false;
                        
                        List<String> groups = null;
                        try {
                            groups = wpm.getPermissionSet(worlds[i]).getGroups(player.getName());
                        } catch (Exception e)
                        {
                            log.log(Level.WARNING, "ERROR!!!", e);
                            this.getPluginLoader().disablePlugin(this);
                        }
                        if(groups != null)
                        {
                            for(int j=0; j < groups.size();j++) {
                                if(group.equals(groups.get(j)))
                                {
                                    //found a group
                                    //User is already in that group
                                    found = true;
                                }
                                if (groups.get(j).equals(startGroup))
                                    promotion = true;
                            }
                        }
                        if(!found && promotion)
                        {
                           //promote Player to the group
                           
                               wpm.getPermissionSet(worlds[i]).addGroup(player, group);
                               if(!promotedGroups.contains(group))
                               promotedGroups.add(group);
                               //player.sendMessage(ChatColor.DARK_GREEN +"You have been promoted to group: "+ChatColor.DARK_RED + group);  
                               //player.sendMessage(ChatColor.DARK_GREEN +"Group: "+ChatColor.DARK_RED + group);
                               //player.sendMessage(ChatColor.DARK_GREEN +"World: "+ChatColor.DARK_RED + worlds[i]);
                           
                           saveList();
                           checkReset();

                        }
                    }
                }
                else
                {
                    log.warning("No Permissionsplugin detected... disabling");
                    this.getPluginLoader().disablePlugin(this);
                }
                
                
            }
            else
            {
                //let us check if we have to depromote a Player
                
                if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
                    PermissionManager permissions = PermissionsEx.getPermissionManager();
                    
                    
                    for(String name: permissions.getUser(player).getGroupsNames()) {
                        if(group.equals(name))
                        {
                            //found a group the user shouldnt be in atm
                            //so cheking passiveMonth
                            boolean degrade = false;
                            
                            //checken, ob starGroup leer ist, wenn ja, dann immer befördern
                           if(startGroup.isEmpty())
                               degrade = true;
                            
                            for(String name2: permissions.getUser(player).getGroupsNames()) 
                            {
                                if (name2.equals(startGroup))
                                        degrade = true;
                            }
                            
                            if(!passivePeriod.get(player.getName()))
                            {    
                                for(int i = 0; i < worlds.length; i++)
                                {
                                    //check if world exists
                                    if (getServer().getWorld(worlds[i]) == null)
                                    {
                                        log.warning("[ActivityPromotion "+this.getDescription().getVersion()+"] World "+worlds[i]+" not found. Check your Configfile");
                                        continue;
                                    }
                                    
                                    if (!degrade)
                                       continue;
                                    
                                    permissions.getUser(player).removeGroup(group,worlds[i]);
                                    if(!removedGroups.contains(group))
                                        removedGroups.add(group);
                                    //player.sendMessage(ChatColor.DARK_GREEN +"You have been removed from group: "+ChatColor.DARK_RED + group); 
                                    //player.sendMessage(ChatColor.DARK_GREEN +"You have been degraded!"); 
                                    //player.sendMessage(ChatColor.DARK_GREEN +"Group: "+ChatColor.DARK_RED + group);
                                    //player.sendMessage(ChatColor.DARK_GREEN +"World: "+ChatColor.DARK_RED + worlds[i]);
                                }
                                saveList();
                                checkReset();
                            }
                        }
                    }
                }
                else if(Bukkit.getServer().getPluginManager().isPluginEnabled("bPermissions")){
                    
                    WorldPermissionsManager wpm = null;
                    try {
                        wpm = Permissions.getWorldPermissionsManager();
                    } catch (Exception e) {
                        log.warning("bPermissions not detected!");
                        this.getPluginLoader().disablePlugin(this);
                    }
                    
                    
                    for(int i = 0; i < worlds.length; i++)
                    {  
                        
                        //check if world exists
                        if (getServer().getWorld(worlds[i]) == null)
                        {
                            log.warning("[ActivityPromotion "+this.getDescription().getVersion()+"] World "+worlds[i]+" not found. Check the config.yml");
                            continue;
                        }
                            
                        List<String> groups = wpm.getPermissionSet(worlds[i]).getGroups(player.getName());
                        
                        if(groups != null)
                        {
                            for(int j=0; j < groups.size();j++) 
                            { 
                                

                                if(group.equals(groups.get(j)))
                                {
                                    boolean degrade = false;

                                    //checken, ob starGroup leer ist, wenn ja, dann immer befördern
                                   if(startGroup.isEmpty())
                                       degrade = true;

                                    for(String name2: wpm.getPermissionSet(worlds[i]).getGroups(player)) 
                                    {
                                        if (name2.equals(startGroup))
                                                degrade = true;
                                    }

                                    if(!passivePeriod.get(player.getName()))
                                    { 
                                            if (!degrade)
                                                continue;
                                            
                                            wpm.getPermissionSet(worlds[i]).removeGroup(player, group);
                                            if(!removedGroups.contains(group))
                                                removedGroups.add(group);
                                            //player.sendMessage(ChatColor.DARK_GREEN +"You have been removed from group: "+ChatColor.DARK_RED + group);  
                                            //player.sendMessage(ChatColor.DARK_GREEN +"Group: "+ChatColor.DARK_RED + group);
                                            //player.sendMessage(ChatColor.DARK_GREEN +"World: "+ChatColor.DARK_RED + worlds[i]);

                                        saveList();
                                        checkReset();
                                    }
                                }

                            }
                        }
                    }
                        
                }
                else
                {
                    log.warning("No Permissionsplugin detected... disabling");
                    this.getPluginLoader().disablePlugin(this);
                }
                
            }
            
        }
        
        
        
        if(promotedGroups.size() > 0 )
        {
            String gr = "";
            for(int i = 0; i < promotedGroups.size();i++)
            {
                gr += " "+promotedGroups.get(i);
            }
            
            player.sendMessage(ChatColor.DARK_GREEN +"You have been promoted to group: "+ChatColor.DARK_RED + gr);
        }
        if(removedGroups.size() > 0 )
        {
            String gr = "";
            for(int i = 0; i < removedGroups.size();i++)
            {
                gr += " "+removedGroups.get(i);
            }
            
            player.sendMessage(ChatColor.DARK_GREEN +"You have been removed from group: "+ChatColor.DARK_RED + gr);  
        }
        
    }

    public void checkReset() {
        
        if (CONFIG.getBoolean("resetActivity", true) == false)
            return;
        
        saveList();
        
        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        
        Date date = null;
        
        try {
            date  = df.parse(CONFIG.getString("nextReset"));
        } catch (ParseException ex) {
            log.log(Level.WARNING, "[ActivityPromotion "+this.getDescription().getVersion()+"] Cant read Date in config.yml", ex);
            
            //diable Plugin
            this.getPluginLoader().disablePlugin(this);
        }
        
        if (date.compareTo(new Date()) <= 0 && CONFIG.getBoolean("resetActivity", true) == true)
        {
            //date passed, reset the activity
            log.log(Level.INFO, "[ActivityPromotion "+this.getDescription().getVersion()+"] A resetDate has been passed. Reseting all stats");
            
            LIST.load();
            
            List<String> players = LIST.getKeys("players");
            
            if(players != null)
            {
                for (int i = 0; i < players.size(); i++)
                {
                    LIST.setProperty("players." + players.get(i)+".passivePeriod", Boolean.FALSE);
                    passivePeriod.put(players.get(i), Boolean.FALSE);
                    
                    for( Map.Entry<Long, String> entry : PromotionGroups.entrySet() )
                    {
                      String group = entry.getValue();
                      Long time = entry.getKey();

                      if (TimePlayed.get(players.get(i)) >= time)
                      {
                          //set passivePeriod
                          LIST.setProperty("players." + players.get(i)+".passivePeriod", Boolean.TRUE);
                          passivePeriod.put(players.get(i), Boolean.TRUE);
                      }
                    }
                    
                    if (CONFIG.getBoolean("saveTotalTime", false))
                    {
                        Long actime = Long.valueOf(LIST.getString("players." + players.get(i)+".activityTime"));
                        
                        Long tTime;
                        
                        try
                        {
                            tTime = Long.valueOf(LIST.getString("players." + players.get(i)+".totalTime"));
                        } catch (Exception e)
                        {
                            tTime = Long.parseLong("0");
                        }
                        LIST.setProperty("players."+players.get(i)+".totalTime", actime + tTime);
                    }
                    
                    LIST.setProperty("players."+players.get(i)+".activityTime", 0);
                    TimePlayed.put(players.get(i), Long.parseLong("0"));
                }
            }
            //everything done, so lets set the new date...
            // create Calendar instance with actual date
            Calendar calendar = new GregorianCalendar();

            //set Time
            calendar.setTime(date);
            
            // add x minutes to calendar instance
            calendar.add(Calendar.MINUTE, Integer.parseInt(CONFIG.getString("timePeriod")));
            
            // get the date instance
            Date future = calendar.getTime();
            
            CONFIG.setProperty("nextReset", df.format(future));
            
            CONFIG.save();
            saveList();
            
            log.info("[ActivityPromotion "+this.getDescription().getVersion()+"] new resetDate set to "+df.format(future));
            getServer().broadcastMessage(ChatColor.DARK_GREEN + "Activity resetted.");
            getServer().broadcastMessage(ChatColor.DARK_GREEN + "Next reset in "+ChatColor.DARK_RED+formatSek(Long.toString(Long.valueOf(CONFIG.getString("timePeriod"))*60)));
            
        //done later
            //reset all stats and set passivePeriod
            
            
        }
    }

    void showAwayUser(Player player) {
        if (player.hasPermission("activitypromotion.loginmessage"))
        {
            
            if(CONFIG.getBoolean("saveLastLogout", false))
            {
                //show all Users, which their last logout was some while ago
                String users = "";

                List<String> empty = new ArrayList<String>();
                
                for(String name : LIST.getStringList("players", empty))
                {
                    Long tsp = Long.valueOf(LIST.getString("players."+name+"lastLogout"));
                    Long akt = Calendar.getInstance().getTimeInMillis()/1000;
                    if (akt - tsp >= Long.valueOf(CONFIG.getString("maxAway")))
                    {
                        //Too long away
                        users += " " + name;
                    }                    

                }
                
                if(!users.trim().isEmpty())
                {
                    player.sendMessage(ChatColor.DARK_GREEN + "The following People haven't been online for "+formatSek(Long.toString(Long.valueOf(CONFIG.getString("maxAway"))*60)));
                    player.sendMessage(ChatColor.DARK_RED + users);
                }
                else
                {
                    player.sendMessage(ChatColor.DARK_GREEN + "No Player is inactive for more than " + formatSek(Long.toString(Long.valueOf(CONFIG.getString("maxAway"))*60)));
                }
            }
        }
    }
    
    
    
}
