package de.Chumper.ActivityPromotion;

import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author nplaschk
 */
class ActivityPromotionPlayerListener extends PlayerListener{
    
    public static ActivityPromotion plugin;
   
    
    public ActivityPromotionPlayerListener(ActivityPromotion instance) {
        plugin = instance;
    }
    
    @Override
    public void onPlayerAnimation(PlayerAnimationEvent event)
    {
        plugin.updatePlayer(event.getPlayer().getName());
        plugin.checkPromotion(event.getPlayer());
        plugin.checkReset();
    }
    @Override
    public void onPlayerMove(PlayerMoveEvent event)
    {
        plugin.updatePlayer(event.getPlayer().getName());
        plugin.checkPromotion(event.getPlayer());
        plugin.checkReset();
    }
    @Override
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        plugin.initiatePlayer(event.getPlayer());
        plugin.showAwayUser(event.getPlayer());
    }
    @Override
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        plugin.finishPlayer(event.getPlayer());
    }
    @Override
    public void onPlayerKick(PlayerKickEvent event)
    {
        plugin.finishPlayer(event.getPlayer());
    }
   
    
}
