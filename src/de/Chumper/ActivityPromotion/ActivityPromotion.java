package de.Chumper.ActivityPromotion;

import java.io.File;
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
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
/**
 *
 * @author Nils Plaschke
 */
public class ActivityPromotion extends JavaPlugin{
    
    ActivityPromotionPlayerListener playerListener = new ActivityPromotionPlayerListener(this);
    public  Map<String, Long> TimePlayed = new HashMap<String, Long>();
    private Map<String, Long> TimeLastAction = new HashMap<String, Long>();
    public Map<Long, String> PromotionGroups = new HashMap<Long, String>();
    private Map<String, Long> passivePeriod = new HashMap<String, Long>();
    private Map<String, Long> lastLogout = new HashMap<String, Long>();
    
    public Logger log; 
    private PluginManager pm;
    
    protected static Configuration CONFIG;
    protected static Configuration LIST;
    
    private Permission PermissionHandler;
    
    public Long idleTime;
    
    public String AP;
    
    @Override
    public void onEnable(){
 
        log = this.getServer().getLogger();
        pm = this.getServer().getPluginManager();
        AP = "[ActivityPromotion "+this.getDescription().getVersion()+"] ";
        //register Events
        
        pm.registerEvent(Event.Type.PLAYER_ANIMATION, playerListener, Event.Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Lowest, this);
        
        setupPermission();
        
        ActivityPromotionCommandExecutor myExecutor = new ActivityPromotionCommandExecutor(this,PermissionHandler);
	getCommand("ap").setExecutor(myExecutor);
        
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
    public String formatSek(String time)
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
        if (ts >= 0)
            result += ts+ " sec ";
        
        return result;
    }
    
    public void checkConfig() {
        
        
        if (!this.getDataFolder().exists())
            this.getDataFolder().mkdir();
        
        File configFile = new File(this.getDataFolder(), "config.yml");
            
        
        
        if(configFile.exists() == false)
        {
            CONFIG = new Configuration (configFile);
            
            List<String> worlds = new ArrayList<String>();
            worlds.add("world");
            worlds.add("world_nether");
            
            List<String> groups = new ArrayList<String>();
            groups.add("examplegroup");
            groups.add("examplegroup2");
            
            List<String> groups2 = new ArrayList<String>();
            groups2.add("^examplegroup3");
            groups2.add("examplegroup4");
            
            List<String> groups3 = new ArrayList<String>();
            groups3.add("Member");
            
            List<String> pm = new ArrayList<String>();
            pm.add("example.build");
            pm.add("^example.admin");
            pm.add("activitypromotion.info");
            
            List<String> pm1 = new ArrayList<String>();
            pm1.add("example.build");
            pm1.add("^example.admin");
            
            List<String> iu = new ArrayList<String>();
            iu.add("Chumper_tm");
            iu.add("Notch");
            
            CONFIG.setProperty("groups.70.world", worlds);
            CONFIG.setProperty("groups.70.startGroup", "");
            CONFIG.setProperty("groups.70.promotionGroup", groups);
            CONFIG.setProperty("groups.70.permissions", pm);
            CONFIG.setProperty("groups.70.default", Boolean.TRUE);
            CONFIG.setProperty("groups.70.ignoreUser", iu);
            CONFIG.setProperty("groups.70.endTime", 80);
            
            CONFIG.setProperty("groups.0.world", worlds);
            CONFIG.setProperty("groups.0.startGroup", groups3);
            CONFIG.setProperty("groups.0.promotionGroup", groups2);
            CONFIG.setProperty("groups.0.permissions", pm1);
            CONFIG.setProperty("groups.0.default", Boolean.FALSE);
            CONFIG.setProperty("groups.0.ignoreUser", iu);
            CONFIG.setProperty("groups.0.endTime", 50);
            
            CONFIG.setProperty("idleTime", 10);
            CONFIG.setProperty("resetActivity", Boolean.TRUE);
            CONFIG.setProperty("saveTotalTime", Boolean.TRUE);
            CONFIG.setProperty("saveLastLogout", Boolean.TRUE);
            CONFIG.setProperty("maxAway", 10080);
            CONFIG.setProperty("timePeriod", 2);
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
        
        saveList();
        
        if (!this.TimePlayed.containsKey(name))
        {
            //check the LIST
            String test = LIST.getString("players." + name+".activityTime");
            
            if (test != null)
            {
                this.TimePlayed.put(name, Long.parseLong(LIST.getString("players." + name+".activityTime")));
                this.passivePeriod.put(name, Long.valueOf(LIST.getString("players." + name+".passivePeriod")));
            }
            else
            {    
                this.TimePlayed.put(name, Long.parseLong("0"));
                this.passivePeriod.put(name, Long.valueOf("0"));
            }
        }
        if(!CONFIG.getBoolean("saveTotalTime", false))
        {
            LIST.setProperty("players."+name+".totalTime", 0);
        }
        
        if(!CONFIG.getBoolean("saveLastLogout", false))
        {   
            String test = LIST.getString("players." + name+".lastLogout");
            if(test != null)
                lastLogout.put(name,Long.parseLong(LIST.getString("players." + name+".lastLogout")));
            else
                lastLogout.put(name,Long.parseLong("0"));
        }
        
        this.TimeLastAction.put(name, Calendar.getInstance().getTimeInMillis()/1000);
        
        checkReset();
        checkPromotion(player);
    }

    void finishPlayer(Player player) {
        //whenever a player quits we will save the map
        
        Long time = this.TimePlayed.get(player.getName());
        //Update Time
        
        Long aktime = (Calendar.getInstance().getTimeInMillis()/1000);
        this.lastLogout.put(player.getName(), aktime);
        
        if(aktime - this.TimeLastAction.get(player.getName()) <= idleTime)
        {
            this.TimePlayed.put(player.getName(), time +  (aktime - this.TimeLastAction.get(player.getName())));
        }
        
        
        
        
        this.saveList();
        checkReset();
    }

    public void saveList() {
       
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
              if(this.lastLogout.containsKey(entry.getKey()))
                  LIST.setProperty(pl+".lastLogout", this.lastLogout.get(entry.getKey()));
              else
              {
                  this.lastLogout.put(entry.getKey(), System.currentTimeMillis()/1000);
                  LIST.setProperty(pl+".lastLogout", System.currentTimeMillis()/1000);
              }
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
            
            List<String> worlds = CONFIG.getStringList("groups."+String.valueOf(time)+".world",new ArrayList<String>());
            
            List<String> startGroup = CONFIG.getStringList("groups."+String.valueOf(time)+".startGroup",new ArrayList<String>());
            
            String endTime = CONFIG.getString("groups."+Long.valueOf(time)+".endTime");
                
            if(endTime == null || endTime.isEmpty()) 
               endTime = Long.toString(TimePlayed.get(player.getName())+100);
            
            if (TimePlayed.get(player.getName()) > time && TimePlayed.get(player.getName()) < Long.valueOf(endTime))
            {
               
               //Ok, we have to asign all the groups and nodes...
               //let us check if we have to ignore the User or not...
               if(CONFIG.getStringList("groups."+time+"ignoreUser", new ArrayList<String>()).contains(player.getName()))
               {    
                   //I am sorry, but we have to ignore the User...
                   //log.info(AP+"ignoring user \""+player.getName()+"\"");
                   continue;
               }
               
               //Now let us check the StartGroup and see if hes in the group or not...
               Boolean promotion = false;
               
               for(String groupname : startGroup)
               {
                   for(String world: worlds)
                   {
                       if(getServer().getWorld(world) == null)
                       {
                           log.warning(AP+"Could not find the world \""+world+"\" May you spelled it wrong?");
                           continue;
                       }
                       
                       if(PermissionHandler.isInGroup(player, groupname.replace("^", ""), world))
                       {
                           promotion = true;
                       }
                   }
               }
               if(startGroup.isEmpty())
                   promotion = true;
               
               if (promotion)
               {
                   for(String groupname : CONFIG.getStringList("groups."+time+".promotionGroup", new ArrayList<String>()))
                   {
                       
                       for(String world : worlds)
                       {
                           if(getServer().getWorld(world) == null)
                           {
                               log.warning(AP+"Could not find the world \""+world+"\" May you spelled it wrong?");
                               continue;
                           }
                           if (!groupname.startsWith("^"))
                           {
                               if (PermissionHandler.isInGroup(player, groupname, world) == false)
                               {   
                                    PermissionHandler.addGroup(player, groupname, world);
                                    if(!promotedGroups.contains(groupname))
                                        promotedGroups.add(groupname);
                               }
                           }
                           else
                           {
                               if (PermissionHandler.isInGroup(player, groupname.replace("^", ""), world))
                               {
                                    PermissionHandler.removeGroup(player, groupname.replace("^", ""), world);
                                    
                                    if(!removedGroups.contains(groupname))
                                        removedGroups.add(groupname.replace("^", ""));
                               }
                           }
                       }
                   }
                   for(String node : CONFIG.getStringList("groups."+time+".permissions", new ArrayList<String>()))
                   {
                       for(String world : worlds)
                       {
                           if(getServer().getWorld(world) == null)
                           {
                               log.warning(AP+"Could not find the world \""+world+"\" May you spelled it wrong?");
                               continue;
                           }
                           
                           if (!node.startsWith("^"))
                           {
                               if (!PermissionHandler.hasNode(player, node, world))
                               {
                                   PermissionHandler.addNode(player, node, world);
                               }
                           }
                           else
                           {
                               if (PermissionHandler.hasNode(player, node, world))
                               {
                                   PermissionHandler.removeNode(player, node, world);
                               }
                           }
                       }
                   }
               } 
            }
            else
            {
                //let us check if we have to degrade a Player   
                if(passivePeriod.get(player.getName()) < time || passivePeriod.get(player.getName()) > Long.valueOf(endTime))
                {
                    //passivePeriod is false so check all groups if they have to be removed
                    //iterate all groups "promotionGroup"
                    if(CONFIG.getStringList("groups."+time+".ignoreUser", new ArrayList<String>()).contains(player.getName()))
                    {
                        //We have to ignore the User
                        continue;
                    }
                    Boolean degrade = false;
               
                   for(String groupname : startGroup)
                   {
                       for(String world: worlds)
                       {
                           if(getServer().getWorld(world) == null)
                           {
                               log.warning(AP+"Could not find the world \""+world+"\" May you spelled it wrong?");
                               continue;
                           }

                           if(PermissionHandler.isInGroup(player, groupname.replace("^", ""), world))
                           {
                               degrade = true;
                           }
                       }
                   }
                   if(startGroup.isEmpty())
                       degrade = true;
                   
                   if(degrade)
                   {
                       for(String groupname : CONFIG.getStringList("groups."+time+".promotionGroup", new ArrayList<String>()))
                       {

                           for(String world : worlds)
                           {
                               if(getServer().getWorld(world) == null)
                               {
                                   log.warning(AP+"Could not find the world \""+world+"\" May you spelled it wrong?");
                                   continue;
                               }
                               if (!groupname.startsWith("^"))
                               {
                                   if (PermissionHandler.isInGroup(player, groupname.replace("^",""), world) == true)
                                   {                               
                                        PermissionHandler.removeGroup(player, groupname, world);
                                        if(!removedGroups.contains(groupname))
                                            removedGroups.add(groupname);
                                   }
                               }
                               else
                               {
                                   if (PermissionHandler.isInGroup(player, groupname.replace("^", ""), world) == false)
                                   {
                                        PermissionHandler.addGroup(player, groupname.replace("^", ""), world);
                                        if(!promotedGroups.contains(groupname))
                                            promotedGroups.add(groupname.replace("^", ""));
                                   }
                               }
                           }
                       }
                       for(String node : CONFIG.getStringList("groups."+time+".permissions", new ArrayList<String>()))
                       {
                           for(String world : worlds)
                           {
                               if(getServer().getWorld(world) == null)
                               {
                                   log.warning(AP+"Could not find the world \""+world+"\" May you spelled it wrong?");
                                   continue;
                               }

                               if (!node.startsWith("^"))
                               {
                                   if (PermissionHandler.hasNode(player, node, world))
                                   {
                                       PermissionHandler.removeNode(player, node, world);
                                   }
                               }
                               else
                               {
                                   if (!PermissionHandler.hasNode(player, node, world))
                                   {
                                       PermissionHandler.addNode(player, node, world);
                                   }
                               }
                           }
                       }
                   }
                   
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
            PermissionHandler.reload();
        }
        if(removedGroups.size() > 0 )
        {
            String gr = "";
            for(int i = 0; i < removedGroups.size();i++)
            {
                gr += " "+removedGroups.get(i);
            }
            
            player.sendMessage(ChatColor.DARK_GREEN +"You have been removed from group: "+ChatColor.DARK_RED + gr);  
            PermissionHandler.reload();
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
                    LIST.setProperty("players." + players.get(i)+".passivePeriod", 0);
                    passivePeriod.put(players.get(i), Long.valueOf("0"));
                    
                    for( Map.Entry<Long, String> entry : PromotionGroups.entrySet() )
                    {
                      String group = entry.getValue();
                      Long time = entry.getKey();

                      if(!TimePlayed.containsKey(players.get(i)))
                          TimePlayed.put(players.get(i), Long.parseLong("0"));
                      
                      if (TimePlayed.get(players.get(i)) >= time)
                      {
                          //set passivePeriod
                          LIST.setProperty("players." + players.get(i)+".passivePeriod", TimePlayed.get(players.get(i)));
                          passivePeriod.put(players.get(i), TimePlayed.get(players.get(i)));
                      }
                      if(CONFIG.getBoolean("groups."+time+".default", false))
                      {
                          //default group, so set to false an pass the group to the player
                          CONFIG.setProperty("groups."+time+".default", Boolean.FALSE);
                          
                          //-- promote group and permission
                          for(String p : LIST.getKeys("players"))
                          {
                              Player player = getServer().getPlayer(p);
                              
                              for(String groupname : CONFIG.getStringList("groups."+time+".promotionGroup", new ArrayList<String>()))
                               {

                                   for(String world : CONFIG.getStringList("groups."+time+".world", new ArrayList<String>()))
                                   {
                                       if(getServer().getWorld(world) == null)
                                       {
                                           log.warning(AP+"Could not find the world \""+world+"\" May you spelled it wrong?");
                                           continue;
                                       }
                                       if (!groupname.startsWith("^"))
                                       {
                                           if (PermissionHandler.isInGroup(player, groupname.replace("^",""), world) == false)
                                           {                               
                                                PermissionHandler.addGroup(player, groupname.replace("^",""), world);
                                                
                                           }
                                       }
                                       else
                                       {
                                           if (PermissionHandler.isInGroup(player, groupname.replace("^", ""), world))
                                           {
                                                PermissionHandler.removeGroup(player, groupname.replace("^", ""), world);
                                                
                                           }
                                       }
                                   }
                               }
                               for(String node : CONFIG.getStringList("groups."+time+".permissions", new ArrayList<String>()))
                               {
                                   for(String world : CONFIG.getStringList("groups."+time+".world", new ArrayList<String>()))
                                   {
                                       if(getServer().getWorld(world) == null)
                                       {
                                           log.warning(AP+"Could not find the world \""+world+"\" May you spelled it wrong?");
                                           continue;
                                       }

                                       if (!node.startsWith("^"))
                                       {
                                           if (!PermissionHandler.hasNode(player, node, world))
                                           {
                                               PermissionHandler.addNode(player, node, world);
                                           }
                                       }
                                       else
                                       {
                                           if (PermissionHandler.hasNode(player, node, world))
                                           {
                                               PermissionHandler.removeNode(player, node, world);
                                           }
                                       }
                                   }
                               }
                          }
                          
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
            
            while (calendar.getTimeInMillis()/1000 <= Calendar.getInstance().getTimeInMillis()/1000)
            {
                // add x minutes to calendar instance
                calendar.add(Calendar.MINUTE, Integer.parseInt(CONFIG.getString("timePeriod")));
            }
            
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
        if (PermissionHandler.hasNode(player, "activitypromotion.loginmessage",player.getWorld().getName()) || player.isOp())
        {
            saveList();
            if(CONFIG.getBoolean("saveLastLogout", false))
            {
                //show all Users, which their last logout was some while ago
                String users = "";

                for( Map.Entry<String, Long> entry : lastLogout.entrySet() )
                {
                    String group = entry.getKey();
                    Long tsp = entry.getValue(); 
                    
                    

                    Long akt = Calendar.getInstance().getTimeInMillis()/1000;
                    if (akt - tsp >= (Long.valueOf(CONFIG.getString("maxAway"))*60))
                    {
                        //Too long away
                        users += " " + group;
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

    private void setupPermission() {
        
        if(Bukkit.getServer().getPluginManager().getPlugin("PermissionsEx") != null){
            PermissionHandler = (Permission) new PermissionEx(this);
            log.info(AP+"PermissionsEX detected");
            log.info(AP+"PermissionsEX is fully supporting ActivityPromotion");
            log.info(AP+"Have fun");
        }
        else if(Bukkit.getServer().getPluginManager().getPlugin("bPermissions") != null){
            PermissionHandler = (Permission) new bPermission(this);
            log.info(AP+"bPermissions detected");
            log.warning(AP+"bPermissions is not fully supporting individual nodes...");
            log.warning(AP+"trying with a workaround..."); 
        }
        else if(Bukkit.getServer().getPluginManager().getPlugin("PermissionsBukkit") != null){
            PermissionHandler = (Permission) new PermissionBukkit(this);
            log.info(AP+"PermissionsBukkit detected");
            log.warning(AP+"PermissionsBukkit is not fully supporting external calls...");
            log.warning(AP+"will work with ingamecommands...");
        }
        else if(Bukkit.getServer().getPluginManager().getPlugin("Permissions") != null){
            
            if ("2.7.4".equals(Bukkit.getServer().getPluginManager().getPlugin("Permissions").getDescription().getVersion()))
            {    
                PermissionHandler = (Permission) new Permission274(this);
                log.info(AP+"Permissions 2.7.4 detected");
                log.warning(AP+"Permissions 2.7.4 is not supporting multigroups...");
                log.warning(AP+"Please be aware, that promoting to groups wont work"); 
            }
            if ("3.1.6".equals(Bukkit.getServer().getPluginManager().getPlugin("Permissions").getDescription().getVersion()))
            {    
                PermissionHandler = (Permission) new Permission316(this);
                log.info(AP+"Permissions 3.1.6 detected");
                log.warning(AP+"Permissions 3.1.6 is not fully supporting external calls...");
                log.warning(AP+"will work with ingamecommands...");
            }
        }
        else
        {
            log.warning(AP+"No Permissions detected...");
        }
        
    }
}
