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

package com.Moshu.TreasureHunt.Components;

import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Core.Treasure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.Plugin;

public class TreasureWaypoint {

    private final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private final boolean enabled;
    private final String color;
    private final int range;

    private ArmorStand stand;

    public TreasureWaypoint(boolean enabled, String color, int range) {
        this.enabled = enabled;
        this.color = color;
        this.range = range;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getColor() {
        return color;
    }

    public int getRange() {
        return range;
    }

    private ArmorStand getStand() {
        return stand;
    }

    public void remove() {
        if (!isEnabled()) return;
        if (getStand() == null) return;

        getStand().remove();
    }

    public void set(Treasure t) {

        if (!isEnabled()) return;
        if (getStand() != null) remove();

        Location treasureLocation = t.getLocation().clone();

        stand = treasureLocation.getWorld().spawn(treasureLocation, ArmorStand.class, as -> {

            as.setInvisible(true);
            as.setMarker(true);
            as.setInvulnerable(true);
            as.setGravity(false);

            if (Utils.isPaper()) {
                as.setPersistent(false);
            }

            as.setCustomName(t.getTreasureData().getIdentifier() + "-waypoint");
            as.setCustomNameVisible(false);
            as.addScoreboardTag("treasureWaypointStand");
        });

        try {

            AttributeInstance transmit = stand.getAttribute(Attribute.WAYPOINT_TRANSMIT_RANGE);

            plugin.getLogger().info("Setting waypoint with commands until further API..");

            if (transmit != null) {
                transmit.setBaseValue(getRange());
            }

            String uuid = stand.getUniqueId().toString();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("minecraft:waypoint modify %s color " + getColor(), uuid));

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set waypoint. If you're not on Spigot/Paper >= 1.21.6 disable the waypoint from treasure.yml: " + e.getMessage());
        }

    }


}

