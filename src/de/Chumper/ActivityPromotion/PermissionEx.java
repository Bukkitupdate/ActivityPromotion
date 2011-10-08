/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.Chumper.ActivityPromotion;

import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;


/**
 *
 * @author Nils
 */
public class PermissionEx implements Permission{
    private final ActivityPromotion plugin;
    private PermissionManager permissions;

    PermissionEx(ActivityPromotion instance) {
        plugin = instance;
        
        permissions = PermissionsEx.getPermissionManager();
        
    }

    @Override
    public Boolean isInGroup(Player player, String group, String world) {
        
        if(permissions == null)
            permissions = PermissionsEx.getPermissionManager();
        
        return permissions.getUser(player).inGroup(group, world);
        
    }

    @Override
    public void addGroup(Player player, String group, String world) {
        if(permissions == null)
            permissions = PermissionsEx.getPermissionManager();
        
        permissions.getUser(player).addGroup(group, world);
    }

    @Override
    public void removeGroup(Player player, String group, String world) {
        if(permissions == null)
            permissions = PermissionsEx.getPermissionManager();
        
        permissions.getUser(player).removeGroup(group, world);
    }

    @Override
    public void addNode(Player player, String node, String world) {
        if(permissions == null)
            permissions = PermissionsEx.getPermissionManager();
        
        permissions.getUser(player).addPermission(node, world);
    }

    @Override
    public void removeNode(Player player, String node, String world) {
        if(permissions == null)
            permissions = PermissionsEx.getPermissionManager();
        
        permissions.getUser(player).removePermission(node, world);
    }

    @Override
    public Boolean hasNode(Player player, String node, String world)
    {
        if(permissions == null)
            permissions = PermissionsEx.getPermissionManager();
        
        return permissions.getUser(player).has(node, world);
    }
    
    @Override
    public void reload()
    {
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "pex reload");
    }
    
}
