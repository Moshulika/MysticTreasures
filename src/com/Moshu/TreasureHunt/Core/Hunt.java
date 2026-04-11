/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.Moshu.TreasureHunt.Core;

import com.Moshu.Misc.Locations;
import com.Moshu.Misc.Storage.Messages;
import com.Moshu.Misc.Storage.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Components.TreasureData;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents an active treasure hunt instance.
 * This class manages the lifecycle of a treasure hunt, from creation to completion,
 * including location generation, treasure spawning, and hunt state management.
 * <p>
 * Each Hunt instance corresponds to a single treasure hunt event and contains
 * all the necessary data and logic to manage that specific hunt.
 *
 * @author Moshu
 * @version 1.0
 */
public class Hunt {

    private Treasure treasure;
    private Location l;
    private final int duration;
    private long startTime = 0;
    private final String treasureTypeString;
    private TreasureData treasureData;

    private static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("MysticTreasures");
    }

    private static final HashMap<UUID, Hunt> activeHunts = new HashMap<>();
    private static final List<Hunt> activeTreasuresCache = Collections.synchronizedList(new ArrayList<>());
    private final UUID huntId;

    // Future that completes when the location is ready (non-blocking)
    private final CompletableFuture<Location> locationReady = new CompletableFuture<>();

    /**
     * Gets called on removal of treasure
     */
    public void setInactive() {
        activeHunts.remove(huntId);
        activeTreasuresCache.remove(this);
    }

    /**
     * The hunt is active but the treasure is not yet generated and active
     */
    private void setActive() {
        activeHunts.put(huntId, this);
    }

    public void setTreasureActive() {
        if (!activeTreasuresCache.contains(this)) {
            activeTreasuresCache.add(this);
        }
    }

    public UUID getHuntId() {
        return huntId;
    }

    /**
     * Retrieves an active hunt by its unique identifier.
     * This method searches through all currently active hunts and returns the one matching
     * the provided identifier.
     *
     * @param id the unique identifier of the hunt to retrieve
     * @return the Hunt instance with the matching identifier, or null if no active hunt is found
     */

    public static Hunt getHuntById(UUID id) {
        return activeHunts.get(id);
    }

    /**
     * Retrieves an active hunt by its treasure identifier.
     * If multiple hunts of the same type are active, returns the first one found.
     *
     * @param id the treasure identifier
     * @return the Hunt instance, or null if not found
     */
    public static Hunt getHuntByIdentifier(String id) {
        for (Hunt h : activeHunts.values()) {
            if (h.getTreasureData().getIdentifier().equals(id)) return h;
        }
        return null;
    }

    /**
     * Checks if there is any active hunt in the specified world.
     * This method iterates through all active hunts to determine if any are located
     * within the given world boundaries.
     *
     * @param w the world to check for active hunts
     * @return true if at least one hunt is active in the world, false otherwise
     */

    public static boolean huntActiveInWorld(World w) {
        return !getHuntsInWorld(w).isEmpty();
    }

    /**
     * Retrieves all hunts currently active in the specified world.
     * This method filters all active hunts and returns only those that are located
     * within the boundaries of the given world.
     *
     * @param w the world to search for hunts
     * @return an ArrayList containing all hunts active in the specified world
     */

    public static ArrayList<Hunt> getHuntsInWorld(World w) {

        ArrayList<Hunt> hunts = new ArrayList<>();

        for (Hunt h : activeHunts.values()) {

            if (h.getTreasureData().getWorld().getName().equals(w.getName())) hunts.add(h);

        }

        return hunts;

    }

    /**
     * Creates a new hunt at a random location with the specified treasure type and duration.
     * The location will be determined asynchronously based on the treasure configuration
     * and world boundaries. This constructor initializes the hunt but does not start it.
     *
     * @param treasureTypeString the identifier of the treasure type to spawn
     * @param duration           the duration in minutes for how long the hunt should remain active
     */

    public Hunt(String treasureTypeString, int duration) {

        this.huntId = UUID.randomUUID();
        this.treasureTypeString = treasureTypeString;
        this.duration = duration;

        deserializeTreasureData();

        if (getTreasureData().spawnToCertainCoords()) {


            List<Location> locations = getTreasureData().getSpawnCoords();

            if (!locations.isEmpty()) {
                this.l = locations.get(Utils.randInt(0, locations.size() - 1));
            } else {
                World w = Bukkit.getServer().getWorlds().get(0);
                this.l = new Location(w, w.getSpawnLocation().getX(), w.getSpawnLocation().getY(), w.getSpawnLocation().getZ());
            }
            // Complete immediately when location is known
            locationReady.complete(this.l);


        } else {

            World w = getTreasureData().getWorld();
            double distance = Math.min(Locations.getBorder(w) - 10, getTreasureData().getMaxTreasureDistance());
            int maxTreasureDistance = getTreasureData().getMaxTreasureDistance();

            // Compute asynchronously; do NOT block or join
            CompletableFuture
                    .supplyAsync(() -> Locations.getRandomLocationMoreThan(w, maxTreasureDistance, distance, distance))
                    .thenAccept(loc -> {
                        this.l = loc;
                        locationReady.complete(loc);
                    })
                    .exceptionally(ex -> {
                        getPlugin().getLogger().severe("Failed to compute random treasure location: " + ex.getMessage());
                        locationReady.completeExceptionally(ex);
                        return null;
                    });
        }
    }

    /**
     * Creates a new hunt at a specific location with the specified treasure type and duration.
     * This constructor immediately sets the hunt location and initializes the hunt,
     * but does not start it. The location must be valid and safe for treasure spawning.
     *
     * @param location           the specific location where the treasure should spawn
     * @param treasureTypeString the identifier of the treasure type to spawn
     * @param duration           the duration in minutes for how long the hunt should remain active
     */

    public Hunt(Location location, String treasureTypeString, int duration) {
        this.huntId = UUID.randomUUID();
        this.treasureTypeString = treasureTypeString;
        this.duration = duration;
        this.l = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());

        deserializeTreasureData();
        // Location already known
        locationReady.complete(this.l);

    }

    /**
     * Starts the hunt by spawning the treasure at the designated location.
     * This method activates the hunt, spawns the treasure with all its components
     * (particles, keepers, effects), broadcasts messages to players, and starts
     * the hunt timer. The hunt must have a valid location before calling this method.
     */
    public void startOnLocationFound() {

        CompletableFuture<Hunt> started = new CompletableFuture<>();
        getLocationReadyFuture().thenRun(() -> {

                    Bukkit.getScheduler().runTask(getPlugin(), () -> {
                        try {
                            start();
                            started.complete(this);
                        } catch (Throwable t) {
                            started.completeExceptionally(t);
                        }
                    });
                })
                .exceptionally(ex -> {
                    getPlugin().getLogger().severe("Failed to start treasure hunt with id '" + getTreasureData().getIdentifier() + "': " + ex.getMessage());
                    started.completeExceptionally(ex);
                    return null;
                });

    }

    /**
     * Starts the hunt by spawning the treasure at the designated location.
     * This method activates the hunt, spawns the treasure with all its components
     * (particles, keepers, effects), broadcasts messages to players, and starts
     * the hunt timer. The hunt must have a valid location before calling this method.
     */
    public void start() {

        if (this.l == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&5&lMystic&d&lTreasures: &fLocation is null, something went wrong."));
            return;
        }

        treasure = new Treasure(this, getTreasureData());

        if (!Locations.isSafeEnough(this.l)) {
            getPlugin().getLogger().severe("Spawn location for treasure `" + getTreasureData().getTreasureName() + "` is unsafe. Modify your location!");
            return;
        }

        this.startTime = System.currentTimeMillis();
        setActive();
        treasure.create();

        Bukkit.getConsoleSender().sendMessage(Messages.get("treasure-generated-confirmation")
                .replace("{treasure}", getTreasureData().getTreasureName())
                .replace("{x}", getLocation().getBlockX() + "")
                .replace("{z}", getLocation().getBlockZ() + "")
                .replace("{alias}", getTreasureData().getTreasureName())
                .replace("{world}", getLocation().getWorld().getName()));


    }

    /**
     * Deserializes and loads treasure configuration data from the treasure files.
     * This private method reads the treasure configuration based on the treasureTypeString
     * and populates the treasureData field with all necessary information including
     * rewards, keepers, effects, and spawn settings.
     */

    private void deserializeTreasureData() {
        this.treasureData = TreasureData.getByIdentifier(treasureTypeString);
    }

    /**
     * Returns the treasure configuration data associated with this hunt.
     * The TreasureData contains all information from the treasure.yml file including
     * rewards, spawn settings, particle effects, and keeper configurations.
     *
     * @return the TreasureData object containing all treasure configuration
     */

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TreasureData getTreasureData() {
        return treasureData;
    }

    /**
     * Retrieves all hunts that have active treasures spawned in the world.
     * This method returns a cached list of active treasures to optimize performance.
     *
     * @return a List of Hunt instances that have active treasures
     */

    @SuppressFBWarnings("MS_EXPOSE_REP")
    public static List<Hunt> getActiveTreasures() {
        return activeTreasuresCache;
    }

    /**
     * Hunt here are guaranteed to be active
     *
     * @return a list of all the hunts active
     */
    public static List<Hunt> getHunts() {
        List<Hunt> h = Collections.synchronizedList(new ArrayList<Hunt>());
        h.addAll(activeHunts.values());
        return h;
    }

    /**
     * Hunt here are guaranteed to be active
     *
     * @return a list of all the hunts active
     */
    public static List<UUID> getHuntsIds() {
        List<UUID> h = Collections.synchronizedList(new ArrayList<>());
        h.addAll(activeHunts.keySet());
        return h;
    }


    /**
     * Retrieves the identifiers of all active treasure hunts.
     * This method returns the string identifiers for all hunts that have active
     * treasures currently spawned in the world, useful for administrative purposes
     * and API access.
     *
     * @return a List of String identifiers for all hunts with active treasures
     */

    public static List<String> getActiveTreasureIdentifiers() {

        List<String> h = Collections.synchronizedList(new ArrayList<>());

        for (Hunt x : activeTreasuresCache) {
            h.add(x.getTreasureData().getIdentifier());
        }

        return h;
    }

    /**
     * Finds the hunt closest to the specified location.
     * This method calculates distances between the given location and all active
     * hunt locations, returning the hunt with the minimum distance.
     *
     * @param loc the location to measure distances from
     * @return the Hunt instance closest to the specified location, or null if no hunts are active
     */

    public static Hunt getNearestHunt(Location loc) {

        Location huntLoc;

        double minDistanceSquared = Double.MAX_VALUE;
        Hunt closestHunt = null;
        Hunt backupHunt = null;

        for (Hunt h : activeTreasuresCache) {
            huntLoc = h.getLocation();

            if (huntLoc == null || huntLoc.getWorld() == null) continue;

            if (!huntLoc.getWorld().getName().equals(loc.getWorld().getName())) {
                backupHunt = h;
                continue;
            }

            double distSquared = huntLoc.distanceSquared(loc);
            if (distSquared < minDistanceSquared) {
                minDistanceSquared = distSquared;
                closestHunt = h;
            }

        }

        if (closestHunt == null && Settings.getBoolean("broadcast-to-all-worlds")) {
            return backupHunt;
        }

        return closestHunt;

    }

    /**
     * Stops and deactivates the hunt, cleaning up all associated resources.
     * This method removes the treasure from the world, despawns any associated
     * keepers, stops particle effects, removes the hunt from active collections,
     * and performs all necessary cleanup operations.
     */

    public void stop() {

        if (getTreasure() == null) return;
        if (!getTreasure().isActive()) return;

        Bukkit.getConsoleSender().sendMessage(Messages.get("treasure-stopped-confirmation")
                .replace("{x}", getLocation().getBlockX() + "")
                .replace("{z}", getLocation().getBlockZ() + "")
                .replace("{alias}", getTreasureData().getTreasureName())
                .replace("{world}", getLocation().getWorld().getName()));

        getTreasure().remove(true);
    }

    /**
     * Calculates the remaining time before the hunt expires.
     * This method computes the time left by subtracting the elapsed time from
     * the total duration, returning the result in milliseconds.
     *
     * @return the remaining time in milliseconds before the hunt expires, or 0 if expired
     */

    public long getRemainingTime() {
        return getStartTime() + TimeUnit.MINUTES.toMillis(getDuration()) - System.currentTimeMillis();
    }

    /**
     * Checks if there is an active hunt at the specified location.
     * This method determines if any hunt has a treasure spawned at or very close
     * to the given location coordinates.
     *
     * @param loc the location to check for active hunts
     * @return true if a hunt is active at the location, false otherwise
     */

    public static boolean isHuntActive(Location loc) {

        Location treasureLoc;

        for (Hunt h : activeTreasuresCache) {

            treasureLoc = h.getLocation();

            if (treasureLoc.getWorld().getName().equals(loc.getWorld().getName()) &&
                    treasureLoc.getBlockX() == loc.getBlockX() && treasureLoc.getBlockY() == loc.getBlockY() &&
                    treasureLoc.getBlockZ() == loc.getBlockZ()) return true;

        }

        return false;

    }

    /**
     * Checks if this specific hunt instance is currently active.
     * This method verifies that the hunt is in the active hunts collection
     * and has not been stopped or expired.
     *
     * @return true if this hunt is currently active, false otherwise
     */

    public boolean isHuntActive() {
        return treasure != null && treasure.isActive();
    }

    /**
     * Checks if a hunt with the specified identifier is currently active.
     * This method searches the active hunts collection for a hunt matching
     * the given identifier string.
     *
     * @param id the identifier to check for activity
     * @return true if a hunt with the identifier is active, false otherwise
     */

    public static boolean isHuntActive(String id) {
        for (Hunt h : activeHunts.values()) {
            if (h.getTreasureData().getIdentifier().equals(id)) return true;
        }
        return false;
    }

    /**
     * Returns a string representation of whether a hunt with the given identifier is active.
     * This method provides a user-friendly string response indicating the active
     * status of a hunt, useful for command responses and administrative interfaces.
     *
     * @param id the identifier to check for activity status
     * @return a String indicating whether the hunt is active or not
     */

    public static String isActiveString(String id) {

        if (isHuntActive(id)) return Messages.get("menu-yes");
        return Messages.get("menu-no");

    }

    /**
     * Returns the location where this hunt's treasure is or will be spawned.
     * For hunts created with random locations, this may return null until
     * a suitable location is found asynchronously.
     *
     * @return the Location where the treasure is positioned, or null if not yet determined
     */

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Location getLocation() {
        return l;
    }

    /**
     * Provides a CompletableFuture that completes when the hunt location is ready.
     * This is particularly useful for hunts created with random locations, allowing
     * callers to react asynchronously when a suitable spawn location is found
     * without blocking the main thread.
     *
     * @return a CompletableFuture<Location> that completes when the location is determined
     */

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public CompletableFuture<Location> getLocationReadyFuture() {
        return locationReady;
    }

    /**
     * Returns the treasure instance associated with this hunt.
     * The treasure object contains the physical representation in the world,
     * including blocks, entities, particles, and interaction handlers.
     *
     * @return the Treasure instance spawned by this hunt, or null if not yet spawned
     */

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Treasure getTreasure() {
        return treasure;
    }

    /**
     * Returns the configured duration for this hunt in minutes.
     * This represents the total time the hunt should remain active before
     * automatically expiring and cleaning up.
     *
     * @return the duration in minutes for this hunt
     */

    public int getDuration() {
        return duration;
    }

    /**
     * Calculates the elapsed time since the hunt started.
     * This method computes the time that has passed since the hunt's startTime
     * was set, returning the result in milliseconds.
     *
     * @return the elapsed time in milliseconds since the hunt started
     */

    public long getElapsedTime() {
        return getStartTime() - getRemainingTime();
    }

    /**
     * Returns the timestamp when this hunt was started.
     * This value is set when the start() method is called and represents
     * the time in milliseconds since epoch when the hunt became active.
     *
     * @return the start time in milliseconds since epoch, or 0 if not yet started
     */

    public long getStartTime() {
        return startTime;
    }

    /**
     * Generates formatted lore text for menu items based on hunt information.
     * This private utility method creates user-friendly descriptions for GUI
     * menu items, incorporating hunt details like location, time remaining,
     * and treasure information.
     *
     * @param list the base lore strings to enhance with hunt information
     * @param h    the hunt instance to extract information from
     * @return an ArrayList of formatted lore strings for display in menus
     */

    private static ArrayList<String> menuItemLore(List<String> list, Hunt h) {

        ArrayList<String> newList = new ArrayList<>();

        for (String s : list) {
            newList.add(s.replace("{time}", Utils.getCountDown(h.getRemainingTime()))
                    .replace("{keepers}", "" + h.getTreasure().getRemainingMobs().size())
                    .replace("{x}", "" + h.getLocation().getBlockX())
                    .replace("{y}", "" + h.getLocation().getBlockY())
                    .replace("{world}", h.getLocation().getWorld().getName())
                    .replace("{participants}", "" + h.getTreasure().getParticipants().size())
                    .replace("{key}", "" + h.getTreasureData().getTreasureKey().requiresKey()));
        }

        return newList;
    }

    /**
     * Opens a GUI menu showing all currently active hunts for the specified player.
     * This method creates and displays an interactive menu interface where players
     * can view information about all active treasure hunts, including locations,
     * remaining time, and treasure details.
     *
     * @param p the player to show the active hunts menu to
     */

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public static void activeHuntsMenu(Player p) {

        Inventory inv = Bukkit.createInventory(null, 27, Messages.get("active-hunts-menu.title"));
        ItemMeta meta;

        String name = Utils.format(Messages.get("active-hunts-menu.name"));

        int i = 0;
        for (Hunt h : activeTreasuresCache) {

            ItemStack item = new ItemStack(Utils.checkMaterial(h.getTreasureData().getMenuItem()));
            meta = item.getItemMeta();
            if (meta == null) continue;

            meta.setDisplayName(name.replace("{treasure_name}", h.getTreasure().getTreasureData().getTreasureName()));
            meta.setLore(menuItemLore(Messages.getAndFormatList("messages.active-hunts-menu.lore"), h));
            item.setItemMeta(meta);

            inv.setItem(i, item);
            i++;
        }


        Utils.fillWithGlass(inv);
        p.openInventory(inv);

    }

}

