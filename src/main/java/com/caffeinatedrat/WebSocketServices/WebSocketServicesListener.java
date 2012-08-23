/**
 * 
 */
package com.caffeinatedrat.WebSocketServices;

import java.text.MessageFormat;

import org.bukkit.event.Listener;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author CaffeinatedRat
 *
 */
public class WebSocketServicesListener implements Listener {
    private final WebSocketServices plugin;
    
    /*
     * This listener needs to know about the plugin which it came from
     */
    public WebSocketServicesListener(WebSocketServices plugin) {
        // Register the listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        this.plugin = plugin;
    }

    /*
     * Send the sample message to all players that join
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        
        String configVal = this.plugin.getConfig().getString("sample.message");
        if(configVal != null)
            event.getPlayer().sendMessage(configVal);
    }
    
    /*
     * Another example of a event handler. This one will give you the name of
     * the entity you interact with, if it is a Creature it will give you the
     * creature Id.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        final EntityType entityType = event.getRightClicked().getType();

        event.getPlayer().sendMessage(MessageFormat.format(
                "You interacted with a {0} it has an id of {1}",
                entityType.getName(),
                entityType.getTypeId()));
    }
}
