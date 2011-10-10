/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.Chumper.ActivityPromotion;

import de.bananaco.permissions.Permissions;
import de.bananaco.permissions.worlds.WorldPermissionsManager;
import java.util.List;
import org.bukkit.entity.Player;

/**
 *
 * @author Nils
 */
public class bPermission implements Permission{

    WorldPermissionsManager wpm = null;
    private final ActivityPromotion plugin;
    
    public bPermission(ActivityPromotion instance)
    {
        plugin = instance;
        
    }
    
    private void init()
    {
        try {
            wpm = Permissions.getWorldPermissionsManager();
        } catch (Exception e) {
            plugin.log.warning("bPermissions not detected!");
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    @Override
    public Boolean isInGroup(Player player, String group, String world) {
        
        if(wpm == null)
            init();
        
        List<String> check = null;
        
        try {
            check = wpm.getPermissionSet(world).getGroups(player);
        }
        catch( Exception e)
        {
            check = null;
        }
        
        if(check == null)
            return false;
        
        for (String groupname : check)
        {
            if (group.replace(",","").equals(groupname))
                return true;
        }
        
        return false;
    }

    @Override
    public void addGroup(Player player, String group, String world) {
        
        if(wpm == null)
            init();
        
        wpm.getPermissionSet(world).addGroup(player, group);
    }

    @Override
    public void removeGroup(Player player, String group, String world) {
        
        if(wpm == null)
            init();
        
        wpm.getPermissionSet(world).removeGroup(player, group);
    }

    @Override
    public void addNode(Player player, String node, String world) {
        
        if(wpm == null)
            init();
        
        if(!isInGroup(player, player.getName(), world))
        {
            wpm.getPermissionSet(world).addGroup(player, player.getName());
            if(!hasNode(player, node, world))
            {
                wpm.getPermissionSet(world).addNode(node, player.getName());
            }
        }
    }

    @Override
    public void removeNode(Player player, String node, String world) {
        
        if(wpm == null)
            init();
        
        if (isInGroup(player, player.getName(),world))
            wpm.getPermissionSet(world).removeNode(node, player.getName());
    }
    
    @Override
    public Boolean hasNode(Player player, String node, String world)
    {
        return player.hasPermission(node);
    }
    
    @Override
    public void reload()
    {
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "perm reload");
    }
    
}
