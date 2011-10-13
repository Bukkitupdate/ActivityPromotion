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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
/**
 *
 * @author Nils Plaschke
 */
public class ActivityPromotion extends JavaPlugin{

    
    ActivityPromotionPlayerListener playerListener = new ActivityPromotionPlayerListener(this);
    public Map<Long, String> PromotionGroups = new HashMap<Long, String>();
    
    //Neue Überlegung
    //Eine Hashmap, die das aktuelle Spielerobjekt beinhaltet...
    //Gute Idee :)
    //Hashmap(name, Objekt)
    public Map<String,APPlayer> PLAYER = new HashMap<String,APPlayer>();
    
    public Logger log; 
    private PluginManager pm;
    
    protected static Configuration CONFIG;
    
    private Permission PermissionHandler;
    private CFileHandler FileHandler;
    public Long idleTime;
    
    public String AP;
    
    @Override
    public void onEnable(){
 
        log = this.getServer().getLogger();
        pm = this.getServer().getPluginManager();
        AP = "[ActivityPromotion "+this.getDescription().getVersion()+"] ";
       
        setupPermission();
        
        ActivityPromotionCommandExecutor myExecutor = new ActivityPromotionCommandExecutor(this,PermissionHandler);
	getCommand("ap").setExecutor(myExecutor);
        
        //config
        this.checkConfig();
        this.checkFileHandler();
        
        PLAYER = FileHandler.load();
        
        this.idleTime = Long.parseLong(CONFIG.getString("idleTime"));
        
        for(Player player: getServer().getOnlinePlayers()) {
            initiatePlayer(player);
        }
       
        //register Events
        pm.registerEvent(Event.Type.PLAYER_ANIMATION, playerListener, Event.Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Lowest, this);
        
        
        
        log.log(Level.INFO, "[ActivityPromotion "+this.getDescription().getVersion()+"] enabled");
    }
    
    @Override
    public void onDisable(){
        
        //CONFIG.save();
        FileHandler.save(PLAYER);
        FileHandler.close();
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
            
            CONFIG.setProperty("db.enable", Boolean.FALSE);
            CONFIG.setProperty("db.adress", "localhost");
            CONFIG.setProperty("db.port", 3306);
            CONFIG.setProperty("db.database", "AP");
            CONFIG.setProperty("db.user", "root");
            CONFIG.setProperty("db.pass", "");
            
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
        
        CONFIG.load();
        
        checkReset();
        
        List<String> groups = CONFIG.getKeys("groups");

        log.log(Level.INFO,"[ActivityPromotion "+this.getDescription().getVersion()+"] "+String.valueOf(groups.size())+" groups found. parsing...");

        for(int i=0; i < groups.size(); i++){
            String time = String.valueOf(groups.get(i));
            
            String groupname = CONFIG.getString("groups."+time+".promotionGroup");
            
            PromotionGroups.put(Long.valueOf(time), groupname);
        }
        
    }

    public void updatePlayer(String name) {
        
        
        
        if (PLAYER.containsKey(name) && PLAYER.get(name) != null)
        {
                
            APPlayer tmp = PLAYER.get(name);
            
            Long time = tmp.getTimePlayed();
            //Update Time
            
            Long aktime = (long) Calendar.getInstance().getTimeInMillis()/1000;
            
            Long test = aktime - tmp.getTimeLastAction();
            
            Long lA = tmp.getTimeLastAction();
            
            if(aktime - lA <= idleTime)
            {
                tmp.setTimePlayed( time + (aktime - lA));
            }
            
            tmp.setTimeLastAction(Calendar.getInstance().getTimeInMillis()/1000);
            
            PLAYER.put(name, tmp);
        }
        else
        {
            //User is not in PLAYER, create a new one
            APPlayer tmp = new APPlayer();
            tmp.setLastLogout(Calendar.getInstance().getTimeInMillis()/1000);
            tmp.setPassivePeriod(Long.valueOf("0"));
            tmp.setTimeLastAction(Calendar.getInstance().getTimeInMillis()/1000);
            tmp.setTimePlayed(Long.valueOf("0"));
            tmp.setTotalTime(Long.valueOf("0"));
            
            //now put the player in the Hashmap
            PLAYER.put(name, tmp);
        }
        
        PLAYER.get(name).setLastLogout(Calendar.getInstance().getTimeInMillis()/1000);
    }

    void initiatePlayer(Player player) {
        
        String name = player.getName();
        
        if (PLAYER.containsKey(name) && PLAYER.get(name) != null)
        {
            APPlayer tmp = FileHandler.loadPlayer(name);
            
            PLAYER.put(name, tmp);
        }
        else
        {
            APPlayer tmp = new APPlayer();
            tmp.setLastLogout(Calendar.getInstance().getTimeInMillis()/1000);
            tmp.setPassivePeriod(Long.valueOf("0"));
            tmp.setTimeLastAction(Calendar.getInstance().getTimeInMillis()/1000);
            tmp.setTotalTime(Long.valueOf("0"));
            tmp.setTimePlayed(Long.valueOf("0"));
            
            PLAYER.put(name, tmp);
            
        }

        log.warning(AP+Boolean.toString(PLAYER.containsKey(name)));
        
        
        PLAYER.get(name).setTimeLastAction(Calendar.getInstance().getTimeInMillis()/1000);
    }

    void finishPlayer(Player player) {
        //whenever a player quits we will save the specific entry
        
        Long time = PLAYER.get(player.getName()).getTimePlayed();
        //Update Time
        
        Long aktime = (Calendar.getInstance().getTimeInMillis()/1000);
        
        PLAYER.get(player.getName()).setLastLogout(aktime);
        
        Long lA = PLAYER.get(player.getName()).getTimeLastAction();
        
        if(aktime - lA <= idleTime)
        {
            PLAYER.get(player.getName()).setTimePlayed(time + (aktime - lA));
        }
        
        FileHandler.savePlayer(player.getName(), PLAYER.get(player.getName()));
        checkReset();
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
               endTime = Long.toString(PLAYER.get(player.getName()).getTimePlayed()+100);
            
            if (PLAYER.get(player.getName()).getTimePlayed() > time && PLAYER.get(player.getName()).getTimePlayed() < Long.valueOf(endTime))
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
                if(PLAYER.get(player.getName()).getPassivePeriod() < time || PLAYER.get(player.getName()).getPassivePeriod() > Long.valueOf(endTime))
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
            //PermissionHandler.reload();
        }
        if(removedGroups.size() > 0 )
        {
            String gr = "";
            for(int i = 0; i < removedGroups.size();i++)
            {
                gr += " "+removedGroups.get(i);
            }
            
            player.sendMessage(ChatColor.DARK_GREEN +"You have been removed from group: "+ChatColor.DARK_RED + gr);  
            //PermissionHandler.reload();
        }
        
    }

    public void checkReset() {
        
        if (CONFIG.getBoolean("resetActivity", true) == false)
            return;
        
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
            
            //--- NEW
            
            for( Map.Entry<String, APPlayer> entry : PLAYER.entrySet() )
            {
                String name = entry.getKey();
                APPlayer pl = entry.getValue();
                Player player = this.getServer().getPlayer(name); 
                
                //first of all set passivePeriod to zero (0)
                pl.setPassivePeriod(Long.valueOf("0"));
              
                //now go through the promotion groups
                for( Map.Entry<Long, String> entry2 : PromotionGroups.entrySet() )
                {
                    String group = entry2.getValue();
                    Long time = entry2.getKey();
                    
                    //set passiveperiod
                    if(PLAYER.get(name).getTimePlayed() > time)
                    {
                        PLAYER.get(name).setPassivePeriod(PLAYER.get(name).getTimePlayed());
                    }
                    
                    //now check if the group is default or not
                    //so we can promote the players
                    if(CONFIG.getBoolean("groups."+time+".default", false))
                    {
                        //the group IS default so we have to promote the player
                        //but first we set the group default to false
                        CONFIG.setProperty("groups."+time+".default", Boolean.FALSE);
                        
                        //Now promote the player
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
                    //After promoting we will set the totalTime
                    PLAYER.get(name).setTotalTime(PLAYER.get(name).getTotalTime() + PLAYER.get(name).getTimePlayed());
                    
                    //At least set timePlayed to zero (0) and everything is done
                    PLAYER.get(name).setTimePlayed(Long.valueOf("0"));
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
            
            log.info("[ActivityPromotion "+this.getDescription().getVersion()+"] new resetDate set to "+df.format(future));
            getServer().broadcastMessage(ChatColor.DARK_GREEN + "Activity resetted.");
            getServer().broadcastMessage(ChatColor.DARK_GREEN + "Next reset in "+ChatColor.DARK_RED+formatSek(Long.toString(Long.valueOf(CONFIG.getString("timePeriod"))*60)));
            
            //completly done
        }
    }

    void showAwayUser(Player player) {
        if (PermissionHandler.hasNode(player, "activitypromotion.loginmessage",player.getWorld().getName()) || player.isOp())
        {
            //save the list is not necessary at the moment
            //saveList();
            
            if(CONFIG.getBoolean("saveLastLogout", false))
            {
                
                //save the list and load it again
                FileHandler.save(PLAYER);
                
                PLAYER = FileHandler.load();
                
                
                //show all Users, which their last logout was some while ago
                String users = "";

                for( Map.Entry<String, APPlayer> entry : PLAYER.entrySet() )
                {
                    String name = entry.getKey();
                    APPlayer pl = entry.getValue(); 
                    
                    Long maxA = Long.valueOf(CONFIG.getString("maxAway"));
                    
                    if(maxA == null)
                    {
                        maxA = Long.valueOf(10080*60);
                    }
                    else
                    {
                        maxA = maxA * 60;
                    }

                    Long akt = Calendar.getInstance().getTimeInMillis()/1000;
                    
                    if (akt - PLAYER.get(name).getLastLogout() >= maxA)
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

    private void checkFileHandler() {
        
        if (CONFIG.getBoolean("db.enable", false))
        {
            log.info(AP+" MySQL selected. Will try do connect");
            
            FileHandler = new MySQLHandler(this,
                                           CONFIG.getString("db.adress"), 
                                           CONFIG.getString("db.port"),
                                           CONFIG.getString("db.database"), 
                                           CONFIG.getString("db.user"),
                                           CONFIG.getString("db.pass"));
            
            
            
        }
        else
        {
            log.info(AP+" Normal yaml file selected. Will save everything in list.yml");
            
            FileHandler = new FlatFileHandler(this, "list.yml");
        }
    }
}