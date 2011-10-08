/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.Chumper.ActivityPromotion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Nils
 */
public class ActivityPromotionCommandExecutor implements CommandExecutor{
        
        private ActivityPromotion plugin;
        private final Permission PermissionHandler;
 
	public ActivityPromotionCommandExecutor(ActivityPromotion plugin, Permission permissions) {
		this.plugin = plugin;
                this.PermissionHandler = permissions;
	}
    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String string, String[] args) {
        Player player = null;
	if (sender instanceof Player) {
		player = (Player) sender;
	}
 
        if (args.length > 0)
                {    
                    if(args[0].equals("reload"))
                    {
                        if (PermissionHandler.hasNode(player, "activitypromotion.reload",player.getWorld().getName()) || player.isOp())
                        {
                            plugin.saveList();
                            plugin.log.info("[ActivityPromotion "+plugin.getDescription().getVersion()+"] reload");
                            //config
                            plugin.checkConfig();

                            plugin.idleTime = Long.parseLong(plugin.CONFIG.getString("idleTime"));

                            for(Player pplayer: plugin.getServer().getOnlinePlayers()) {
                                plugin.initiatePlayer(pplayer);
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
                            plugin.saveList();
                        
                            String actime;

                            try
                            {
                                actime = plugin.LIST.getString("players."+args[1]+".activityTime");
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
                                sender.sendMessage(ChatColor.DARK_GREEN+"ActivityTime: "+ChatColor.DARK_RED+plugin.formatSek(actime) +""+ ChatColor.DARK_GREEN+ "in " + ChatColor.DARK_RED + plugin.formatSek(Long.toString(Long.parseLong(plugin.CONFIG.getString("timePeriod"))*60))+" ("+Double.toString((Double)(Double.valueOf(actime)/60) / Double.valueOf(plugin.CONFIG.getString("timePeriod")) * 100 )+"%)");

                                if (plugin.CONFIG.getBoolean("saveTotalTime", false))
                                {
                                    String tT = null;
                                    try
                                    {
                                        tT = Long.toString(Long.valueOf(plugin.LIST.getString("players."+args[1]+".totalTime")) + plugin.TimePlayed.get(args[1]));
                                    }catch(Exception e)
                                    {}

                                    if ( tT != null && !tT.isEmpty())
                                    {
                                        sender.sendMessage(ChatColor.DARK_GREEN + "TotalTime: "+ChatColor.DARK_RED+plugin.formatSek(tT));
                                    }
                                    else
                                        sender.sendMessage(ChatColor.DARK_GREEN + "TotalTime: "+ChatColor.DARK_RED+"Can't read totalTime");
                                }
                                if (plugin.CONFIG.getBoolean("saveLastLogout", false))
                                {
                                    String lastlogout = plugin.LIST.getString("players."+args[1]+".lastLogout");

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

                                for( Map.Entry<Long, String> entry : plugin.PromotionGroups.entrySet() )
                                {
                                    Long time = entry.getKey();
                                    String group = entry.getValue(); 

                                    for(String world : plugin.CONFIG.getStringList("groups." + Long.toString(time) + ".world",new ArrayList<String>()))
                                    {
                                        
                                        for (String groupname : plugin.CONFIG.getStringList("groups."+Long.toString(time)+".promotionGroup",new ArrayList<String>()))
                                        {
                                            if(!groupname.startsWith("^"))
                                                if (PermissionHandler.isInGroup(player, groupname, world))
                                                    if(!groups.contains(groupname))
                                                        groups.add(groupname);

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
                                    date = df.parse(plugin.CONFIG.getString("nextReset"));
                                } catch(Exception e)
                                {

                                }
                                Long zeit = (Long) Calendar.getInstance().getTimeInMillis()/1000;

                                if (date != null)
                                {
                                    Long sek = date.getTime()/1000-zeit;
                                    sender.sendMessage(ChatColor.DARK_GREEN + "Next Reset in "+ChatColor.DARK_RED+plugin.formatSek(sek.toString()));
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
        return true;
    }
    
}
