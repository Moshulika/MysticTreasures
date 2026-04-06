package com.Moshu.TreasureHunt.Core;

import com.Moshu.Misc.Cooldown;
import com.Moshu.Misc.Effects;
import com.Moshu.Misc.Hooks.DiscordWebhook;
import com.Moshu.Misc.SendCenteredMessage;
import com.Moshu.Misc.Storage.Messages;
import com.Moshu.Misc.Storage.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeper;
import com.Moshu.TreasureHunt.Components.Rewards.CommandReward;
import com.Moshu.TreasureHunt.Components.Rewards.ItemReward;
import com.Moshu.TreasureHunt.Components.RoundData;
import com.Moshu.TreasureHunt.Components.TreasureData;
import com.Moshu.TreasureHunt.Components.TreasureRound;
import com.Moshu.TreasureHunt.Components.TreasureRoundController;
import com.Moshu.TreasureHunt.Core.API.Events.TreasureSpawnEvent;
import com.Moshu.TreasureHunt.Handlers.HologramHandler;
import com.Moshu.TreasureHunt.TreasureTask;
import com.google.common.base.Joiner;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomEntity;
import dev.lone.itemsadder.api.CustomFurniture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenFurniture;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents a treasure instance in the world.
 * This class manages the physical treasure entity, including spawning, interaction,
 * reward distribution, and treasure keeper management.
 * <p>
 * Each Treasure instance is associated with a Hunt and contains all the logic
 * for treasure behavior, player interactions, and reward systems.
 *
 * @author Moshu
 * @version 1.0
 */
public class Treasure {

    private Location l;
    private final Hunt h;
    private boolean isActive;
    private Entity furnitureEntity;
    private final HashMap<UUID, Double> playerDamage = new HashMap<>();
    private boolean isLocked = true;
    private Inventory rewardInventory;
    private final ArrayList<Player> receivedCommandRewards = new ArrayList<>();
    private int currentClicks = 0;
    private boolean secondWaveActivated = false;
    private boolean firstOpen = true;

    public boolean isFirstOpen() {
        return firstOpen;
    }

    public void setFirstOpen(boolean firstOpen) {
        this.firstOpen = firstOpen;
    }

    private int currentRound = 0;

    private final TreasureData treasureData;

    private boolean spawned = false;

    private static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("MysticTreasures");
    }

    private final TreasureRoundController roundController;

    public TreasureRoundController getRoundController() {
        return roundController;
    }

    ArrayList<Entity> spawnedTreasureKeepers = new ArrayList<>();

    private static final Particle EXPLOSION = Settings.getCompatParticle("treasure-spawn-particle");
    private static final Particle EXPLOSION_EMITTER = Settings.getCompatParticle("treasure-remove-particle");
    private static final Particle CAMPFIRE_SIGNAL_SMOKE = Settings.getCompatParticle("treasure-fall-particle");

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<Entity> getSpawnedTreasureKeepers() {
        return this.spawnedTreasureKeepers;
    }

    public enum AwardMethod {
        LEGACY,
        CHEST,
        ALL_PLAYERS,
        HIGHEST_DAMAGE,
        DROP_ON_GROUND,
        TOP_X;

        /**
         * Parse an AwardMethod from a string (case-insensitive).
         * Falls back to CHEST if the value is invalid or null.
         *
         * @param value the string value from config
         * @return parsed AwardMethod (never null)
         */
        public static AwardMethod fromString(String value) {

            if (value == null) {
                return CHEST;
            }

            try {

                if (value.startsWith("TOP_")) return TOP_X;

                return AwardMethod.valueOf(value.trim().toUpperCase());

            } catch (IllegalArgumentException e) {

                getPlugin().getLogger().warning("The award-method of the treasure is invalid, defaulting to CHEST");
                return CHEST;
            }
        }

    }

    /**
     * Constructor for creating a new treasure instance.
     * Initializes the treasure with the associated hunt and treasure data configuration.
     * Sets up the location, reward inventory, and prepares the treasure for spawning.
     *
     * @param h the Hunt instance that owns this treasure
     * @param d the TreasureData configuration containing all treasure settings
     */

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public Treasure(Hunt h, TreasureData d) {
        this.h = h;
        this.l = h.getLocation() != null ? h.getLocation().clone() : null;
        this.treasureData = d;
        isLocked = d.getTreasureKey().requiresKey();
        this.isActive = false;

        this.roundController = new TreasureRoundController(this, treasureData.getRoundRegistry());

        setupInventory();

    }

    /**
     * Sets up the reward inventory for the treasure chest.
     * This private method initializes the inventory with all configured item rewards
     * from the treasure configuration, preparing them for distribution when the
     * treasure is claimed.
     */

    public void giveRewards(RoundData roundData) {
        if (roundData == null) return;
        
        Treasure.AwardMethod method = roundData.getAwardMethod();
        if (method == null) method = Treasure.AwardMethod.CHEST;
        
        switch (method) {
            case ALL_PLAYERS:
                awardPrizes(roundData);
                break;
            case HIGHEST_DAMAGE:
                if (wereTreasureKeepersDamaged()) {
                    awardPrize(getPlayerWithMostDamage(), roundData);
                } else {
                    // Fallback if no damage recorded
                    awardPrizes(roundData);
                }
                break;
            case TOP_X:
                awardPrizesToTop(roundData.getRewardTopX(), roundData);
                break;
            case DROP_ON_GROUND:
                awardPrize(null, roundData); // Passing null will drop on ground
                break;
            case CHEST:
            default:
                setupInventory();
                break;
        }
    }

    public void awardPrizes(RoundData roundData) {
        launchFireworks();
        int cooldown = Settings.getCooldown();
        
        for (Player p : getParticipants()) {
            if (Cooldown.hasCooldown(p.getUniqueId(), "treasure-winner")) {
                p.sendMessage(Messages.get("winner-cooldown").replace("{time}", Utils.formatRemainingTime(Cooldown.getRemainingTimeMinutes(p.getUniqueId(), "treasure-winner"))));
                continue;
            }

            if (cooldown != 0) {
                new Cooldown(p.getUniqueId(), "treasure-winner", cooldown * 60).set();
            }

            runCommandPrizes(p, roundData);
            giveItemRewards(p, roundData, false);
        }
        announceWinners();
    }

    public void awardPrizesToTop(int topPlayers, RoundData roundData) {
        launchFireworks();
        int cooldown = Settings.getCooldown();
        int topCounter = 0;

        for (UUID u : getSortedPlayersByDamage()) {
            if (topCounter >= topPlayers) break;
            Player p = Bukkit.getPlayer(u);
            if (p == null) continue;

            if (Cooldown.hasCooldown(p.getUniqueId(), "treasure-winner")) continue;

            if (cooldown != 0) {
                new Cooldown(p.getUniqueId(), "treasure-winner", cooldown * 60).set();
            }

            runCommandPrizes(p, roundData);
            giveItemRewards(p, roundData, false);
            topCounter++;
        }
        announceWinners();
    }

    public void awardPrize(Player p, RoundData roundData) {
        launchFireworks();
        int cooldown = Settings.getCooldown();

        if (p != null) {
            if (cooldown != 0) {
                new Cooldown(p.getUniqueId(), "treasure-winner", cooldown * 60).set();
            }
            runCommandPrizes(p, roundData);
            giveItemRewards(p, roundData, roundData.getAwardMethod() == Treasure.AwardMethod.DROP_ON_GROUND);
            announceWinner(p);
        } else if (roundData.getAwardMethod() == Treasure.AwardMethod.DROP_ON_GROUND) {
            // Drop for everyone or generic drop
            for (ItemReward r : roundData.getItemRewards()) {
                if (Utils.chance() > r.getChance()) continue;
                dropItemOnGround(r);
            }
        }
    }

    private void giveItemRewards(Player p, RoundData roundData, boolean dropOnGround) {
        for (ItemReward r : roundData.getItemRewards()) {
            if (Utils.chance() > r.getChance()) continue;

            if (dropOnGround) {
                dropItemOnGround(r);
            } else {
                ItemStack item = r.getItemStack();
                if (item == null || item.getType() == Material.AIR) continue;

                if (!Utils.hasFullInventory(p)) {
                    p.getInventory().addItem(item);
                } else {
                    p.getWorld().dropItemNaturally(p.getLocation(), item);
                    p.sendMessage(Messages.get("full-inventory").replace("{amount}", item.getAmount() + "").replace("{item}", Utils.setCapitals(item.getType().toString().toLowerCase().replace("_", " "))));
                }
            }
        }
    }

    private void dropItemOnGround(ItemReward r) {
        World w = getLocation().getWorld();
        int xOffset = (int) ((Math.random() * 10) - 5);
        int zOffset = (int) ((Math.random() * 10) - 5);
        Location dropLoc = Utils.getHighestBlock(w, getLocation().getBlockX() + xOffset, getLocation().getBlockZ() + zOffset, getLocation());

        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
            Item i = w.dropItemNaturally(dropLoc, r.getItemStack());
            i.setGlowing(true);
            i.setInvulnerable(true);
            i.setCustomNameVisible(true);
            i.setCustomName(Utils.format(getTreasureData().getDroppedItemName()));
        }, 20L);
    }

    public void runCommandPrizes(Player p, RoundData roundData) {
        for (CommandReward c : roundData.getCommandRewards()) {
            if (Utils.chance() > c.getChance()) continue;
            c.run(p);
        }
        if (!receivedCommandRewards.contains(p)) {
            receivedCommandRewards.add(p);
        }
    }

    public void setupInventory() {

        String title = Messages.get("treasure-reward-menu-title");
        rewardInventory = Bukkit.createInventory(null, 54, title != null ? title : "Treasure Rewards");

        List<ItemReward> allPossibleRewards = new ArrayList<>();
        for(TreasureRound round : treasureData.getRoundRegistry().getRounds()) {
            allPossibleRewards.addAll(round.getRoundData().getItemRewards());
        }

        if (allPossibleRewards.isEmpty() && getTreasureData().canOpenChest()) {
             // If NO rewards are configured at all, we might have an issue if the user expects a loot chest
             return;
        }

        for (ItemReward i : allPossibleRewards) {
            if (Utils.chance() > i.getChance()) continue;
            ItemStack is = i.getItemStack();
            if (is == null || is.getType() == Material.AIR) continue;

            int slot = Utils.randInt(0, 53);
            if (rewardInventory.getItem(slot) != null && rewardInventory.getItem(slot).getType() != Material.AIR) {
                slot = rewardInventory.firstEmpty();
                if (slot == -1) break;
            }
            rewardInventory.setItem(slot, is);
        }

        // If after rolling chances we have an empty inventory, but chest is required,
        // force at least one reward if possible to prevent tickTreasure from removing it
        if (rewardInventory.isEmpty() && getTreasureData().canOpenChest() && !allPossibleRewards.isEmpty()) {
            ItemReward guaranteed = allPossibleRewards.get(Utils.randInt(0, allPossibleRewards.size() - 1));
            rewardInventory.setItem(Utils.randInt(0, 53), guaranteed.getItemStack());
        }
    }


    /**
     * Checks if the treasure has already applied debuff effects.
     * This method determines if debuff effects (such as negative potion effects)
     * have been applied to players, preventing duplicate applications.
     *
     * @return true if debuff effects have already been applied, false otherwise
     */

    public boolean alreadyDebuffed() {
        return secondWaveActivated;
    }

    /**
     * Returns the current number of clicks the treasure has received.
     * This tracks player interactions with the treasure, which may be required
     * to reach a certain threshold before the treasure can be opened.
     *
     * @return the current click count on this treasure
     */

    public int getCurrentClicks() {
        return currentClicks;
    }

    /**
     * Increments the click counter for this treasure.
     * This method is called each time a player successfully interacts with
     * the treasure, tracking progress toward any required click threshold.
     */

    public void incrementCurrentClicks() {
        currentClicks++;
    }

    /**
     * Checks if the specified player has already received command rewards.
     * This method prevents duplicate execution of command rewards for the same
     * player during a single treasure hunt session.
     *
     * @param p the player to check for received rewards
     * @return true if the player has already received command rewards, false otherwise
     */

    public boolean receivedCommandRewards(Player p) {
        return receivedCommandRewards.contains(p);
    }

    /**
     * Returns a list of player UUIDs sorted by damage dealt to treasure keepers.
     * This method sorts all participants by their damage contribution in descending order,
     * useful for determining rewards based on participation level.
     *
     * @return a List of UUIDs ordered by damage dealt (highest first)
     */

    public List<UUID> getSortedPlayersByDamage() {

        List<Map.Entry<UUID, Double>> entries = new ArrayList<>(playerDamage.entrySet());
        entries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        List<UUID> sortedUUIDs = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : entries) {
            sortedUUIDs.add(entry.getKey());
        }

        return sortedUUIDs;
    }

    /**
     * Returns the list of players who have already received command rewards.
     * This method provides access to the collection of players who have been
     * awarded command-based rewards to prevent duplicate distributions.
     *
     * @return a List of Players who have received command rewards
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<Player> getReceivedCommandRewards() {
        return receivedCommandRewards;
    }

    /**
     * Returns the inventory containing the treasure's item rewards.
     * This inventory is populated during treasure setup and contains all
     * configured item rewards that will be distributed to successful players.
     *
     * @return the Inventory containing item rewards for this treasure
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Inventory getRewardInventory() {
        return rewardInventory;
    }

    /**
     * Returns the treasure configuration data for this treasure.
     * The TreasureData contains all settings from the treasure.yml file including
     * spawn behavior, rewards, effects, and keeper configurations.
     *
     * @return the TreasureData configuration object for this treasure
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TreasureData getTreasureData() {
        return treasureData;
    }

    private final ArrayList<Player> participants = new ArrayList<>();

    /**
     * Returns the location where this treasure is positioned in the world.
     * This location represents the exact coordinates where the treasure
     * block or entity has been placed.
     *
     * @return the Location of this treasure in the world
     */
    public Location getLocation() {
        return l;
    }

    /**
     * Checks if the treasure keeper mobs have been spawned.
     * This method indicates whether the guardian creatures protecting the
     * treasure have been generated and are active in the world.
     *
     * @return true if treasure keeper mobs have spawned, false otherwise
     */
    public boolean haveTheMobsSpawned() {
        return spawned;
    }

    /**
     * Checks if this treasure is currently active in the world.
     * An active treasure has been spawned, is visible to players, and can
     * be interacted with. Inactive treasures have been removed or never spawned.
     *
     * @return true if the treasure is active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Gets the total damage dealt by a specific player to treasure keepers.
     * This method tracks individual player contributions during combat phases,
     * which may be used for reward distribution calculations.
     *
     * @param p the player whose damage total to retrieve
     * @return the total damage dealt by the player as a double value
     */
    public double getDamageGiven(Player p) {
        return playerDamage.getOrDefault(p.getUniqueId(), 0.0);
    }

    /**
     * Records damage dealt by a player to treasure keepers.
     * This method accumulates damage values for tracking player participation
     * and contribution levels during treasure keeper combat.
     *
     * @param p      the player who dealt the damage
     * @param damage the amount of damage dealt
     */
    public void addDamageGiven(Player p, double damage) {
        playerDamage.put(p.getUniqueId(), getDamageGiven(p) + damage);
    }

    /**
     * Checks if any treasure keepers have taken damage from players.
     * This method determines if combat has begun with the treasure guardians,
     * which may trigger various game mechanics or unlock conditions.
     *
     * @return true if treasure keepers have been damaged, false otherwise
     */
    public boolean wereTreasureKeepersDamaged() {
        return !playerDamage.isEmpty();
    }

    /**
     * Gets the Xth highest damage dealer and their damage amount.
     * This method returns a map entry containing the UUID of the player
     * and their damage total for a specific ranking position.
     *
     * @param x the ranking position to retrieve (1 for highest, 2 for second, etc.)
     * @return a Map.Entry containing the player UUID and damage amount, or null if not found
     */
    public Map.Entry<UUID, Double> getXthMostDamage(int x) {

        if (playerDamage == null || playerDamage.size() < x || x <= 0) {
            return null;
        }

        List<Map.Entry<UUID, Double>> sortedEntries = new ArrayList<>(playerDamage.entrySet());
        sortedEntries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        return sortedEntries.get(x - 1);
    }

    /**
     * Checks if the treasure is currently locked and inaccessible.
     * A locked treasure cannot be opened by players and typically requires
     * certain conditions to be met (like defeating all keepers) before unlocking.
     *
     * @return true if the treasure is locked, false if it can be accessed
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * Unlocks the treasure, making it accessible to players.
     * This method is typically called when all unlock conditions have been met,
     * such as defeating all treasure keepers or waiting for a time delay.
     */
    public void unlock() {
        isLocked = false;
    }

    /**
     * Gets the player who dealt the most damage to treasure keepers.
     * This method identifies the top contributor during combat phases,
     * which may be used for special rewards or recognition.
     *
     * @return the Player who dealt the most damage, or null if no damage was recorded
     */
    @Nullable
    public Player getPlayerWithMostDamage() {

        double max = 0;
        UUID maxUUID = null;

        for (Map.Entry<UUID, Double> entry : playerDamage.entrySet()) {

            if (entry.getValue() > max) {

                if (Bukkit.getPlayer(entry.getKey()) == null) continue;

                max = entry.getValue();
                maxUUID = entry.getKey();
            }

        }

        if (maxUUID == null) return null;
        return Bukkit.getPlayer(maxUUID);

    }

    /**
     * Returns all players who have participated in this treasure hunt.
     * Participants are typically players who have engaged with the treasure
     * or its keepers in some meaningful way.
     *
     * @return a List of all participating Players
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<Player> getParticipants() {
        return participants;
    }

    /**
     * Returns the names of all participating players as strings.
     * This method provides a convenient way to get participant names
     * for display purposes in messages or administrative interfaces.
     *
     * @return an ArrayList of participant names as Strings
     */
    public ArrayList<String> getParticipantsNames() {
        ArrayList<String> names = new ArrayList<>();

        participants.forEach(p -> names.add(p.getName()));
        return names;
    }

    /**
     * Checks if enough time has passed before the treasure can be picked up.
     * Some treasures have a minimum time delay before they become available
     * to prevent immediate claiming after spawn.
     *
     * @return true if the pickup delay time has elapsed, false otherwise
     */
    public boolean timePassedBeforePickup() {
        return getHunt().getElapsedTime() >= getTreasureData().getMilliesBeforePickup();
    }

    /**
     * Adds a player to the list of treasure hunt participants.
     * This method registers a player as having engaged with the treasure,
     * typically called when they interact with keepers or the treasure itself.
     *
     * @param p the Player to add to the participants list
     */
    public void addParticipant(Player p) {
        participants.add(p);
    }

    /**
     * Removes all players from the participants list.
     * This method clears the participant registry, typically used during
     * treasure cleanup or reset operations.
     */
    public void clearParticipants() {
        participants.clear();
    }

    private static boolean hasCustomModels(Hunt h, Location loc) {

        TreasureData.TreasureType type = h.getTreasure().getTreasureData().getTreasureType();
        if (type == TreasureData.TreasureType.VANILLA) return false;

        if (Utils.isEnabled("ItemsAdder")) {
            if (type == TreasureData.TreasureType.ITEMSADDER_FURNITURE || type == TreasureData.TreasureType.ITEMSADDER_BLOCK || type == TreasureData.TreasureType.ITEMSADDER_ENTITY)
                return CustomBlock.byAlreadyPlaced(loc.getBlock()) != null || CustomFurniture.byAlreadySpawned(loc.getBlock()) != null;
        }

        if (Utils.isEnabled("Nexo")) {
            if (type == TreasureData.TreasureType.NEXO_BLOCK || type == TreasureData.TreasureType.NEXO_FURNITURE)
                return NexoFurniture.isFurniture(loc) || NexoBlocks.isCustomBlock(loc.getBlock());
        }

        if (Utils.isEnabled("Oraxen")) {
            if (type == TreasureData.TreasureType.ORAXEN_FURNITURE)
                return OraxenFurniture.isFurniture(loc.getBlock()) || OraxenBlocks.isOraxenBlock(loc.getBlock());
        }

        return false;

    }

    /**
     * Checks if the specified location contains an active treasure.
     * This static method examines the given location to determine if
     * any treasure is currently positioned there.
     *
     * @param loc the location to check for treasure presence
     * @return true if a treasure exists at the location, false otherwise
     */
    public static boolean isTreasure(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;

        for (Hunt h : Hunt.getActiveTreasures()) {
            Location treasureLoc = h.getTreasure().getLocation();
            if (treasureLoc.getWorld() == null) continue;

            if (treasureLoc.getWorld().getName().equals(loc.getWorld().getName())
                    && treasureLoc.getBlockX() == loc.getBlockX()
                    && treasureLoc.getBlockZ() == loc.getBlockZ()
                    && treasureLoc.getBlockY() == loc.getBlockY()) {
                return true;
            }

        }

        return false;

    }

    /**
     * Gets the treasure keeper configuration associated with a specific entity.
     * This method maps spawned keeper entities back to their configuration
     * data for access to behavior settings and properties.
     *
     * @param e the LivingEntity to check for keeper association
     * @return the TreasureKeeper configuration, or null if entity is not a keeper
     */
    public TreasureKeeper getTreasureKeeper(LivingEntity e) {

        UUID uuid = e.getUniqueId();

        for (TreasureKeeper t : getTreasureData().getTreasureKeepers()) {

            if (t.isSpawned()) {
                if (t.getUUIDs().contains(uuid)) return t;
            }

        }

        return null;

    }

    /**
     * Retrieves the treasure instance located at the specified position.
     * This static method searches all active treasures and returns the one
     * positioned at the given location coordinates.
     *
     * @param loc the location to search for a treasure
     * @return the Treasure at the location, or null if none exists there
     */
    public static Treasure getTreasure(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;

        for (Hunt h : Hunt.getActiveTreasures()) {
            Location treasureLoc = h.getTreasure().getLocation();
            if (treasureLoc.getWorld() == null) continue;

            if (treasureLoc.getWorld().getName().equals(loc.getWorld().getName())
                    && treasureLoc.getBlockX() == loc.getBlockX()
                    && treasureLoc.getBlockZ() == loc.getBlockZ()
                    && treasureLoc.getBlockY() == loc.getBlockY()) {
                return h.getTreasure();
            }

        }

        return null;

    }


    /**
     * Checks if all treasure keeper mobs have been defeated.
     * This method determines if the combat phase is complete by verifying
     * that no living keeper entities remain active.
     *
     * @return true if all keepers are defeated, false if any remain alive
     */
    public boolean mobsCleared() {
        for (Entity e : spawnedTreasureKeepers) {
            if (!e.isDead()) return false;
        }

        return true;
    }

    /**
     * Returns a list of all remaining living treasure keeper entities.
     * This method filters the spawned keeper list to return only those
     * entities that are still alive and active in the world.
     *
     * @return an ArrayList of living keeper Entities
     */
    public ArrayList<Entity> getRemainingMobs() {

        if (spawnedTreasureKeepers.isEmpty()) return new ArrayList<>();

        ArrayList<Entity> entities = new ArrayList<>();

        for (Entity e : spawnedTreasureKeepers) {

            if (!e.isDead()) entities.add(e);

        }

        return entities;


    }

    /**
     * Returns the count of remaining living treasure keepers.
     * This method provides a quick way to check how many guardian
     * creatures are still protecting the treasure.
     *
     * @return the number of remaining alive treasure keepers
     */
    public int remainingMobs() {
        int count = 0;
        for (Entity e : spawnedTreasureKeepers) {
            if (!e.isDead()) count++;
        }
        return count;
    }

    /**
     * Checks if the specified entity is a treasure keeper for this treasure.
     * This method determines if a given living entity is one of the
     * guardian creatures spawned to protect this treasure.
     *
     * @param e the LivingEntity to check for keeper status
     * @return true if the entity is a treasure keeper, false otherwise
     */
    public boolean isTreasureKeeper(LivingEntity e) {
        if (e.hasMetadata("treasure-hunt-id") && !e.getMetadata("treasure-hunt-id").isEmpty()) {
            return e.getMetadata("treasure-hunt-id").get(0).asString().equals(h.getHuntId().toString());
        }
        return spawnedTreasureKeepers.contains(e);
    }

    public static String renameWorld(World w) {
        String worldName = w.getName();
        return worldName.replaceAll("[^-_A-Za-z0-9]", "_").trim();
    }

    public static String getHologramName(World w, String id) {
        String worldName = renameWorld(w).toLowerCase();
        return "treasure_" + worldName + "_" + id.toLowerCase();
    }

    /**
     * Creates and displays holographic text above the treasure.
     * This method generates floating text displays that provide information
     * about the treasure status, requirements, or other relevant details to players.
     */
    public void hologram() {

        Location loc = getLocation().clone();
        String hologramName = getHologramName(getLocation().getWorld(), getTreasureData().getIdentifier());

        if (Utils.isEnabled("DecentHolograms")) {

            if (DHAPI.getHologram(hologramName) != null) {
                DHAPI.getHologram(hologramName).delete();
            }

            DHAPI.createHologram(hologramName, loc.clone().add(0.5, 1.5, 0.5), false).setDownOrigin(true);
            Hologram h = DHAPI.getHologram(hologramName);

            ArrayList<String> lines = new ArrayList<>();

            for (String s : Messages.getAndFormatList("messages.treasure-hologram")) {
                lines.add(s.replace("{time}", Utils.getCountDown(getHunt().getRemainingTime())));
            }

            h.enable();
            h.setUpdateInterval(20);
            DHAPI.setHologramLines(h, lines);
            h.updateAll();

        } else if (Utils.isEnabled("FancyHolograms")) {

            if (FancyHologramsPlugin.get().getHologramManager().getHologram(hologramName).isPresent()) {
                HologramHandler.getInstance().delete(hologramName);
            }

            HologramHandler handler = HologramHandler.getInstance();
            handler.createFancyHologram(loc, hologramName);
        } else {
            createItem();
        }

    }

    /**
     * Triggers a flare effect to mark the treasure location.
     * This method creates visual effects (particles, sounds, or other indicators)
     * to help players locate the treasure from a distance.
     */
    public void flare() {

        String flare = getTreasureData().getFlareType();
        int refresh = flare.equalsIgnoreCase("few") ? 40 : 10;
        Location loc = getLocation().clone();

        if (!flare.equalsIgnoreCase("none")) {

            BukkitRunnable run = new BukkitRunnable() {

                final Particle p = getTreasureData().getFlareParticle();

                @Override
                public void run() {

                    if (!isActive()) {
                        this.cancel();
                        return;
                    }

                    if (flare.equalsIgnoreCase("few")) {

                        for (int i = 0; i < 10; i++) {
                            loc.getWorld().spawnParticle(p, Utils.getParticleLocation(loc).add(0, i, 0), 1);
                        }

                    } else {

                        for (int i = 0; i < 20; i++) {
                            loc.getWorld().spawnParticle(p, Utils.getParticleLocation(loc).add(0, i, 0), 1);
                        }

                    }

                }

            };

            run.runTaskTimerAsynchronously(getPlugin(), 0, refresh);

        }

    }

    /**
     * Activates continuous particle and visual effects around the treasure.
     * This method starts ongoing visual effects that make the treasure
     * more visible and attractive to players, running until the treasure is removed.
     */
    public void enableEffects() {
        boolean mobs = getTreasureData().requireAllMobsDead();
        String anim = getTreasureData().getTreasureAnimation();

        if (mobs) {
            if (anim.equalsIgnoreCase("protection")) Effects.runCircle(this);
        } else {

            if (anim.equalsIgnoreCase("orb")) {
                Effects.runOrbs(this);
            } else if (anim.equalsIgnoreCase("spiral")) {
                Effects.createDoubleSpiral(this);
            }

        }

    }

    /**
     * Spawns the physical treasure representation in the world.
     * This private method creates the actual treasure block, entity, or item
     * at the designated location based on the treasure type configuration.
     */
    private void spawnTreasure() {

        Bukkit.getScheduler().runTask(getPlugin(), () ->
        {

            Location location;

            Location temp_location = getLocation().clone();

            Particle part = getTreasureData().getTreasureParticles();
            int distance_to_spawn = getTreasureData().getDistanceFromPlayerToSpawnMobs();

            if (temp_location.getBlock().getType().isSolid()) {
                location = temp_location.clone().add(0, 1, 0);
                this.l = location;
            } else location = temp_location.clone();

            placeTreasureBlock(location);

            Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
            {

                if (distance_to_spawn <= 0) {
                    roundController.startRound();
                }

            }, 1);

            hologram();
            enableEffects();
            flare();

            this.isActive = true;
            h.setTreasureActive();

            tickTreasure(location, part, distance_to_spawn);
            announceSpawnedTreasure();
            getTreasureData().getWaypoint().set(this);

            Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () ->
            {
                DiscordWebhook webhook = DiscordWebhook.getInstance();
                webhook.sendWebhookMessage(DiscordWebhook.DiscordTreasureEventType.SPAWN, this);
            });

        });


    }

    /**
     * Returns the hunt instance that owns this treasure.
     * This method provides access to the parent hunt object for retrieving
     * hunt-specific information like duration, start time, and configuration.
     *
     * @return the Hunt instance that created this treasure
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Hunt getHunt() {
        return h;
    }

    /**
     * Spawns all configured treasure keeper guardian entities.
     * This method creates and positions the protective creatures around the treasure
     * based on the keeper configurations defined in the treasure data.
     */
    /*
    public void spawnTreasureKeepers()
    {

        for(TreasureKeeper k : getTreasureData().getTreasureKeepers())
        {
            k.spawn(spawnedTreasureKeepers, getLocation());
        }

    }
     */

    /**
     * Checks if the treasure can be opened by players.
     * This method evaluates all conditions required for treasure access including
     * lock status, keeper defeats, time delays, and other configured requirements.
     *
     * @return true if the treasure can be opened, false if conditions are not met
     */
    public boolean canBeOpened() {
        return remainingMobs() == 0 && timePassedBeforePickup() && !isLocked();
    }

    /**
     * Finds a safe location near the treasure for entity spawning inside structures.
     * This private method calculates appropriate positions for spawning keepers
     * or effects when the treasure is located within buildings or enclosed spaces.
     *
     * @param loc the base location to find a nearby position for
     * @return a Location adjusted for safe spawning inside structures
     */
    private Location getNearLocationInside(Location loc) {
        int x = Utils.randInt(-6, 6);
        int z = Utils.randInt(-6, 6);

        Location randomLoc = loc.clone().add(x, 0, z);

        return Utils.getSafeBlock(randomLoc, loc.getWorld().getSpawnLocation());
    }

    /**
     * Monitors and controls treasure keeper movement within allowed boundaries.
     * This private method ensures that spawned keeper entities don't wander too far
     * from the treasure location, maintaining them within the configured distance limits.
     *
     * @param location           the treasure location to measure distances from
     * @param wandering_distance the maximum allowed distance for keeper movement
     * @param spawnsInside       whether the treasure is located inside a structure
     */
    private void checkMobWandering(Location location, int wandering_distance, boolean spawnsInside) {
        for (Entity e : getRemainingMobs()) {

            if (!e.getLocation().getWorld().getName().equals(location.getWorld().getName())) {
                e.remove();
                continue;
            }

            if (e.getLocation().distanceSquared(location) > wandering_distance * wandering_distance) {

                Bukkit.getScheduler().runTask(getPlugin(), () ->
                {
                    e.teleport(getNearLocationInside(location));
                });
            }

        }
    }

    /**
     * Handles automatic mob spawning when players approach the treasure.
     * This private method triggers the appearance of treasure keepers when players
     * come within a specified distance, creating dynamic encounter scenarios.
     *
     * @param distance_to_spawn the distance threshold for triggering mob spawning
     * @param location          the treasure location to measure player distances from
     * @param w                 the world where the treasure is located
     */
    private void spawnMobsOnPlayerApproach(int distance_to_spawn, Location location, World w) {
        if (distance_to_spawn > 0 && !Utils.getNearbyPlayers(location, distance_to_spawn).isEmpty() && !spawned) {
            Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
            {
                // Only here to amplify the rising from the ground effect
                w.strikeLightningEffect(location);

                roundController.startRound();

                enableEffects();
                spawned = true;
            }, 1);
        }
    }

    /**
     * Updates holographic displays above the treasure each tick.
     * This private method refreshes floating text displays with current treasure
     * status information, showing locked/unlocked states and other dynamic content.
     *
     * @param location the treasure location where holograms should appear
     * @param w        the world containing the treasure
     * @param part     the particle effect to display with the hologram
     * @param unlocked the text to show when the treasure is accessible
     * @param locked   the text to show when the treasure is still locked
     */
    private void tickHologram(Location location, World w, Particle part, String unlocked, String locked) {
        String playerWithMostDamage = getPlayerWithMostDamage() == null ? "N/A" : getPlayerWithMostDamage().getName();
        String playerWithSecondDamage = getXthMostDamage(2) == null ? "N/A" : Bukkit.getOfflinePlayer(getXthMostDamage(2).getKey()).getName();
        String playerWithThirdDamage = getXthMostDamage(3) == null ? "N/A" : Bukkit.getOfflinePlayer(getXthMostDamage(3).getKey()).getName();

        double mostDamageGiven = getPlayerWithMostDamage() == null ? 0 : getDamageGiven(getPlayerWithMostDamage());
        double secondMostDamageGiven = getXthMostDamage(2) == null ? 0 : getXthMostDamage(2).getValue();
        double thirdMostDamageGiven = getXthMostDamage(3) == null ? 0 : getXthMostDamage(3).getValue();

        w.spawnParticle(part, Utils.getParticleLocation(location), 3);
        w.spawnParticle(part, Utils.getParticleLocation(location), 3);

        ArrayList<String> lines = new ArrayList<>();
        String yes = Messages.get("menu-yes");
        String no = Messages.get("menu-no");

        String hologramName = getHologramName(w, getTreasureData().getIdentifier());

        int remMobs = remainingMobs();

        for (String s : Messages.getAndFormatList("messages.treasure-hologram")) {
            lines.add(s
                    .replace("{time}", Utils.getCountDown(getHunt().getRemainingTime()))
                    .replace("{status}", canBeOpened() ? unlocked : locked)
                    .replace("{alias}", getTreasureData().getTreasureName())
                    .replace("{requires-key}", isLocked() ? yes : no)
                    .replace("{remaining_mobs}", remMobs + "")
                    .replace("{player-with-most-damage}", playerWithMostDamage)
                    .replace("{most-damage-given}", mostDamageGiven + "")
                    .replace("{second-most-damage-given-player}", playerWithSecondDamage)
                    .replace("{third-most-damage-given-player}", playerWithThirdDamage)
                    .replace("{second-most-damage}", secondMostDamageGiven + "")
                    .replace("{third-most-damage}", thirdMostDamageGiven + ""));
        }

        if (Utils.isEnabled("DecentHolograms")) {

            if (DHAPI.getHologram(hologramName) != null) {

                Hologram h = DHAPI.getHologram(hologramName);

                Bukkit.getScheduler().runTask(getPlugin(), () ->
                {
                    DHAPI.setHologramLines(h, lines);
                    h.updateAll();
                });

            }

        } else if (Utils.isEnabled("FancyHolograms")) {
            HologramHandler.getInstance().update(hologramName, lines);
        }
    }

    /**
     * Handles per-tick treasure updates including effects and mob management.
     * This private method manages continuous treasure behavior such as particle
     * effects, player proximity detection, and keeper spawning logic.
     *
     * @param location          the treasure location for effect positioning
     * @param part              the particle effect to display around the treasure
     * @param distance_to_spawn the distance threshold for mob spawning
     */
    private void tickTreasure(Location location, Particle part, int distance_to_spawn) {

        World w = location.getWorld();
        int wandering_distance = getTreasureData().getMobWanderingDistance();

        String unlocked = Messages.get("treasure-unlocked");
        String locked = Messages.get("treasure-locked");

        boolean spawnsInside = getTreasureData().isSpawnsInside() || getTreasureData().spawnToCertainCoords();

        BukkitRunnable run = new BukkitRunnable() {

            @Override
            public void run() {

                try {

                    if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - h.getStartTime()) >= h.getDuration()) {
                        Bukkit.getScheduler().runTask(getPlugin(), () -> remove(true));
                    }

                    if (getTreasureData().canOpenChest()) {
                        if (getRewardInventory().isEmpty()) {

                            if (getReceivedCommandRewards().isEmpty()) {
                                getPlugin().getLogger().warning("There is no ItemReward configured (either there is none, or none of the rewards will be given to players due to their chance). This thing is incompatible with the `get-rewards-from-chest` setting. If you have this setting set to `true` you should have at least one reward with 100% chance in order to make sure there is always a reward.");
                                this.cancel();
                            }

                            if (isActive()) Bukkit.getScheduler().runTask(getPlugin(), () ->
                            {
                                remove(true);
                                Bukkit.getScheduler().runTask(getPlugin(), () -> l.getChunk().setForceLoaded(false));
                                this.cancel();
                            });

                            return;
                        }
                    }

                    if (isActive()) {

                        tickHologram(location, w, part, unlocked, locked);

                        spawnMobsOnPlayerApproach(distance_to_spawn, location, w);
                        checkMobWandering(location, wandering_distance, spawnsInside);

                    } else {
                        getHunt().setInactive();
                        Bukkit.getScheduler().runTask(getPlugin(), () -> l.getChunk().setForceLoaded(false));
                        this.cancel();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };

        run.runTaskTimerAsynchronously(getPlugin(), 0, 20);
    }

    private boolean isInLava(Entity e) {

        Location loc = e.getLocation();
        Location under = loc.clone().add(0, -1, 0);

        return loc.getBlock().getType() == Material.LAVA || under.getBlock().getType() == Material.LAVA;
    }

    private boolean isTouchdownLocation(Entity e) {

        if (Utils.isPaper()) {
            return e.isOnGround() || e.isInLava() || e.isInWater();
        }

        return e.isOnGround() || e.isInWater() || isInLava(e);

    }

    /**
     * Performs animation effects on treasure entities and items.
     * This private method handles visual animations like rotation, floating,
     * or other movement effects applied to treasure representations.
     *
     * @param location the location where animation effects should occur
     * @param e        the entity to animate (may be null)
     * @param is       the item stack to animate (may be null)
     */
    private void animate(Location location, Entity e, ItemStack is) {

        if (e instanceof ArmorStand) {

            ArmorStand animation = (ArmorStand) e;

            animation.setMetadata("treasure_stand", new FixedMetadataValue(getPlugin(), "treasure_stand"));
            animation.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 600, 1)); //Doesn't exist < 1.13
            animation.setGravity(true);

            animation.addScoreboardTag("treasureAnimationStand");

            if (Utils.isPaper()) {
                animation.setCanMove(true);
                animation.setCanTick(true);
                animation.setPersistent(false);
            }

            animation.setBasePlate(false);
            animation.setHelmet(is);
            animation.setInvulnerable(true);
            animation.setVisible(false);

        }

        BukkitRunnable runFallingParticles = new BukkitRunnable() {

            @Override
            public void run() {

                if (e == null || !e.isValid()) {
                    this.cancel();
                    return;
                }

                if (isTouchdownLocation(e)) {

                    Bukkit.getScheduler().runTask(getPlugin(), e::remove);
                    location.getWorld().spawnParticle(EXPLOSION, e.getLocation(), 1);

                    spawnTreasure();
                    this.cancel();
                    return;
                }

                location.getWorld().spawnParticle(CAMPFIRE_SIGNAL_SMOKE, e.getLocation().add(0, 3, 0), 1);

            }
        };

        runFallingParticles.runTaskTimerAsynchronously(getPlugin(), 0, 1);

        BukkitRunnable animationWatchdog = new BukkitRunnable() {

            Location prevLoc;

            @Override
            public void run() {

                Location currentLoc = e.getLocation();

                if (prevLoc == null) {
                    prevLoc = currentLoc;
                    return;
                }

                if (prevLoc.getWorld().getName().equals(currentLoc.getWorld().getName())) {
                    if (prevLoc.distanceSquared(currentLoc) == 0) {
                        e.remove();
                        remove(false);
                        this.cancel();

                        getPlugin().getLogger().warning("Animation watchdog detected a stuck animation and removed it for treasure: " + getTreasureData().getIdentifier());

                    }
                }

            }

        };

        animationWatchdog.runTaskTimer(getPlugin(), 0, 30);

    }

    /**
     * Executes the treasure spawn animation sequence.
     * This private method handles the visual and auditory effects that occur
     * when a treasure first appears in the world, creating an impressive reveal.
     *
     * @param location the location where the spawn animation should play
     */
    private void runAnimation(Location location) {

        String s = getTreasureData().getTreasureBlockString();

        if (getTreasureData().getTreasureType() == TreasureData.TreasureType.VANILLA) {

            if (getTreasureData().fallFromTheSky()) {
                ArmorStand animation = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, 50, 0), EntityType.ARMOR_STAND);
                ItemStack is = new ItemStack(Utils.checkMaterial(s));

                animate(location, animation, is);
            } else {
                location.getWorld().spawnParticle(EXPLOSION_EMITTER, location, 1);
                spawnTreasure();
            }

        } else {
            location.getWorld().spawnParticle(EXPLOSION_EMITTER, location, 1);
            spawnTreasure();
        }

    }

    /**
     * Creates and initializes the treasure in the world.
     * This method orchestrates the complete treasure creation process including
     * spawning, effect activation, keeper generation, and announcement to players.
     */
    public void create() {

        Location location = getLocation().clone();
        int delay = getTreasureData().getDelay();

        announceUpcomingTreasure(delay);

        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
        {

            location.getChunk().load();
            location.getChunk().setForceLoaded(true);

            TreasureSpawnEvent event = new TreasureSpawnEvent(this);
            Bukkit.getPluginManager().callEvent(event);

            runAnimation(location);

        }, Math.abs(delay));

    }

    /**
     * Places the physical treasure block at the designated location.
     * This private method sets the actual block in the world that represents
     * the treasure, handling different block types based on configuration.
     *
     * @param location the exact position where the treasure block should be placed
     */
    private void placeTreasureBlock(Location location) {

        ItemStack stack;
        Material mat;

        boolean itemsAdder = Utils.isEnabled("ItemsAdder");
        boolean oraxen = Utils.isEnabled("Oraxen");
        boolean nexo = Utils.isEnabled("Nexo");
        String name = getTreasureData().getTreasureBlockString();

        TreasureData.TreasureType type = getTreasureData().getTreasureType();

        if (itemsAdder) {

            if (type == TreasureData.TreasureType.ITEMSADDER_ENTITY) {

                CustomEntity entity = CustomEntity.spawn(name, location);
                furnitureEntity = entity.getEntity();

            } else if (type == TreasureData.TreasureType.ITEMSADDER_BLOCK) {

                CustomBlock block = CustomBlock.place(name, location);
                furnitureEntity = null;

            } else if (type == TreasureData.TreasureType.ITEMSADDER_FURNITURE) {

                CustomFurniture furniture = CustomFurniture.spawn(name, location.getBlock());
                furnitureEntity = furniture.getArmorstand();

            } else {
                stack = Utils.checkMaterial(name);
                mat = stack.getType();

                location.getBlock().setType(mat);
            }

        } else if (oraxen) {

            if (type == TreasureData.TreasureType.ORAXEN_FURNITURE) {
                furnitureEntity = OraxenFurniture.place(name, location, Rotation.NONE, BlockFace.NORTH);
            } else {
                stack = Utils.checkMaterial(name);
                mat = stack.getType();

                location.getBlock().setType(mat);
            }

        } else if (nexo) {

            if (type == TreasureData.TreasureType.NEXO_FURNITURE) {
                furnitureEntity = NexoFurniture.place(name, location, Rotation.NONE, BlockFace.NORTH);
            } else if (type == TreasureData.TreasureType.NEXO_BLOCK) {
                NexoBlocks.place(name, location);
                furnitureEntity = null;
            } else {
                stack = Utils.checkMaterial(name);
                mat = stack.getType();

                location.getBlock().setType(mat);
            }

        } else {
            stack = Utils.checkMaterial(name);
            mat = stack.getType();

            location.getBlock().setType(mat);
        }

        location.getWorld().playEffect(location, Effect.STEP_SOUND, Material.DIRT);

    }

    /**
     * Broadcasts messages announcing that the treasure has spawned.
     * This private method sends notifications to players informing them
     * about the newly available treasure and its location.
     */
    private void announceSpawnedTreasure() {

        SendCenteredMessage scm = new SendCenteredMessage();

        int x = h.getLocation().getBlockX();
        int z = h.getLocation().getBlockZ();
        String world = h.getLocation().getWorld().getName();
        String alias = getTreasureData().getTreasureName();
        String duration = h.getDuration() + "";

        int offset = getTreasureData().getCoordsNearTreasure();
        int x_offset = x + Utils.randInt(-offset, offset);
        int z_offset = z + Utils.randInt(-offset, offset);

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () ->
        {

            for (Player p : Bukkit.getOnlinePlayers()) {

                for (String s : Messages.getAndFormatList("messages.hunt-message")) {
                    scm.sendCenteredMessage(p,
                            s.replace("{x}", x + "")
                                    .replace("{z}", z + "")
                                    .replace("{x-offset}", x_offset + "")
                                    .replace("{z-offset}", z_offset + "")
                                    .replace("{world}", world)
                                    .replace("{alias}", alias)
                                    .replace("{duration}", duration));
                }

            }

        });

    }

    /**
     * Broadcasts messages announcing an upcoming treasure spawn.
     * This private method sends advance notifications to players about
     * a treasure that will appear after the specified delay period.
     *
     * @param delay the time in seconds until the treasure will spawn
     */
    private void announceUpcomingTreasure(int delay) {
        if (delay > 0) {

            SendCenteredMessage scm = new SendCenteredMessage();

            int x = h.getLocation().getBlockX();
            int z = h.getLocation().getBlockZ();
            String world = h.getLocation().getWorld().getName();
            String alias = getTreasureData().getTreasureName();
            String duration = h.getDuration() + "";

            int offset = getTreasureData().getCoordsNearTreasure();
            int x_offset = x + Utils.randInt(-offset, offset);
            int z_offset = z + Utils.randInt(-offset, offset);

            for (Player p : Bukkit.getOnlinePlayers()) {

                for (String s : Messages.getAndFormatList("messages.announce-treasure")) {
                    scm.sendCenteredMessage(p, s
                            .replace("{x}", x + "")
                            .replace("{z}", z + "")
                            .replace("{x-offset}", x_offset + "")
                            .replace("{z-offset}", z_offset + "")
                            .replace("{world}", world)
                            .replace("{alias}", alias)
                            .replace("{duration}", duration));
                }

            }
        }
    }

    /**
     * Gets all players currently near the specified treasure.
     * This static method finds players within interaction range of the treasure,
     * useful for applying effects or determining who can access the treasure.
     *
     * @param t the treasure to check for nearby players
     * @return an ArrayList of Players within range of the treasure
     */
    public static ArrayList<Player> getPlayersNearTreasure(Treasure t) {
        return Utils.getNearbyPlayers(t.getLocation(), Settings.getProtectionRadius());
    }

    /**
     * Checks if the specified player is near any active treasure.
     * This static method determines if a player is within interaction distance
     * of any currently spawned treasure in the game world.
     *
     * @param p the player to check for treasure proximity
     * @return true if the player is near a treasure, false otherwise
     */
    public static boolean isNearTreasure(Player p) {

        double radius = Settings.getProtectionRadius();
        double radiusSquared = radius * radius;
        Location pLoc = p.getLocation();

        for (Hunt h : Hunt.getActiveTreasures()) {

            Location hLoc = h.getLocation();
            if (hLoc.getWorld() != null && hLoc.getWorld().getName().equals(pLoc.getWorld().getName())) {

                if (hLoc.distanceSquared(pLoc) <= radiusSquared) {
                    return true;
                }

            }

        }

        return false;

    }

    /**
     * Launches celebratory fireworks at the treasure location.
     * This private method creates firework displays to mark special treasure
     * events such as spawning, unlocking, or successful claiming.
     */
    private void launchFireworks() {

        Location loc = getLocation().clone();

        if (getTreasureData().shootFireworks()) {

            Firework firework_1 = loc.getWorld().spawn(loc.add(1, 0, 0), Firework.class);
            FireworkMeta meta_1 = firework_1.getFireworkMeta();
            meta_1.addEffects(FireworkEffect.builder().withColor(Color.PURPLE).withTrail().with(FireworkEffect.Type.BALL_LARGE).build());
            meta_1.setPower(3);
            firework_1.setFireworkMeta(meta_1);

            Firework firework_2 = loc.getWorld().spawn(loc.add(0, 0, 1), Firework.class);
            FireworkMeta meta_2 = firework_2.getFireworkMeta();
            meta_2.addEffects(FireworkEffect.builder().withColor(Color.WHITE).withTrail().with(FireworkEffect.Type.BALL).build());
            meta_2.setPower(1);
            firework_2.setFireworkMeta(meta_2);

            Firework firework_3 = loc.getWorld().spawn(loc.subtract(1, 0, 0), Firework.class);
            FireworkMeta meta_3 = firework_3.getFireworkMeta();
            meta_3.addEffects(FireworkEffect.builder().withColor(Color.ORANGE).withTrail().with(FireworkEffect.Type.STAR).build());
            meta_3.setPower(2);
            firework_3.setFireworkMeta(meta_3);

            Firework firework_4 = loc.getWorld().spawn(loc.subtract(0, 0, 1), Firework.class);
            FireworkMeta meta_4 = firework_4.getFireworkMeta();
            meta_4.addEffects(FireworkEffect.builder().withColor(Color.FUCHSIA).withTrail().with(FireworkEffect.Type.BURST).build());
            meta_4.setPower(2);
            firework_4.setFireworkMeta(meta_4);

        }

    }

    /**
     * Executes command-based rewards for the specified player.
     * This method runs all configured console commands as rewards for the player,
     * typically called when they successfully claim or interact with the treasure.
     *
     * @param p the player to award command rewards to
     */
    public void runCommandPrizes(Player p) {

        try {
            for (CommandReward c : getTreasureData().getCommandRewards()) {

                if (c.shouldRewardToTopX() && !c.isTopX(getHowManyPlayersOpenedChest() + 1)) continue;

                c.run(p);
            }

        } catch (Exception e) {
            getPlugin().getLogger().warning("Tried to run the command rewards, but one of the commands is invalid!");
        }

        receivedCommandRewards.add(p);

    }

    /**
     * Counts how many players have opened the treasure chest.
     * This private method tracks the number of unique players who have
     * successfully accessed the treasure's reward inventory.
     *
     * @return the count of players who have opened the treasure chest
     */
    private int getHowManyPlayersOpenedChest() {
        return receivedCommandRewards.size();
    }

    /**
     * Awards prizes to the top participating players based on damage dealt.
     * This method distributes rewards to the highest-contributing players
     * when the treasure is configured for top-player-only rewards.
     *
     * @param topPlayers the number of top players to reward
     */
    public void awardPrizesToTop(int topPlayers) {

        launchFireworks();
        int cooldown = Settings.getCooldown();

        int topCounter = 0;

        for (UUID u : getSortedPlayersByDamage()) {

            if (topCounter >= topPlayers) break;

            if (Bukkit.getPlayer(u) == null) continue;
            Player p = Bukkit.getPlayer(u);

            if (Cooldown.hasCooldown(p.getUniqueId(), "treasure-winner")) {
                String msg = Messages.get("winner-cooldown");
                if (msg != null) {
                    p.sendMessage(msg.replace("{time}", Utils.formatRemainingTime(Cooldown.getRemainingTimeMinutes(p.getUniqueId(), "treasure-winner"))));
                }
                return;
            }

            if (cooldown != 0) {
                Cooldown cd = new Cooldown(p.getUniqueId(), "treasure-winner", cooldown * 60);
                cd.set();
            }

            runCommandPrizes(p);

            for (ItemReward i : getTreasureData().getItemRewards()) {

                if (i.shouldGiveOnlyToTopX() && !i.isTopX(topCounter)) continue;
                if (Utils.chance() > i.getChance()) continue;

                ItemStack item = i.getItemStack();

                if (!Utils.hasFullInventory(p)) {

                    if (item == null || item.getType() == Material.AIR) continue;

                    p.getInventory().addItem(item);

                } else {

                    if (item == null || item.getType() == Material.AIR) continue;

                    p.getWorld().dropItemNaturally(p.getLocation(), item);
                    p.sendMessage(Messages.get("full-inventory").replace("{amount}", item.getAmount() + "").replace("{item}", Utils.setCapitals(item.getType().toString().toLowerCase().replace("_", " "))));

                }

            }

            announceWinners();
            topCounter++;
        }

    }

    /**
     * Distributes all configured rewards to eligible players.
     * This method handles the complete reward distribution process including
     * item rewards, command execution, and notification of successful claims.
     */
        public void awardPrizes() {
        if (getRoundController() != null) {
            int idx = getRoundController().getRoundNumber() - 1;
            TreasureRound round = getRoundController().getRoundRegistry().getRound(idx);
            if (round != null && round.getRoundData() != null) {
                awardPrizes(round.getRoundData());
            }
        }
    }

    /**
     * Awards all configured prizes to a specific player.
     * This method gives both item and command rewards to the specified player,
     * typically called when they successfully claim the treasure.
     *
     * @param p the player to award prizes to
     */
    public void awardPrize(Player p) {

        int cooldown = Settings.getCooldown();
        World w = getLocation().getWorld();

        if (cooldown != 0) {
            Cooldown cd = new Cooldown(p.getUniqueId(), "treasure-winner", cooldown * 60);
            cd.set();
        }

        launchFireworks();
        runCommandPrizes(p);

        int xOffset = (int) ((Math.random() * 2 * 5 + 1) - 5);
        int zOffset = (int) ((Math.random() * 2 * 5 + 1) - 5);
        Location dropLocation = Utils.getHighestBlock(w, getLocation().getBlockX() + xOffset, getLocation().getBlockZ() + zOffset, getLocation());

        int x = 1;
        boolean dropOnGround = getTreasureData().dropsItemsOnGround();

        for (ItemReward r : getTreasureData().getItemRewards()) {
            if (Utils.chance() > r.getChance()) continue;

            if (dropOnGround) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
                {

                    Item i = w.dropItemNaturally(dropLocation, r.getItemStack());

                    i.setGlowing(true);
                    i.setInvisible(false);
                    i.setVisibleByDefault(true);
                    i.setInvulnerable(true);
                    i.setCanMobPickup(false);
                    i.setCustomNameVisible(true);

                    i.setCustomName(Utils.format(getTreasureData().getDroppedItemName()));

                }, 20L * x);

                x++;
            } else {
                ItemStack item = r.getItemStack();

                if (!Utils.hasFullInventory(p)) {

                    if (item == null || item.getType() == Material.AIR) continue;

                    p.getInventory().addItem(item);

                } else {

                    if (item == null || item.getType() == Material.AIR) continue;

                    p.getWorld().dropItemNaturally(p.getLocation(), item);
                    p.sendMessage(Messages.get("full-inventory").replace("{amount}", item.getAmount() + "").replace("{item}", Utils.setCapitals(item.getType().toString().toLowerCase().replace("_", " "))));

                }

            }

        }

        announceWinner(p);

    }

    /**
     * Removes all active treasures from all worlds.
     * This static method performs a complete cleanup of all treasure instances,
     * typically used during server shutdown or plugin reloading.
     */
    public static void removeAll() {

        for (Hunt h : Hunt.getActiveTreasures()) {

            h.getTreasure().remove(false);
        }

    }

    /**
     * Removes the default Hologram if not Hologram plugin is used
     */
    private void removeItem() {


        for (Entity e : Utils.getNearbyEntities(getLocation(), 2)) {
            if (e instanceof Item || e instanceof ArmorStand) {
                e.remove();
            }
        }


    }

    /**
     * Removes all treasure keeper entities from the world.
     * This method despawns and cleans up all guardian creatures associated
     * with this treasure, freeing up server resources.
     */
    public void clearMobs() {

        for (LivingEntity e : getLocation().getWorld().getLivingEntities()) {
            if (isTreasureKeeper(e)) e.remove();
        }

    }

    /**
     * Applies cooldowns to players who have opened the treasure chest.
     * This private method sets appropriate cooldown periods for players
     * to prevent immediate re-interaction with treasure systems.
     */
    private void setCooldownForChestOpeners() {
        int cooldown = Settings.getCooldown();

        for (Player p : receivedCommandRewards) {
            if (cooldown != 0) {
                Cooldown cd = new Cooldown(p.getUniqueId(), "treasure-winner", cooldown * 60);
                cd.set();
            }
        }
    }

    /**
     * Completely removes the treasure from the world and performs cleanup.
     * This method handles the full treasure removal process including entity cleanup,
     * block restoration, effect stopping, and hunt deactivation.
     */
    public void remove(boolean runEffects) {

        TreasureData.TreasureType type = getTreasureData().getTreasureType();
        String hologramName = getHologramName(getLocation().getWorld(), getTreasureData().getIdentifier());

        if (getTreasureData().canOpenChest()) {

            setCooldownForChestOpeners();

            List<HumanEntity> viewers = new ArrayList<>(getRewardInventory().getViewers());

            for (HumanEntity h : viewers) {
                h.closeInventory();
            }

        }

        if (type == TreasureData.TreasureType.ITEMSADDER_BLOCK) {
            CustomBlock.remove(getLocation());
        } else if (type == TreasureData.TreasureType.ITEMSADDER_ENTITY) {
            if (furnitureEntity != null) CustomEntity.byAlreadySpawned(furnitureEntity).destroy();
        } else if (type == TreasureData.TreasureType.ITEMSADDER_FURNITURE) {
            if (furnitureEntity != null) CustomFurniture.remove(furnitureEntity, false);
        } else if (type == TreasureData.TreasureType.ORAXEN_FURNITURE) {
            if (furnitureEntity != null) OraxenFurniture.remove(furnitureEntity, null);
        } else if (type == TreasureData.TreasureType.NEXO_FURNITURE) {
            if (furnitureEntity != null) NexoFurniture.remove(furnitureEntity);
        } else if (type == TreasureData.TreasureType.NEXO_BLOCK) {
            NexoBlocks.remove(getLocation());
        }

        getLocation().getBlock().setType(Material.AIR);

        if (runEffects) {
            getLocation().getWorld().spawnParticle(EXPLOSION_EMITTER, getLocation(), 3);
            getLocation().getWorld().strikeLightningEffect(getLocation());
        }

        isActive = false;

        if (Utils.isEnabled("DecentHolograms")) {
            Hologram h = DHAPI.getHologram(hologramName);
            if (h != null) h.delete();
        } else if (Utils.isEnabled("FancyHolograms")) {

            HologramHandler.getInstance().delete(hologramName);

        }

        removeItem();
        clearMobs();

        getHunt().setInactive();
        getTreasureData().getWaypoint().remove();

    }

    /**
     * Announces the winners of the treasure hunt to all players.
     * This private method broadcasts messages celebrating the successful
     * treasure claimants and their achievements.
     */
    private void announceWinners() {

        SendCenteredMessage scm = new SendCenteredMessage();
        String participantsNames = Joiner.on(", ").join(getParticipantsNames());
        String playerWithMostDamage = getPlayerWithMostDamage() == null ? "N/A" : getPlayerWithMostDamage().getName();
        double mostDamageGiven = getPlayerWithMostDamage() == null ? 0 : getDamageGiven(getPlayerWithMostDamage());

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () ->
        {
            for (Player p : Bukkit.getOnlinePlayers()) {

                for (String s : Messages.getAndFormatList("messages.winner-broadcast")) {

                    scm.sendCenteredMessage(p, s.replace("{player}", participantsNames)
                            .replace("{player-with-most-damage}", playerWithMostDamage)
                            .replace("{most-damage-given}", mostDamageGiven + ""));

                }

            }


            for (Player p : getParticipants()) {
                scm.sendCenteredMessage(p, Messages.get("winner-message"));
            }

            Bukkit.getConsoleSender().sendMessage(Messages.get("winner-console").replace("{player}", participantsNames).replace("{x}", getLocation().getBlockX() + "")
                    .replace("{z}", getLocation().getBlockZ() + ""));
        });

        TreasureTask.updateLastHunt(getTreasureData().getIdentifier());

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () ->
        {
            DiscordWebhook webhook = DiscordWebhook.getInstance();
            webhook.sendWebhookMessage(DiscordWebhook.DiscordTreasureEventType.CLAIM, this);
        });

    }

    /**
     * Announces a specific player as a treasure hunt winner.
     * This method broadcasts congratulatory messages about the specified
     * player's successful treasure claim to all online players.
     *
     * @param k the player to announce as a winner
     */
    public void announceWinner(Player k) {

        SendCenteredMessage scm = new SendCenteredMessage();
        String playerWithMostDamage = getPlayerWithMostDamage() == null ? "N/A" : getPlayerWithMostDamage().getName();
        double mostDamageGiven = getPlayerWithMostDamage() == null ? 0 : getDamageGiven(getPlayerWithMostDamage());

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () ->
        {
            for (Player p : Bukkit.getOnlinePlayers()) {

                for (String s : Messages.getAndFormatList("messages.winner-broadcast")) {

                    scm.sendCenteredMessage(p, s.replace("{player}", k.getName())
                            .replace("{player-with-most-damage}", playerWithMostDamage)
                            .replace("{most-damage-given}", mostDamageGiven + ""));

                }

            }

            scm.sendCenteredMessage(k, Messages.get("winner-message"));

            Bukkit.getConsoleSender().sendMessage(Messages.get("winner-console").replace("{player}", k.getName()).replace("{x}", getLocation().getBlockX() + "")
                    .replace("{z}", getLocation().getBlockZ() + ""));
        });

        TreasureTask.updateLastHunt(getTreasureData().getIdentifier());
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () ->
        {
            DiscordWebhook webhook = DiscordWebhook.getInstance();
            webhook.sendWebhookMessage(DiscordWebhook.DiscordTreasureEventType.CLAIM, this);
        });
    }

    /**
     * Calculates the appropriate location for dropped treasure items.
     * This private method determines the best position for placing reward items
     * that are configured to drop on the ground rather than go to inventory.
     *
     * @param l the base treasure location to calculate drop position from
     * @return a Location where items should be dropped
     */
    private Location getItemLocation(Location l) {

        return new Location(l.getWorld(), l.getBlockX() + 0.5, l.getBlockY() + 1, l.getBlockZ() + 0.5);
    }

    /**
     * Creates an item to hold the default hologram if not Hologram getPlugin() is installed
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    private void createItem() {

        Location loc = getLocation().clone();
        Location l = getItemLocation(loc);

        if (!l.getBlock().isEmpty() || l.getBlock().isLiquid()) {
            return;
        }

        ItemStack itm = new ItemStack(getTreasureData().getTreasureIcon());
        ItemMeta meta = itm.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aAncient Treasure"));
        itm.setItemMeta(meta);

        Bukkit.getScheduler().runTask(getPlugin(), () ->
        {

            Item i = l.getWorld().dropItem(l, itm);

            i.setVelocity(new Vector(0, 0, 0));
            i.setInvulnerable(true);
            i.setPickupDelay(32767);
            i.setCustomName(Messages.get("treasure-icon-text"));
            i.setCustomNameVisible(true);
            i.setGravity(false);

            i.addScoreboardTag("defaultTreasureHologram");

        });

    }

    /**
     * Runs on startup and cleans up the world of all MysticTreasure related things
     * Only thing not covered by any cleanup is the treasure block -- in case of a hard crash
     * the block will stay in the world
     */
    public static void cleanup() {

        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {

                if (e.getScoreboardTags().contains("defaultTreasureHologram")
                        || e.getScoreboardTags().contains("treasureWaypointStand")
                        || e.getScoreboardTags().contains("treasureKeeper")
                        || e.getScoreboardTags().contains("treasureAnimationStand")) {
                    e.remove();
                }

            }
        }

    }

}
