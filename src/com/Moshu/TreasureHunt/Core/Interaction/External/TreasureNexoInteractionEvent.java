package com.Moshu.TreasureHunt.Core.Interaction.External;

import com.Moshu.TreasureHunt.Core.Interaction.TreasureEvents;
import com.nexomc.nexo.api.events.custom_block.NexoBlockBreakEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TreasureNexoInteractionEvent implements Listener {

    @EventHandler
    public void onInteract(NexoFurnitureInteractEvent e)
    {
        Location loc = e.getBaseEntity().getLocation();
        TreasureEvents treasureEvents = TreasureEvents.getInstance();
        boolean isTreasure = treasureEvents.handleInteraction(e.getPlayer(), loc);
        e.setCancelled(isTreasure);
    }

    @EventHandler
    public void onInteract(NexoBlockBreakEvent e)
    {
        Location loc = e.getBlock().getLocation();
        TreasureEvents treasureEvents = TreasureEvents.getInstance();
        boolean isTreasure = treasureEvents.handleInteraction(e.getPlayer(), loc);
        e.setCancelled(isTreasure);
    }

}
