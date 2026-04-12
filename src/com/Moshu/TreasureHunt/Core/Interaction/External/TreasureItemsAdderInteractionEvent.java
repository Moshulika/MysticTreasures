/*
 * Copyright 2026 Moshu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This software is also subject to the Commons Clause License Condition v1.0.
 * You may obtain a copy of the Commons Clause at:
 * https://commonsclause.com/
 */

package com.Moshu.TreasureHunt.Core.Interaction.External;

import com.Moshu.TreasureHunt.Core.Interaction.TreasureEvents;
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TreasureItemsAdderInteractionEvent implements Listener {

    @EventHandler
    public void onInteract(FurnitureInteractEvent e) {

        Location loc = e.getBukkitEntity().getLocation();
        TreasureEvents treasureEvents = TreasureEvents.getInstance();
        boolean isTreasure = treasureEvents.handleInteraction(e.getPlayer(), loc);
        e.setCancelled(isTreasure);

    }

    @EventHandler
    public void onInteract(CustomBlockInteractEvent e) {
        Location loc = e.getBlockClicked().getLocation();
        TreasureEvents treasureEvents = TreasureEvents.getInstance();
        boolean isTreasure = treasureEvents.handleInteraction(e.getPlayer(), loc);
        e.setCancelled(isTreasure);
    }

}

