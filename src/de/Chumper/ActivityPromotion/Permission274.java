/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.Chumper.ActivityPromotion;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Nils
 */
class Permission274 implements Permission{
    private final ActivityPromotion plugin;
    private final PermissionHandler permissions;
    private Player player;

    public Permission274(ActivityPromotion instance) {
        plugin = instance;
        
        Plugin permissionsPlugin = plugin.getServer().getPluginManager().getPlugin("Permissions");

        permissions = ((Permissions) permissionsPlugin).getHandler();   
    }

    @Override
    public Boolean isInGroup(Player player, String group, String world) {
        return permissions.inGroup(world, player.getName(), group);
    }

    @Override
    public void addGroup(Player player, String group, String world) {
        plugin.log.warning(plugin.AP+"coudn't promote "+player.getName()+" to group "+group+" because Permissions 2.7.4 doesn't support Multigroups...");
        //throw new UnsupportedOperationException("Permissions 2.7.4 does not support multigroups...");
    }

    @Override
    public void removeGroup(Player player, String group, String world) {
        plugin.log.warning(plugin.AP+"coudn't remove "+player.getName()+" from group "+group+" because Permissions 2.7.4 doesn't support Multigroups...");
        //throw new UnsupportedOperationException("Permissions 2.7.4 does not support multigroups...");
    }

    @Override
    public void addNode(Player player, String node, String world) {
        permissions.addUserPermission(world, player.getName(), node);
    }

    @Override
    public void removeNode(Player player, String node, String world) {
        permissions.removeUserPermission(world, player.getName(), node);
    }

    @Override
    public Boolean hasNode(Player player, String node, String world) {
        Boolean test = false;
        try
        {
            test = permissions.has(world, player.getName(), node);
        }
        catch(Exception e)
        {
            plugin.log.warning(plugin.AP+"Please update your Permissionplugin... Even the check for a permission failed...");
        }
        return test;
    }
    
    @Override
    public void reload()
    {
        permissions.reload();
    }

}
