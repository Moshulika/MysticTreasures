/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.Moshu.TreasureHunt.Components;

import com.Moshu.Misc.Storage.FileHandler;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeper;
import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeperDrops;
import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeperEquipment;
import com.Moshu.TreasureHunt.Components.Rewards.CommandReward;
import com.Moshu.TreasureHunt.Components.Rewards.ItemReward;
import com.Moshu.TreasureHunt.Core.Treasure;
import com.Moshu.TreasureHunt.TreasureScheduler;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomEntity;
import dev.lone.itemsadder.api.CustomFurniture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.th0rgal.oraxen.api.OraxenFurniture;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Represents the configuration data for a treasure hunt.
 * This class contains all the settings and properties that define how a treasure hunt behaves,
 * including spawn conditions, rewards, keepers, and various gameplay mechanics.
 * <p>
 * TreasureData objects are loaded from configuration files and used to create
 * Treasure instances when hunts are initiated.
 *
 * @author Moshu
 * @version 1.0
 */
public class TreasureData {

    /**
     * Enumeration of different treasure types supported by the plugin.
     * Defines the various ways treasures can be implemented and spawned.
     */
    public enum TreasureType {

        VANILLA,
        ITEMSADDER_ENTITY,
        ITEMSADDER_BLOCK,
        ITEMSADDER_FURNITURE,
        ORAXEN_FURNITURE,
        NEXO_FURNITURE,
        NEXO_BLOCK,
        OTHER
    }

    private TreasureType treasureType;


    @NotNull
    private final static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private TreasureKey treasureKey;
    private final String worldName;
    private final String treasureName;
    private final String treasureBlockString;
    private Particle treasureParticles;
    private final String treasureAnimation;
    private final boolean fallFromTheSky;
    private final Material treasureIcon;
    private final boolean requireAllMobsDead;
    private final String flareType;
    private Particle flareParticle;
    private final boolean fireworks;
    private final int maxTreasureDistance;
    private final int delay;
    private final int chanceForTreasure;
    private final int distanceFromPlayerToSpawnMobs;
    private final int mobWanderingDistance;
    private final boolean enableMobTracker;
    private final boolean animateMobSpawning;
    private final int interval;
    private final int duration;
    private final int cooldown;
    private final int minutesBeforePickup;
    private final int clicksToOpen;
    private final String droppedItemName;
    private final List<PotionEffect> potionEffects; //Replace String with potion effects
    private final boolean spawnToCertainCoords;
    private final List<Location> spawnCoords; //Replace String with locations
    private String identifier;
    private final ConfigurationSection defaultSection;
    private final int coordsNearTreasure;
    private final String menuItem;
    private final int cooldownBetweenClicks;
    private final boolean spawnsInside;
    private final String awardMethodString;
    private Treasure.AwardMethod awardMethod;
    private final ArrayList<TreasureScheduler> ownTreasureSchedulers = new ArrayList<>();
    private final TreasureWaypoint waypoint;

    private int rewardTopX;
    private boolean openChest;
    private boolean rewardMostDamageGiven;
    private boolean dropItemsOnGround;
    private boolean rewardAllPlayersWhoParticipated;

    private static final ArrayList<TreasureScheduler> allTreasureSchedulers = new ArrayList<>();
    private static ArrayList<TreasureData> treasureData;
    private final static ArrayList<String> treasureIdentifiers = new ArrayList<>();

    private TreasureRoundRegistry roundRegistry;

    public static void load() {
        FileHandler h = FileHandler.getInstance();
        treasureData = h.setup();

        fetchTreasuresIdentifiers();
    }

    public static void reload() {
        FileHandler h = FileHandler.getInstance();
        treasureData = h.reload();

        fetchTreasuresIdentifiers();
    }

    private static void fetchTreasuresIdentifiers() {
        for (TreasureData d : getTreasureData()) {
            String id = d.getIdentifier();
            if (id != null) {
                treasureIdentifiers.add(id);
            }
        }
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TreasureRoundRegistry getRoundRegistry() {
        return roundRegistry;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public ConfigurationSection getDefaultSection() {
        return defaultSection;
    }

    public String getTreasureBlockString() {
        return this.treasureBlockString;
    }

    @SuppressFBWarnings("MS_EXPOSE_REP")
    public static List<String> getTreasureIdentifiers() {
        return Collections.unmodifiableList(treasureIdentifiers);
    }

    public static synchronized ArrayList<TreasureData> getTreasureData() {
        // Always return a non-null, mutable list to simplify callers and avoid NPEs in tests
        if (treasureData == null) {
            treasureData = new ArrayList<>();
        }
        return treasureData;
    }

    public List<TreasureScheduler> getOwnTreasureSchedulers() {
        return Collections.unmodifiableList(ownTreasureSchedulers);
    }

    @SuppressFBWarnings("MS_EXPOSE_REP")
    public static List<TreasureScheduler> getAllTreasureSchedulers() {
        return Collections.unmodifiableList(allTreasureSchedulers);
    }

    public boolean canOpenChest() {
        return openChest;
    }

    public int getCooldownBetweenClicks() {
        return cooldownBetweenClicks;
    }

    public String getMenuItem() {
        return menuItem;
    }

    public boolean isSpawnsInside() {
        return spawnsInside;
    }

    public int getRewardTopX() {
        return rewardTopX;
    }


    public static TreasureData getByIdentifier(String id) {

        for (TreasureData d : getTreasureData()) {
            if (d.getIdentifier().equals(id)) {
                return d;
            }
        }

        return null;

    }

    public boolean shouldOnlyRewardTopX() {
        return rewardTopX > 0;
    }

    public boolean rewardMostDamageGiven() {
        return this.rewardMostDamageGiven;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TreasureKey getTreasureKey() {
        return treasureKey;
    }


    public List<TreasureKeeper> getTreasureKeepers() {
        List<TreasureKeeper> all = new ArrayList<>();
        if (roundRegistry != null && roundRegistry.getRounds() != null) {
            for (TreasureRound round : roundRegistry.getRounds()) {
                all.addAll(round.getRoundTreasureKeepers());
            }
        }
        return Collections.unmodifiableList(all);
    }

    public List<ItemReward> getItemRewards() {
        List<ItemReward> all = new ArrayList<>();
        if (roundRegistry != null && roundRegistry.getRounds() != null) {
            for (TreasureRound round : roundRegistry.getRounds()) {
                if (round.getRoundData() != null) {
                    all.addAll(round.getRoundData().getItemRewards());
                }
            }
        }
        return Collections.unmodifiableList(all);
    }

    public List<CommandReward> getCommandRewards() {
        List<CommandReward> all = new ArrayList<>();
        if (roundRegistry != null && roundRegistry.getRounds() != null) {
            for (TreasureRound round : roundRegistry.getRounds()) {
                if (round.getRoundData() != null) {
                    all.addAll(round.getRoundData().getCommandRewards());
                }
            }
        }
        return Collections.unmodifiableList(all);
    }

    public boolean hasDebuff() {
        if (roundRegistry != null && roundRegistry.getRounds() != null) {
            for (TreasureRound round : roundRegistry.getRounds()) {
                if (round.getRoundData() != null && round.getRoundData().getDebuff() != null) return true;
            }
        }
        return false;
    }
    
    // Kept for backward compatibility, returns the first round's debuff or null
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TreasureDebuff getDebuff() {
        if (roundRegistry != null && roundRegistry.getRounds() != null && !roundRegistry.getRounds().isEmpty()) {
            TreasureRound round = roundRegistry.getRound(0);
            if (round != null && round.getRoundData() != null) {
                return round.getRoundData().getDebuff();
            }
        }
        return null;
    }

    public TreasureType getTreasureType() {
        return treasureType;
    }

    public void setTreasureType(TreasureType type) {
        this.treasureType = type;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName) == null ? Bukkit.getWorlds().get(0) : Bukkit.getWorld(worldName);
    }

    private TreasureType fetchTreasureBlockType() {

        String name = treasureBlockString;
        boolean itemsAdder = Utils.isEnabled("ItemsAdder");
        boolean oraxen = Utils.isEnabled("Oraxen");
        boolean nexo = Utils.isEnabled("Nexo");

        if (plugin != null) {
            plugin.getLogger().log(Level.INFO, "Loading treasure block..");
        }

        if (name == null) {
            return TreasureType.VANILLA;
        }

        if (itemsAdder) {

            try {

                if (CustomEntity.isInRegistry(name)) {
                    return TreasureType.ITEMSADDER_ENTITY;
                } else if (CustomBlock.isInRegistry(name)) {
                    return TreasureType.ITEMSADDER_BLOCK;
                } else if (CustomFurniture.isInRegistry(name)) {
                    return TreasureType.ITEMSADDER_FURNITURE;
                } else {
                    return TreasureType.VANILLA;
                }

            } catch (ClassCastException e) {
                if (plugin != null) {
                    plugin.getLogger().log(Level.SEVERE, "Problem with getting treasure block " + name);
                }
                return TreasureType.VANILLA;
            }

        } else if (oraxen) {

            try {

                if (OraxenFurniture.isFurniture(name)) {
                    return TreasureType.ORAXEN_FURNITURE;
                } else {
                    return TreasureType.VANILLA;
                }


            } catch (ClassCastException e) {
                if (plugin != null) {
                    plugin.getLogger().log(Level.SEVERE, "Problem with getting treasure block " + name);
                }
                return TreasureType.VANILLA;
            }

        } else if (nexo) {

            try {

                if (NexoFurniture.isFurniture(name)) {
                    return TreasureType.NEXO_FURNITURE;
                } else if (NexoBlocks.isCustomBlock(name)) {
                    return TreasureType.NEXO_BLOCK;
                } else {
                    return TreasureType.VANILLA;
                }


            } catch (ClassCastException e) {
                if (plugin != null) {
                    plugin.getLogger().log(Level.SEVERE, "Problem with getting treasure block " + name);
                }
                return TreasureType.VANILLA;
            }

        } else return TreasureType.VANILLA;

    }

    public static int getAmountFromRangeStatic(String s) {

        String[] arr = s.split("-");
        if (arr.length == 0) return 0;

        for (String x : arr) {
            if (!Utils.isInt(x)) {
                if (plugin != null) {
                    plugin.getLogger().warning("Invalid amount of item-reward: " + x);
                }
            }
        }

        if (arr.length == 1) return Integer.parseInt(arr[0]);
        else return Utils.randInt(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));

    }

    private int getAmountFromRange(String s) {
        return getAmountFromRangeStatic(s);
    }


    public static ArrayList<PotionEffect> deserializeEffects(List<String> potionEffects) {

        ArrayList<PotionEffect> potionEffectsList = new ArrayList<>();
        if (potionEffects.isEmpty()) return potionEffectsList;


        PotionEffectType t;

        String[] arr;
        String name;
        int level;

        for (String s : potionEffects) {

            if (s.isEmpty()) continue;

            arr = s.split(":");

            if (arr.length != 2) {
                plugin.getLogger().warning("Invalid potion effect: " + s);
                continue;
            }

            if (!Utils.isInt(arr[1])) {
                plugin.getLogger().warning("Invalid potion effect: " + s);
                continue;
            }

            name = arr[0];
            level = Integer.parseInt(arr[1]);

            t = PotionEffectType.getByName(name);

            if (t == null) {
                plugin.getLogger().warning("Invalid potion effect: " + s);
                continue;
            }


            potionEffectsList.add(new PotionEffect(t, PotionEffect.INFINITE_DURATION, level));

        }

        return potionEffectsList;
    }

    public static ArrayList<PotionEffect> deserializeEffectsWithDuration(List<String> potionEffects) {

        ArrayList<PotionEffect> potionEffectsList = new ArrayList<>();
        if (potionEffects.isEmpty()) return potionEffectsList;

        PotionEffectType t;

        String[] arr;
        String name;
        int level;
        int duration;

        for (String s : potionEffects) {

            if (s.isEmpty()) continue;

            arr = s.split(":");

            if (arr.length != 3) {
                plugin.getLogger().warning("Invalid potion effect: " + s);
                continue;
            }

            name = arr[0];

            if (!Utils.isInt(arr[1]) || !Utils.isInt(arr[2])) {
                plugin.getLogger().warning("Invalid potion effect: " + s);
                continue;
            }

            level = Integer.parseInt(arr[1]);
            duration = Integer.parseInt(arr[2]);

            t = PotionEffectType.getByName(name);

            if (t == null) {
                plugin.getLogger().warning("Invalid potion effect: " + s);
                continue;
            }


            potionEffectsList.add(new PotionEffect(t, duration, level));

        }

        return potionEffectsList;
    }

    /**
     * Does not check if the coords are in the border.
     * User's responsability for now
     *
     * @param s the encoded string
     * @return if the coords are valid integers
     */
    private boolean validCoords(String s) {

        String[] arr = s.split(":");

        for (String x : arr) {

            if (!Utils.isInt(x)) {
                plugin.getLogger().warning("Invalid location: " + s);
                return false;
            }

        }

        return true;

    }

    private ArrayList<Location> deserializeLocations(List<String> coords) {

        ArrayList<Location> locations = new ArrayList<>();

        if (Bukkit.getWorld(getWorldName()) == null) {
            plugin.getLogger().warning("World '" + getWorldName() + "' does not exist!");
            return locations;
        }

        String[] arr;
        int x, y, z;
        World w = Bukkit.getWorld(getWorldName());

        for (String c : coords) {

            if (validCoords(c)) {
                arr = c.split(":");
                x = Integer.parseInt(arr[0]);
                y = Integer.parseInt(arr[1]);
                z = Integer.parseInt(arr[2]);

                locations.add(new Location(w, x, y, z));
            }

        }

        return locations;

    }

    /**
     * Backward compatibility with ex-settings
     */
    private void parseAwardMethod() {

        awardMethod = Treasure.AwardMethod.fromString(awardMethodString);

        switch (awardMethod) {


            case LEGACY: {
                plugin.getLogger().warning("You are using legacy award-method. Please add `award-method`" +
                        " to your treasure.yml and set to one of these values: CHEST, ALL_PLAYERS, HIGHEST_DAMAGE, DROP_ON_GROUND, TOP_X" +
                        ". Each of them correspond to the old fields in the treasure file. For TOP_X, you use it like this: `award-method: TOP_5`.");
            }

            case TOP_X: {

                String x = awardMethodString.replace("TOP_", "");

                if (Utils.isInt(x)) {
                    rewardTopX = Integer.parseInt(x);
                } else {
                    plugin.getLogger().warning("The award-method of the treasure is invalid, reading TOP_X, but has invalid format. Defaulting to 3");
                    rewardTopX = 3;
                }

            }

            case CHEST: {
                this.openChest = true;
            }

            case ALL_PLAYERS: {
                this.rewardAllPlayersWhoParticipated = true;
            }

            case HIGHEST_DAMAGE: {
                this.rewardMostDamageGiven = true;
            }

            case DROP_ON_GROUND: {
                this.dropItemsOnGround = true;
            }

        }

    }

    public void addSpawnpoint(Location loc) {

        ArrayList<String> encoded = new ArrayList<>();
        spawnCoords.add(loc);

        String s;

        for (Location l : spawnCoords) {
            s = l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();

            if (encoded.contains(s)) continue;
            encoded.add(s);
        }

        getDefaultSection().set("spawn-coords", encoded);

    }

    /**
     * Fetches all the data from the treasure file
     *
     * @param defaultSection the treasure's path
     */
    @SuppressFBWarnings({"CT_CONSTRUCTOR_THROW", "EI_EXPOSE_REP2"})
    public TreasureData(ConfigurationSection defaultSection) {

        if (defaultSection == null) {
            throw new IllegalStateException("Default treasure configuration not found.");
        }

        this.identifier = defaultSection.getName();
        this.defaultSection = defaultSection;
        this.worldName = defaultSection.getString("world-name", "world");
        this.treasureName = defaultSection.getString("treasure-name", "Mysterious Treasure");
        this.treasureBlockString = defaultSection.getString("treasure-block", "ENDER_CHEST");
        this.treasureAnimation = defaultSection.getString("treasure-animation", "protection");
        this.fallFromTheSky = defaultSection.getBoolean("fall-from-the-sky", true);
        this.treasureIcon = Material.valueOf(defaultSection.getString("treasure-icon", "EMERALD"));
        this.requireAllMobsDead = defaultSection.getBoolean("require-all-mobs-dead", true);
        this.flareType = defaultSection.getString("flare.type", "many");
        this.fireworks = defaultSection.getBoolean("fireworks", true);
        this.maxTreasureDistance = defaultSection.getInt("max-treasure-distance", 15000);
        this.delay = defaultSection.getInt("spawn-task.delay", 200);
        this.chanceForTreasure = defaultSection.getInt("spawn-task.chance-for-treasure", 20);
        this.distanceFromPlayerToSpawnMobs = defaultSection.getInt("distance-from-player-to-spawn-mobs", 0);
        this.mobWanderingDistance = defaultSection.getInt("mob-wandering-distance", 40);
        this.coordsNearTreasure = defaultSection.getInt("coords-near-treasure", 0);
        this.enableMobTracker = defaultSection.getBoolean("enable-mob-tracker", true);
        this.animateMobSpawning = defaultSection.getBoolean("animate-mob-spawning", true);
        this.interval = Math.max(1, defaultSection.getInt("spawn-task.interval", 30));
        this.duration = Math.max(1, defaultSection.getInt("duration", 20));
        this.cooldown = defaultSection.getInt("spawn-task.cooldown", 15);
        this.minutesBeforePickup = defaultSection.getInt("minutes-before-pickup", 0);
        this.clicksToOpen = defaultSection.getInt("clicks-to-open", 1);
        this.droppedItemName = defaultSection.getString("dropped-item-name", "&6Treasure Loot");
        this.potionEffects = deserializeEffects(defaultSection.getStringList("potion-effects"));
        this.spawnToCertainCoords = defaultSection.getBoolean("spawn-to-certain-coords", false);
        this.spawnCoords = deserializeLocations(defaultSection.getStringList("spawn-coords"));
        this.menuItem = defaultSection.getString("menu-item", "STONE");
        this.cooldownBetweenClicks = defaultSection.getInt("cooldown-between-clicks", 0);
        this.spawnsInside = defaultSection.getBoolean("treasure-spawns-inside", false);

        //TODO: This 4 need removing
        this.openChest = defaultSection.getBoolean("get-rewards-from-chest", false);
        this.rewardTopX = defaultSection.getInt("only-reward-top-x", 0);
        this.rewardAllPlayersWhoParticipated = defaultSection.getBoolean("reward-all-players-who-participated", false);
        this.dropItemsOnGround = defaultSection.getBoolean("drop-items-on-ground", false);
        this.rewardMostDamageGiven = defaultSection.getBoolean("reward-highest-damage", false);

        this.awardMethodString = defaultSection.getString("award-method", "LEGACY");

        parseAwardMethod();


        try {
            String particleStr = defaultSection.getString("treasure-particles", "COMPOSTER");
            this.treasureParticles = Particle.valueOf(particleStr);

            String flareParticle = defaultSection.getString("flare.particle", "COMPOSTER");
            this.flareParticle = Particle.valueOf(flareParticle);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid treasure particles or flare particles: " + e.getMessage());
        }

        plugin.getLogger().log(Level.INFO, "Loading treasure with id: `" + defaultSection.getName() + "`, and name: " + treasureName);

        setTreasureType(fetchTreasureBlockType());
        plugin.getLogger().log(Level.INFO, "Fetched treasure type: " + treasureType + " from id: " + treasureBlockString);

        //Load Waypoint
        ConfigurationSection waypointSection = defaultSection.getConfigurationSection("waypoint");
        if (waypointSection != null) {

            this.waypoint = new TreasureWaypoint(
                    waypointSection.getBoolean("enabled", false),
                    waypointSection.getString("color", "GOLD"),
                    waypointSection.getInt("range", 1000)
            );

            plugin.getLogger().log(Level.INFO, "Fetched waypoint, enabled: " + waypoint.isEnabled());

        } else {
            plugin.getLogger().warning("Configuration section 'waypoint' does not exist for treasure " + treasureName);
            this.waypoint = new TreasureWaypoint(false, "GOLD", 1000);
        }

        // Load TreasureKey
        ConfigurationSection keySection = defaultSection.getConfigurationSection("treasure-key");

        if (keySection != null) {


            String itemStr = keySection.getString("item", "TRIPWIRE_HOOK");

            this.treasureKey = new TreasureKey(

                    keySection.getBoolean("enabled", false),
                    itemStr,
                    keySection.getString("name", "&c&l&oTREASURE KEY"),
                    keySection.getStringList("lore")
            );

            plugin.getLogger().log(Level.INFO, "Fetched treasure key, enabled: " + treasureKey.requiresKey());

        } else {
            plugin.getLogger().warning("Configuration section 'treasure-key' does not exist for treasure " + treasureName);
        }

        ConfigurationSection schedulerSection = defaultSection.getConfigurationSection("scheduler");
        if (schedulerSection != null) {

            Set<String> schedulerSectionKeys = schedulerSection.getKeys(false);

            for (String schedulerId : schedulerSectionKeys) {

                ConfigurationSection schSection = schedulerSection.getConfigurationSection(schedulerId);

                if (schSection != null) {

                    TreasureScheduler treasureScheduler = new TreasureScheduler();
                    treasureScheduler.setId(schedulerId);
                    treasureScheduler.setDay(schSection.getString("day", "monday"));
                    treasureScheduler.setTime(schSection.getString("time", "17:00"));
                    treasureScheduler.setWorld(schSection.getString("world", "world"));
                    treasureScheduler.setEncodedCoords(schSection.getString("coords", "0:0:0"));
                    treasureScheduler.setEnabled(schSection.getBoolean("enabled", false));
                    treasureScheduler.setData(this);

                    ownTreasureSchedulers.add(treasureScheduler);

                    plugin.getLogger().log(Level.INFO, "Fetched scheduler `" + treasureScheduler.getId() + "` with settings: " + treasureScheduler.getDay()
                            + " at " + treasureScheduler.getTime() + " @ " + treasureScheduler.getWorld() + " " + treasureScheduler.getEncodedCoords());

                } else {
                    plugin.getLogger().warning("Configuration section '" + schSection + "' does not exist!");
                }
            }

            allTreasureSchedulers.addAll(ownTreasureSchedulers);

        } else {
            plugin.getLogger().warning("Configuration section 'scheduler' does not exist!");
        }



        roundRegistry = new TreasureRoundRegistry(this);
        List<String> rounds = defaultSection.getStringList("rounds");
        
        if (rounds.isEmpty()) {
            if (plugin != null) {
                plugin.getLogger().severe("Treasure '" + defaultSection.getName() + "' is missing the 'rounds' list! Please configure rounds in your treasure.yml.");
            }
        }
        
        roundRegistry.setRoundIds(rounds);
        roundRegistry.load();

    }

    public String getSanitizedRewards(int max) {

        if (max <= 0) max = 3;

        StringBuilder rewards = new StringBuilder();
        List<ItemReward> rewardsList = getItemRewards();

        ItemReward currentReward;

        max = Math.min(max, rewardsList.size());

        for (int i = 0; i < max; i++) {

            currentReward = rewardsList.get(i);
            rewards.append(currentReward.getAmount()).append("x ").append(currentReward.getName());
            rewards.append(", ");

        }

        String s = rewards.toString();
        return s.substring(0, s.length() - 2);
    }

    public String getSanitizedCommandRewards(int max) {

        if (max <= 0) max = 3;

        StringBuilder rewards = new StringBuilder();
        List<CommandReward> rewardsList = getCommandRewards();

        CommandReward currentReward;

        max = Math.min(max, rewardsList.size());

        for (int i = 0; i < max; i++) {

            currentReward = rewardsList.get(i);
            rewards.append(currentReward.getIdentifier());
            rewards.append(", ");

        }

        String s = rewards.toString();
        return s.substring(0, s.length() - 2);
    }

    public String getSanitizedTreasureKeepers(int max) {

        if (max <= 0) max = 3;

        StringBuilder keepers = new StringBuilder();
        List<TreasureKeeper> keepersList = getTreasureKeepers();

        TreasureKeeper currentKeeper;

        max = Math.min(max, keepersList.size());

        for (int i = 0; i < max; i++) {

            currentKeeper = keepersList.get(i);
            keepers.append(currentKeeper.getAmount()).append("x ");
            keepers.append(currentKeeper.getCustomName());
            keepers.append(", ");

        }

        String s = keepers.toString();
        return s.substring(0, s.length() - 2);
    }

    public int getCoordsNearTreasure() {
        return coordsNearTreasure;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getTreasureName() {
        return treasureName;
    }

    public Particle getTreasureParticles() {
        return treasureParticles;
    }

    public String getTreasureAnimation() {
        return treasureAnimation;
    }

    public boolean fallFromTheSky() {
        return fallFromTheSky;
    }

    public Material getTreasureIcon() {
        return treasureIcon;
    }

    public boolean requireAllMobsDead() {
        return requireAllMobsDead;
    }

    public String getFlareType() {
        return flareType;
    }

    public Particle getFlareParticle() {
        return flareParticle;
    }

    public boolean shootFireworks() {
        return fireworks;
    }

    public int getMaxTreasureDistance() {
        return maxTreasureDistance;
    }

    public int getDelay() {
        return delay;
    }

    public int getChanceForTreasure() {
        return chanceForTreasure;
    }

    public boolean rewardAllPlayersWhoParticipated() {
        return rewardAllPlayersWhoParticipated;
    }

    public boolean dropsItemsOnGround() {
        return dropItemsOnGround;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TreasureWaypoint getWaypoint() {
        return waypoint;
    }

    public int getDistanceFromPlayerToSpawnMobs() {
        return distanceFromPlayerToSpawnMobs;
    }

    public int getMobWanderingDistance() {
        return mobWanderingDistance;
    }

    public boolean enableMobTracker() {
        return enableMobTracker;
    }

    public boolean animateMobSpawning() {
        return animateMobSpawning;
    }

    @NotNull
    public int getInterval() {
        return interval;
    }

    @NotNull
    public int getDuration() {
        return duration;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getMinutesBeforePickup() {
        return minutesBeforePickup;
    }

    public long getMilliesBeforePickup() {
        return TimeUnit.MINUTES.toMillis(minutesBeforePickup);
    }

    public int getClicksToOpen() {
        return clicksToOpen;
    }

    public String getDroppedItemName() {
        return droppedItemName;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public boolean spawnToCertainCoords() {
        return spawnToCertainCoords;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<Location> getSpawnCoords() {
        return spawnCoords;
    }

}
