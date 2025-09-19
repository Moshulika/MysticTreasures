package com.Moshu.TreasureHunt.Core.Interaction.External;

import com.Moshu.TreasureHunt.Core.Interaction.TreasureEvents;
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TreasureItemsAdderInteractionEvent implements Listener {

    @EventHandler
    public void onInteract(FurnitureInteractEvent e)
    {

        Location loc = e.getBukkitEntity().getLocation();
        TreasureEvents treasureEvents = TreasureEvents.getInstance();
        boolean isTreasure = treasureEvents.handleInteraction(e.getPlayer(), loc);
        e.setCancelled(isTreasure);

    }

    @EventHandler
    public void onInteract(CustomBlockInteractEvent e)
    {
        Location loc = e.getBlockClicked().getLocation();
        TreasureEvents treasureEvents = TreasureEvents.getInstance();
        boolean isTreasure = treasureEvents.handleInteraction(e.getPlayer(), loc);
        e.setCancelled(isTreasure);
    }

}
