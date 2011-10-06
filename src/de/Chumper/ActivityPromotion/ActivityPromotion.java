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
    private Map<String, Long> TimePlayed = new HashMap<String, Long>();
    private Map<String, Long> TimeLastAction = new HashMap<String, Long>();
    private Map<Long, String> PromotionGroups = new HashMap<Long, String>();
    private Map<String, Boolean> passivePeriod = new HashMap<String, Boolean>();
    private Map<String, Long> lastLogout = new HashMap<String, Long>();
    
    public Logger log; 
    private PluginManager pm;
    
    protected static Configuration CONFIG;
    protected static Configuration LIST;
    
    private Permission PermissionHandler;
    
    private Long idleTime;
    
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
        
        setupPermission();
        
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
                        if (PermissionHandler.hasNode(player, "activitypromotion.reload",player.getWorld().getName()) || player.isOp())
                        {
                            saveList();
                            log.info("[ActivityPromotion "+this.getDescription().getVersion()+"] reload");
                            //config
                            this.checkConfig();

                            this.idleTime = Long.parseLong(CONFIG.getString("idleTime"));

                            for(Player pplayer: getServer().getOnlinePlayers()) {
                                initiatePlayer(pplayer);
                            }
                            sender.sendMessage(ChatColor.DARK_GREEN+"Activity Promotion reloaded");

                        } else
                        {
                            sender.sendMessage(ChatColor.DARK_GREEN+"You are not allowed to do that");
                            return true;
                        }
                    }
                    else if(args[0].equals("info"))
                    {
                        if (PermissionHandler.hasNode(player, "activitypromotion.info",player.getWorld().getName()) || player.isOp())
                        {
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

                                    for(String world : worlds.split(","))
                                    {
                                        if (PermissionHandler.isInGroup(player, group, world))
                                            if(!groups.contains(group))
                                                groups.add(group);

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
                            sender.sendMessage(ChatColor.DARK_GREEN+"You are not allowed to do that");
                            return true;
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
            
            List<String> worlds = CONFIG.getStringList("groups."+String.valueOf(time)+".world",new ArrayList<String>());
            
            List<String> startGroup = CONFIG.getStringList("groups."+String.valueOf(time)+".startGroup",new ArrayList<String>());
            
            if (TimePlayed.get(player.getName()) > time)
            {
                
               if (time < 0 && time*-1 < TimePlayed.get(player.getName()))
               {
                   //Negative Group - we will skip them, because the played time is above the criteria
                   continue;
               }
               else
               {
                   //degrade
               }
               
               
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
                                    promotedGroups.add(groupname);
                               }
                           }
                           else
                           {
                               if (PermissionHandler.isInGroup(player, groupname.replace("^", ""), world))
                               {
                                    PermissionHandler.removeGroup(player, groupname.replace("^", ""), world);
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
                if(!passivePeriod.get(player.getName()))
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
                                   if (PermissionHandler.isInGroup(player, groupname, world) == false)
                                   {                               
                                        PermissionHandler.removeGroup(player, groupname, world);
                                        removedGroups.add(groupname);
                                   }
                               }
                               else
                               {
                                   if (PermissionHandler.isInGroup(player, groupname.replace("^", ""), world))
                                   {
                                        PermissionHandler.addGroup(player, groupname.replace("^", ""), world);
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
                                   if (!PermissionHandler.hasNode(player, node, world))
                                   {
                                       PermissionHandler.removeNode(player, node, world);
                                   }
                               }
                               else
                               {
                                   if (PermissionHandler.hasNode(player, node, world))
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
                                           if (PermissionHandler.isInGroup(player, groupname, world) == false)
                                           {                               
                                                PermissionHandler.addGroup(player, groupname, world);
                                                
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
            
            while (calendar.getTimeInMillis()/1000 < Calendar.getInstance().getTimeInMillis()/1000)
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

    private void setupPermission() {
        
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
            PermissionHandler = (Permission) new PermissionEx(this);
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
