/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.Chumper.ActivityPromotion;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Nils
 */
class Permission316 implements Permission{
    private final ActivityPromotion plugin;
    private final PermissionHandler permissions;

    public Permission316(ActivityPromotion instance) {
        
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
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "pr "+player.getName()+ " w:"+world+" parents add "+group+" ");
        
    }

    @Override
    public void removeGroup(Player player, String group, String world) {
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "pr "+player.getName()+ " w:"+world+" parents remove "+group+" ");
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
        return permissions.has(world, player.getName(), world);
    }

    @Override
    public void reload() {
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "pr -reload all");
    }
    
}
