/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.Chumper.ActivityPromotion;

import org.bukkit.entity.Player;

/**
 *
 * @author Nils
 */
public interface Permission {
    
    public Boolean isInGroup(Player player, String group, String world);
    public void addGroup(Player player, String group, String world);
    public void removeGroup(Player player, String group, String world);
    public void addNode(Player player, String node, String world);
    public void removeNode(Player player, String node, String world);
    public Boolean hasNode(Player player, String node, String world);
    public void reload();
}
