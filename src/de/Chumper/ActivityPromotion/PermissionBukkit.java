/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.Chumper.ActivityPromotion;


import com.platymuus.bukkit.permissions.PermissionsPlugin;
import de.Chumper.ActivityPromotion.ActivityPromotion;
import de.Chumper.ActivityPromotion.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Nils
 */
public class PermissionBukkit implements Permission{
    private final ActivityPromotion plugin;
    private PermissionsPlugin permissions = null;
    private String playerName;

    PermissionBukkit(ActivityPromotion instance) {
        plugin = instance;
        
        init();
    }

    @Override
    public Boolean isInGroup(Player player, String group, String world) {
        if(permissions == null)
            init();
        
        //aplugin.log.warning(plugin.AP+"\""+group+"\"");
        //plugin.log.warning(plugin.AP+player.getName());
        
        //plugin.log.warning(Boolean.toString(permissions.isEnabled()));
        if (permissions.getGroup(group) == null)
        {
            plugin.log.warning(plugin.AP+"Group \""+group+"\" not found... Are you sure the name is right?");
            return false;
        }

        return permissions.getGroup(group).getPlayers().contains(player.getName().toLowerCase());
    }

    @Override
    public void addGroup(Player player, String group, String world) {
        if(permissions == null)
            init();
        
        if (permissions.getGroup(group) == null)
        {
            plugin.log.warning(plugin.AP+"Group \""+group+"\" not found... Are you sure the name is right?");  
        }
        else
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "permissions player addgroup "+player.getName()+ " "+group);
        
    }

    @Override
    public void removeGroup(Player player, String group, String world) {
        if(permissions == null)
            init();
        if (permissions.getGroup(group) == null)
        {
            plugin.log.warning(plugin.AP+"Group \""+group+"\" not found... Are you sure the name is right?");  
        }
        else
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "permissions player removegroup "+player.getName()+ " "+group);
        
    }

    @Override
    public void addNode(Player player, String node, String world) {
        if(permissions == null)
            init();
        
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "permissions player setperm "+player.getName()+ " "+world+":"+node+" true");
        
    }

    @Override
    public void removeNode(Player player, String node, String world) {
        if(permissions == null)
            init();
        
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "permissions player unsetperm "+player.getName()+ " "+world+":"+node);
        
    }

    @Override
    public Boolean hasNode(Player player, String node, String world)
    {
        if(permissions == null)
            init();
        
        return player.hasPermission(node);
    }
    
    @Override
    public void reload()
    {
        if(permissions == null)
            init();
        
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "permissions reload");
    }

    private void init() {
        Plugin permBukkit = plugin.getServer().getPluginManager().getPlugin("PermissionsBukkit");
        
        if (permBukkit != null) 
        {
            permissions = (PermissionsPlugin) permBukkit;
        }
         else
        {
            plugin.log.warning(plugin.AP+"Can't grab PermissionsBukkit...");
        }
    }
}
