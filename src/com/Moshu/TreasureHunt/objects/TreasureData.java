package com.Moshu.TreasureHunt.objects;

import com.Moshu.Misc.FileHandler;
import com.Moshu.Misc.Utils;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomEntity;
import dev.lone.itemsadder.api.CustomFurniture;
import io.th0rgal.oraxen.api.OraxenFurniture;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class TreasureData {

    public enum TreasureType
    {

        VANILLA,
        ENTITY,
        BLOCK,
        FURNITURE,
        ORAXEN_FURNITURE,
        OTHER
    }

    private TreasureType treasureType;

    private final ArrayList<ItemReward> itemRewards = new ArrayList<>();
    private final ArrayList<CommandReward> commandRewards = new ArrayList<>();
    private final ArrayList<TreasureKeeper> treasureKeepers = new ArrayList<>();

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
    private final boolean rewardAllPlayersWhoParticipated;
    private final boolean dropItemsOnGround;
    private final int distanceFromPlayerToSpawnMobs;
    private final int mobWanderingDistance;
    private final boolean enableMobTracker;
    private final boolean animateMobSpawning;
    private final int interval;
    private final int duration;
    private final int cooldown;
    private final int clicksToOpen;
    private final String droppedItemName;
    private final List<PotionEffect> potionEffects; //Replace String with potion effects
    private final boolean spawnToCertainCoords;
    private final List<Location> spawnCoords; //Replace String with locations
    private String identifier;

    private static ArrayList<TreasureData> treasureData;
    private final static ArrayList<String> treasureIdentifiers = new ArrayList<>();

    public static void load()
    {
        FileHandler h = FileHandler.getInstance();
        treasureData = h.setup();

        fetchTreasuresIdentifiers();

    }

    public static void reload()
    {
        FileHandler h = FileHandler.getInstance();
        treasureData = h.reload();
    }

    private static void fetchTreasuresIdentifiers()
    {
        for(TreasureData d : getTreasureData())
        {
            treasureIdentifiers.add(d.getIdentifier());
        }
    }

    public String getTreasureBlockString()
    {
        return this.treasureBlockString;
    }

    public static ArrayList<String> getTreasureIdentifiers()
    {
        return treasureIdentifiers;
    }

    public static ArrayList<TreasureData> getTreasureData()
    {
        return treasureData;
    }

    public static TreasureData getByIdentifier(String id)
    {

        for(TreasureData d : getTreasureData())
        {
            if(d.getIdentifier().equals(id))
            {
                return d;
            }
        }

        return null;

    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public TreasureKey getTreasureKey() {
        return treasureKey;
    }

    public List<TreasureKeeper> getTreasureKeepers() {
        return treasureKeepers;
    }

    public List<ItemReward> getItemRewards() {
        return itemRewards;
    }

    public List<CommandReward> getCommandRewards() {
        return commandRewards;
    }

    public TreasureType getTreasureType()
    {
        return treasureType;
    }

    public void setTreasureType(TreasureType type)
    {
        this.treasureType = type;
    }

    public World getWorld()
    {
        return Bukkit.getWorld(worldName) == null ?  Bukkit.getWorlds().get(0) : Bukkit.getWorld(worldName);
    }

    private TreasureType fetchTreasureBlockType()
    {

        String name = treasureBlockString;
        boolean itemsAdder = Utils.isEnabled("ItemsAdder");
        boolean oraxen = Utils.isEnabled("Oraxen");

        if (itemsAdder) {

            try {

                if (CustomEntity.isInRegistry(name)) {
                    return TreasureType.ENTITY;
                } else if (CustomBlock.isInRegistry(name)) {
                    return TreasureType.BLOCK;
                } else if (CustomFurniture.isInRegistry(name)) {
                    return TreasureType.FURNITURE;
                } else {
                    return TreasureType.VANILLA;
                }

            } catch (NullPointerException | ClassCastException e) {

                plugin.getLogger().log(Level.SEVERE, "Problem with getting treasure block " + name);
                return TreasureType.VANILLA;

            }

        }
        else if(oraxen)
        {

            try {

                if (OraxenFurniture.isFurniture(name)) {
                    return TreasureType.ORAXEN_FURNITURE;
                } else {
                    return TreasureType.VANILLA;
                }


            } catch (NullPointerException | ClassCastException e) {
                plugin.getLogger().log(Level.SEVERE, "Problem with getting treasure block " + name);
                return TreasureType.VANILLA;
            }

        }
        else return TreasureType.VANILLA;

    }

    private int getAmountFromRange(String s)
    {

        String[] arr = s.split("-");

        for(String x : arr)
        {
            if(!Utils.isInt(x))
            {
                plugin.getLogger().warning("Invalid amount of item-reward: " + x);
            }
        }

        if(arr.length == 1) return Integer.parseInt(arr[0]);
        else return Utils.randInt(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));

    }


    private ArrayList<PotionEffect> deserializeEffects(List<String> potionEffects)
    {

        ArrayList<PotionEffect> potionEffectsList = new ArrayList<>();
        PotionEffectType t;

        String[] arr;
        String name;
        int level;

        for(String s : potionEffects)
        {

            arr = s.split(":");
            name =  arr[0];

            if(!Utils.isInt(arr[1]))
            {
                plugin.getLogger().warning("Invalid potion effect: " + s);
                continue;
            }

            level = Integer.parseInt(arr[1]);

            t = PotionEffectType.getByName(name);

            if(t == null)
            {
                plugin.getLogger().warning("Invalid potion effect: " + s);
                continue;
            }


            potionEffectsList.add(new PotionEffect(t, PotionEffect.INFINITE_DURATION, level));

        }

        return potionEffectsList;
    }

    /**
     * Does not check if the coords are in the border.
     * User's responsability for now
     * @param s the encoded string
     * @return if the coords are valid integers
     */
    private boolean validCoords(String s)
    {

        String[] arr = s.split(":");

        for(String x : arr)
        {

            if(!Utils.isInt(x))
            {
                plugin.getLogger().warning("Invalid location: " + s);
                return false;
            }

        }

        return true;

    }

    private ArrayList<Location> deserializeLocations(List<String> coords)
    {

        ArrayList<Location> locations = new ArrayList<>();

        if(Bukkit.getWorld(getWorldName()) == null)
        {
            return locations;
        }

        String[] arr;

        int x, y, z;

        for(String c : coords)
        {

            if(validCoords(c))
            {
                arr = c.split(":");
                x = Integer.parseInt(arr[0]);
                y = Integer.parseInt(arr[1]);
                z = Integer.parseInt(arr[2]);

                locations.add(new Location(Bukkit.getWorld(getWorldName()), x, y, z));

            }

        }

        return locations;

    }

    /**
     * Fetches all the data from the treasure file
     * @param defaultSection the treasure's path
     */
    public TreasureData(ConfigurationSection defaultSection) {

        if (defaultSection == null) {
            throw new IllegalStateException("Default treasure configuration not found.");
        }

        this.worldName = defaultSection.getString("world-name", "world");
        this.treasureName = defaultSection.getString("treasure-name", "Mysterious Treasure");
        this.treasureBlockString = defaultSection.getString("treasure-block", "ENDER_CHEST");
        this.treasureAnimation = defaultSection.getString("treasure-animation", "protection");
        this.fallFromTheSky = defaultSection.getBoolean("fall-from-the-sky", true);
        this.treasureIcon = Material.valueOf(defaultSection.getString("treasure-icon", "EMERALD"));
        this.requireAllMobsDead = defaultSection.getBoolean("require-all-mobs-dead", true);
        this.flareType = defaultSection.getString("flare-type", "many");
        this.fireworks = defaultSection.getBoolean("fireworks", true);
        this.maxTreasureDistance = defaultSection.getInt("max-treasure-distance", 15000);
        this.delay = defaultSection.getInt("delay", 200);
        this.chanceForTreasure = defaultSection.getInt("chance-for-treasure", 20);
        this.rewardAllPlayersWhoParticipated = defaultSection.getBoolean("reward-all-players-who-participated", false);
        this.dropItemsOnGround = defaultSection.getBoolean("drop-items-on-ground", false);
        this.distanceFromPlayerToSpawnMobs = defaultSection.getInt("distance-from-player-to-spawn-mobs", 0);
        this.mobWanderingDistance = defaultSection.getInt("mob-wandering-distance", 40);
        this.enableMobTracker = defaultSection.getBoolean("enable-mob-tracker", true);
        this.animateMobSpawning = defaultSection.getBoolean("animate-mob-spawning", true);
        this.interval = defaultSection.getInt("interval", 30);
        this.duration = defaultSection.getInt("duration", 20);
        this.cooldown = defaultSection.getInt("cooldown", 15);
        this.clicksToOpen = defaultSection.getInt("clicks-to-open", 1);
        this.droppedItemName = defaultSection.getString("dropped-item-name", "&6Treasure Loot");
        this.potionEffects = deserializeEffects(defaultSection.getStringList("potion-effects"));
        this.spawnToCertainCoords = defaultSection.getBoolean("spawn-to-certain-coords", false);
        this.spawnCoords = deserializeLocations(defaultSection.getStringList("spawn-coords"));

        try
        {
            String particleStr = defaultSection.getString("treasure-particles", "COMPOSTER");
            this.treasureParticles = Particle.valueOf(particleStr);

            String flareParticle = defaultSection.getString("flare-particle", "COMPOSTER");
            this.flareParticle = Particle.valueOf(flareParticle);
        }
        catch (Exception e)
        {
            plugin.getLogger().warning("Invalid treasure particles or flare particles: " + e.getMessage());
        }

        setTreasureType(fetchTreasureBlockType());

        // Load TreasureKey
        ConfigurationSection keySection = defaultSection.getConfigurationSection("treasure-key");

        if (keySection != null) {


            String itemStr = keySection.getString("item", "TRIPWIRE_HOOK");
            Material mat = Material.matchMaterial(itemStr);

            if (mat == null) {
                plugin.getLogger().warning("Material '" + itemStr + "' does not exist!");
                mat = Material.STONE;
            }

            this.treasureKey = new TreasureKey(

                    keySection.getBoolean("enabled", false),


                    mat,
                    keySection.getString("name", "&c&l&oTREASURE KEY"),
                    keySection.getStringList("lore")
            );

        }
        else
        {
            plugin.getLogger().warning("Configuration section 'treasure-key' does not exist!");
        }


        // Load all Mobs
        ConfigurationSection mobsSection = defaultSection.getConfigurationSection("mobs");

        if (mobsSection != null) {

            Set<String> mobKeys = mobsSection.getKeys(false);

            for (String mobId : mobKeys) {

                ConfigurationSection mobSection = mobsSection.getConfigurationSection(mobId);

                if (mobSection != null) {

                    TreasureKeeper mob = new TreasureKeeper(this);

                    mob.setMobId(mobId);
                    mob.setMythicMob(mobSection.getBoolean("mythic-mobs", false));
                    mob.setKeeperIdentifier(mobSection.getString("entity-type", "ZOMBIE"));
                    mob.setCustomName(mobSection.getString("custom-name", "Treasure Keeper"));
                    mob.setAmount(getAmountFromRange(mobSection.getString("range", "5-10")));
                    mob.setChance(mobSection.getInt("chance", 100));
                    mob.setMaxHealth(mobSection.getInt("max-health", 20));
                    mob.setPotionEffects(deserializeEffects(mobSection.getStringList("potion-effects")));
                    mob.setAnimatedSpawn(animateMobSpawning());

                    TreasureKeeperDrops drops = new TreasureKeeperDrops(mobSection.getConfigurationSection("drops"));
                    mob.setDrops(drops);

                    TreasureKeeperEquipment equipment = new TreasureKeeperEquipment(mobSection.getConfigurationSection("equipment"));
                    mob.setEquipment(equipment);

                    treasureKeepers.add(mob);
                }
                else
                {
                    plugin.getLogger().warning("Configuration section '" + mobId + "' does not exist!");
                }
            }
        }

        // Load ItemRewards
        ConfigurationSection itemRewardsSection = defaultSection.getConfigurationSection("item-rewards");

        if (itemRewardsSection != null) {

            Set<String> rewardKeys = itemRewardsSection.getKeys(false);

            for (String rewardId : rewardKeys) {

                ConfigurationSection rewardSection = itemRewardsSection.getConfigurationSection(rewardId);

                if (rewardSection != null) {

                    ItemReward reward = new ItemReward();
                    reward.setIdentifier(rewardId);

                    String itemStr = rewardSection.getString("item", "STONE");
                    Material mat = Material.matchMaterial(itemStr);

                    if(mat == null)
                    {
                        plugin.getLogger().warning("Material '" + itemStr + "' does not exist!");
                        mat = Material.STONE;
                    }

                    reward.setItem(mat);
                    reward.setName(rewardSection.getString("name", "&6&l&oREWARD #1"));
                    reward.setLore(rewardSection.getStringList("lore"));
                    reward.setAmount(getAmountFromRange(rewardSection.getString("amount", "5-10")));
                    reward.setChance(rewardSection.getInt("chance", 40));
                    itemRewards.add(reward);

                }
                else
                {
                    plugin.getLogger().warning("Configuration section '" + rewardSection + "' does not exist!");
                }
            }
        }
        else
        {
            plugin.getLogger().warning("Configuration section '" + itemRewardsSection + "' does not exist!");
        }

        // Load CommandRewards
        ConfigurationSection commandRewardsSection = defaultSection.getConfigurationSection("command-rewards");
        if (commandRewardsSection != null) {

            Set<String> commandKeys = commandRewardsSection.getKeys(false);

            for (String commandId : commandKeys) {

                ConfigurationSection commandSection = commandRewardsSection.getConfigurationSection(commandId);

                if (commandSection != null) {

                    CommandReward cr = new CommandReward();
                    cr.setIdentifier(commandId);
                    cr.setCommand(commandSection.getString("command"));
                    cr.setChance(commandSection.getInt("chance"));
                    commandRewards.add(cr);

                }
                else
                {
                    plugin.getLogger().warning("Configuration section '" + commandId + "' does not exist!");
                }
            }
        }
        else
        {
            plugin.getLogger().warning("Configuration section 'command-rewards' does not exist!");
        }

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

    public int getInterval() {
        return interval;
    }

    public int getDuration() {
        return duration;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getClicksToOpen() {
        return clicksToOpen;
    }

    public String getDroppedItemName() {
        return droppedItemName;
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public boolean spawnToCertainCoords() {
        return spawnToCertainCoords;
    }

    public List<Location> getSpawnCoords() {
        return spawnCoords;
    }

}
