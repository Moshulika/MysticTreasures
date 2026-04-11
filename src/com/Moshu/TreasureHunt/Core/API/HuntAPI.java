/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.Moshu.TreasureHunt.Core.API;

import com.Moshu.TreasureHunt.Components.TreasureData;
import com.Moshu.TreasureHunt.Components.TreasureKey;
import com.Moshu.TreasureHunt.Core.API.Exceptions.TreasureAlreadyRunningException;
import com.Moshu.TreasureHunt.Core.Hunt;
import com.Moshu.TreasureHunt.Core.Treasure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HuntAPI {

    private final static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    /**
     *
     * About TreasureData
     * TreasureData holds all the info from the treasure file
     * It represents only the info of the treasure, not the actual treasure itself
     * You can get info about TreasureKey, ItemRewards, CommandRewards, TreasureDebuff and TreasureKeepers from there
     * If you need info about the treasure itself, you can get it from the Treasure class which is instantiated by starting a Hunt.
     *
     * @return ArrayList of all the TreasureData (the data of all the treasure.yml files)
     */
    @NotNull
    public static ArrayList<TreasureData> getTreasureData() {
        return TreasureData.getTreasureData();
    }

    /**
     *
     * Async method that starts a treasure hunt at a random location.
     * Needs a valid treasure identifier or it will return null.
     *
     * @param treasureIdentifier
     * @return a future that completes when the hunt has started (a location has been found for the treasure).
     * @throws TreasureAlreadyRunningException if a treasure with the same identifier is already running.
     *                                         The location where the treasure will spawn depends on your treasure.yml and config.yml settings.
     *                                         The location is restricted within world boundaries, and cannot spawn in regions or unsafe places.
     */
    @Nullable
    public static CompletableFuture<Hunt> startTreasureHuntAtRandomLocation(@NotNull String treasureIdentifier) {
        if (!TreasureData.getTreasureIdentifiers().contains(treasureIdentifier)) {
            return CompletableFuture.completedFuture(null);
        }

        if (Hunt.isHuntActive(treasureIdentifier)) {
            throw new TreasureAlreadyRunningException("A treasure with the identifier: " + treasureIdentifier + " is already running");
        }

        Hunt h = new Hunt(treasureIdentifier, TreasureData.getByIdentifier(treasureIdentifier).getDuration());

        // Return a future that completes when the hunt has started (non-blocking)
        CompletableFuture<Hunt> started = new CompletableFuture<>();

        h.getLocationReadyFuture()
                .thenRun(() -> {

                    // Ensure start() runs on the main server thread
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            h.start();
                            started.complete(h);
                        } catch (Throwable t) {
                            started.completeExceptionally(t);
                        }
                    });
                })
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Failed to start treasure hunt with id '" + treasureIdentifier + "': " + ex.getMessage());
                    started.completeExceptionally(ex);
                    return null;
                });

        return started;
    }

    /**
     *
     * Starts a treasure hunt at a specific location.
     *
     * @param location           the location where the treasure will spawn -- attention:
     *                           this location still needs to be safe enough (inside world boundaries, not in regions, not on unsafe places)
     *                           or the hunt will fail and won't start.
     * @param treasureIdentifier the treasure identifier (needs a valid identifier)
     * @return a Hunt instance that already started
     */
    public static Hunt startTreasureHunt(@NotNull Location location, @NotNull String treasureIdentifier) {

        Hunt h = new Hunt(location, treasureIdentifier, TreasureData.getByIdentifier(treasureIdentifier).getDuration());
        h.start();
        return h;

    }

    /**
     * Hunts here are guaranteed to be active
     *
     * @return a list of all the hunts active
     */
    public static List<Hunt> getActiveHunts() {
        return Hunt.getHunts();
    }

    /**
     * Hunt IDs from here are guaranteed to be active
     *
     * @return a list of all the hunts' identifiers active
     */
    public static List<String> getActiveHuntsIdentifiers() {
        return Hunt.getActiveTreasureIdentifiers();
    }

    /**
     * Treasures here are guaranteed to be active
     *
     * @return a list of all the treasures active
     */
    public static List<Hunt> getActiveTreasures() {
        return Hunt.getActiveTreasures();
    }

    /**
     * Checks if a treasure is active at a location
     *
     * @param loc the location to check
     * @return true if a treasure is active at the location, false otherwise
     */
    public static boolean isHuntActive(Location loc) {
        return Hunt.isHuntActive(loc);
    }

    /**
     * Gets the nearest hunt at a location
     *
     * @param loc the location to check
     * @return the nearest hunt at the location, null if no hunt is active at the location
     */
    public static Hunt getNearestHunt(Location loc) {
        return Hunt.getNearestHunt(loc);
    }

    /**
     * Checks if a location is a treasure
     *
     * @param loc the location to check
     * @return true if the location is a treasure, false otherwise
     */
    public static boolean isTreasure(Location loc) {
        return Treasure.isTreasure(loc);
    }

    /**
     * Gets the treasure at a location
     *
     * @param loc the location to check
     * @return the treasure at the location, null if no treasure is at the location
     */
    public static Treasure getTreasure(Location loc) {
        return Treasure.getTreasure(loc);
    }

    /**
     * Checks if a player is near a treasure
     *
     * @param p the player to check
     * @return true if the player is near a treasure, false otherwise
     */
    public static boolean isNearTreasure(Player p) {
        return Treasure.isNearTreasure(p);
    }

    /**
     * Checks if an item is a treasure key
     *
     * @param itemStack the item to check
     * @return true if the item is a treasure key, false otherwise
     */
    public static boolean isTreasureKey(ItemStack itemStack) {
        return TreasureKey.isKey(itemStack);
    }

    /**
     * Checks if a hunt is active with a specific identifier
     *
     * @param id the identifier to check
     * @return true if the hunt is active with the identifier, false otherwise
     */
    public static boolean isHuntActive(String id) {
        return Hunt.isHuntActive(id);
    }


}

