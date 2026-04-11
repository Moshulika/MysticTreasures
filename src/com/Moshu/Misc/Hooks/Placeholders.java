/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.Moshu.Misc.Hooks;

import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Core.Hunt;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@SuppressFBWarnings("HE_INHERITS_EQUALS_USE_HASHCODE")
public class Placeholders extends PlaceholderExpansion {

    @Override
    public boolean canRegister() {
        return true;
    }


    @Override
    public String getIdentifier() {
        return "mystictreasures";
    }

    @Override
    public boolean persist() {
        return true;
    }

    /*
     * The author of the Placeholder
     * This cannot be null
     */

    @Override
    public String getAuthor() {
        return "Moshu";
    }

    /*
     * Same with #getAuthor() but for version
     * This cannot be null
     */
    @Override
    public String getVersion() {
        return "1.0";
    }

    /**
     * TODO add the latest winner with damage given, and other stats.
     *
     * @param player
     * @param identifier
     * @return
     */
    @Override
    public String onRequest(OfflinePlayer player, String identifier) {

        //treasures found, mobs killed

        if (player == null) {
            return " ";
        }

        //If it's online
        if (player.getPlayer() != null) {

            Player p = player.getPlayer();
            Hunt h = Hunt.getNearestHunt(p.getLocation());

            if (h != null && h.isHuntActive()) {

                if (identifier.equalsIgnoreCase("x")) {
                    return Integer.toString(h.getLocation().getBlockX());
                }

                if (identifier.equalsIgnoreCase("z")) {
                    return Integer.toString(h.getLocation().getBlockZ());
                }

                if (identifier.equalsIgnoreCase("world")) {
                    return h.getLocation().getWorld().getName();
                }

                if (identifier.equalsIgnoreCase("active")) {
                    return "true";
                }

                if (identifier.equalsIgnoreCase("remaining")) {
                    return Utils.getCountDown(h.getRemainingTime());
                }

                if (identifier.equalsIgnoreCase("mobs")) {
                    return Integer.toString(h.getTreasure().remainingMobs());
                }

                if (identifier.equalsIgnoreCase("participants")) {
                    return Integer.toString(h.getTreasure().getParticipants().size());
                }

            } else {
                if (identifier.equalsIgnoreCase("x")) {
                    return "-";
                }

                if (identifier.equalsIgnoreCase("z")) {
                    return "-";
                }

                if (identifier.equalsIgnoreCase("world")) {
                    return p.getWorld().getName();
                }

                if (identifier.equalsIgnoreCase("active")) {
                    return "false";
                }

                if (identifier.equalsIgnoreCase("remaining")) {
                    return "Hunt inactive";
                }

                if (identifier.equalsIgnoreCase("mobs")) {
                    return "0";
                }

                if (identifier.equalsIgnoreCase("participants")) {
                    return "0";
                }

            }


        }

        return null;
    }

}

