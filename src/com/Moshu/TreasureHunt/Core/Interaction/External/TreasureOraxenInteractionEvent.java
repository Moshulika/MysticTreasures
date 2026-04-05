package com.Moshu.TreasureHunt.Core.Interaction.External;

import com.Moshu.TreasureHunt.Core.Interaction.TreasureEvents;
import io.th0rgal.oraxen.api.events.furniture.OraxenFurnitureInteractEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TreasureOraxenInteractionEvent implements Listener {

    @EventHandler
    public void onInteract(OraxenFurnitureInteractEvent e) {

        Location loc = e.getBaseEntity().getLocation();
        TreasureEvents treasureEvents = TreasureEvents.getInstance();
        boolean isTreasure = treasureEvents.handleInteraction(e.getPlayer(), loc);
        e.setCancelled(isTreasure);

    }

}
