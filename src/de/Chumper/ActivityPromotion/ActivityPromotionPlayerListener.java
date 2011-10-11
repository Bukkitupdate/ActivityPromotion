package de.Chumper.ActivityPromotion;

import java.util.Calendar;
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
        //Add a five (5) second time, to prevent the checking adter every tick
        //and 
        if(plugin.PLAYER.get(event.getPlayer().getName()).getTimeLastAction() + 2 > Calendar.getInstance().getTimeInMillis()/1000 )
        {
            return;
        }
        
        plugin.updatePlayer(event.getPlayer().getName());
        plugin.checkPromotion(event.getPlayer());
        plugin.checkReset();
    }
    @Override
    public void onPlayerMove(PlayerMoveEvent event)
    {
        //Add a five (5) second time, to prevent the checking adter every tick
        //and 
        if(plugin.PLAYER.get(event.getPlayer().getName()).getTimeLastAction() + 2 > Calendar.getInstance().getTimeInMillis()/1000 )
        {
            return;
        }
        
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
