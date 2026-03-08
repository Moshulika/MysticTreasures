package com.Moshu.TreasureHunt.Components.Keepers;

import com.Moshu.Misc.Storage.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Core.Treasure;
import com.Moshu.TreasureHunt.Components.TreasureData;
import com.Moshu.TreasureHunt.Core.Hunt;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TreasureKeeper {

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private MythicMob mythicMob;
    private EntityType entityType;
    private int amount;
    private TreasureKeeperDrops drops;
    private TreasureKeeperEquipment equipment;

    private String mobId;  // The key name (e.g., "zombie")
    private boolean isMythicMob;
    private String customName;
    private int chance;
    private int maxHealth;
    private String keeperIdentifier;
    private List<PotionEffect> potionEffects;
    private boolean animatedSpawn;
    private boolean isSpawned;
    private final TreasureData t;
    private final ArrayList<UUID> uuids = new ArrayList<>();
    private List<Integer> rounds;
    private String menuItem;
    private String range;

    public TreasureKeeper(TreasureData t) {
        this.t = t;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(String menuItem) {
        this.menuItem = menuItem;
    }

    public void setDrops(TreasureKeeperDrops drops) {
        this.drops = drops;
    }

    public List<Integer> getRounds() {
        return rounds;
    }

    public void setRounds(List<String> rounds) {

        ArrayList<Integer> roundsList = new ArrayList<>();

        for(String s : rounds)
        {
            if(Utils.isInt(s)) roundsList.add(Integer.parseInt(s));
        }

        this.rounds = roundsList;
    }

    public void setEquipment(TreasureKeeperEquipment equipment) {
        this.equipment = equipment;
    }

    public TreasureKeeperDrops getDrops() {
        return drops;
    }

    public TreasureKeeperEquipment getEquipment() {
        return equipment;
    }

    public TreasureData getTreasureData()
    {
        return t;
    }

    public boolean isSpawned()
    {
        return isSpawned;
    }

    private void setSpawned(boolean spawned)
    {
        this.isSpawned = spawned;
    }

    public void setAnimatedSpawn(boolean animatedSpawn) {
        this.animatedSpawn = animatedSpawn;
    }

    public boolean isAnimatedSpawn() {
        return animatedSpawn;
    }

    public int getAmount() {
        return amount;
    }

    public String getMobId() {
        return mobId;
    }

    public void setMobId(String mobId) {
        this.mobId = mobId;
    }

    public boolean isMythicMob() {
        return isMythicMob;
    }

    public void setMythicMob(boolean isMythicMob) {
        this.isMythicMob = isMythicMob;
    }

    public MythicMob getMythicMob()
    {
        return mythicMob;
    }

    public void loadMythicMob(MythicMob mythicMob)
    {
        this.mythicMob = mythicMob;
    }

    public void loadEntityType(EntityType type)
    {
        this.entityType = type;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    private MythicMob fetchMythicMob(String s)
    {
        MythicMob mythicmob = MythicBukkit.inst().getMobManager().getMythicMob(s).orElse(null);
        if(mythicmob == null) plugin.getLogger().warning("Mythic Mob not found: " + s);

        loadMythicMob(mythicMob);
        return mythicmob;
    }

    private EntityType fetchEntityType(String s)
    {

        try
        {
            EntityType type = EntityType.valueOf(s);
            loadEntityType(type);
            return type;

        }
        catch (IllegalArgumentException e)
        {
            plugin.getLogger().warning("Vanilla EntityType not found: " + s);
            return EntityType.ZOMBIE;
        }

    }

    public void addUUID(UUID uuid)
    {
        uuids.add(uuid);
    }

    public ArrayList<UUID> getUUIDs()
    {
        return uuids;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public void setKeeperIdentifier(String s) {
        this.keeperIdentifier = s;
        decodeKeeperIdentifier();
    }

    public String getKeeperIdentifier() {
        return this.keeperIdentifier;
    }

    private Location getNearLocationInside(Location loc)
    {
        int x = Utils.randInt(-6, 6);
        int z = Utils.randInt(-6, 6);

        Location randomLoc = loc.clone().add(x, 0, z);

        return Utils.getSafeBlock(randomLoc, loc.getWorld().getSpawnLocation());
    }

    private Location getNearLocation(Location l)
    {
        int x = Utils.randInt(-6, 6);
        int z = Utils.randInt(-6, 6);

        return Utils.getHighestBlock(l.getWorld(), l.getBlockX(), l.getBlockZ(), l.getWorld().getSpawnLocation()).add(x, 0, z);
    }

    private void equip(LivingEntity e)
    {

        EntityEquipment eq = e.getEquipment();

        if(eq == null) return;

        for(TreasureKeeperEquipment.EquipmentData d : getEquipment().getAllEquipment().values())
        {
            eq.setItem(d.getSlot(), d.getItemStack());
        }

        for(PotionEffect p : getPotionEffects())
        {
            e.addPotionEffect(p);
        }

    }

    public static boolean isTreasureKeeper(LivingEntity e)
    {

        return e.hasMetadata("treasure-mob-" + e.getWorld().getName());

    }

    public static String getTreasureIdentifier(LivingEntity e)
    {

        for(String s : TreasureData.getTreasureIdentifiers())
        {
            if(e.hasMetadata("treasure-mob-" + s)) return s;
        }

        plugin.getLogger().warning("Treasure ID not found for " + e.getName());
        return "Invalid";

    }

    public static Hunt getHunt(LivingEntity e)
    {
        if (e.hasMetadata("treasure-hunt-id")) {
            String uuidStr = e.getMetadata("treasure-hunt-id").get(0).asString();
            try {
                return Hunt.getHuntById(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ex) {
                return Hunt.getHuntByIdentifier(getTreasureIdentifier(e));
            }
        }
        return Hunt.getHuntByIdentifier(getTreasureIdentifier(e));
    }

    private Location pickLocation(boolean spawnsInside, Location originalLoc)
    {

        if(spawnsInside)
        {
            return getNearLocationInside(originalLoc);
        }
        else
        {
            return getNearLocation(originalLoc);
        }

    }

    public void spawn(ArrayList<Entity> spawnedEntityRegister, Treasure t)
    {

        boolean spawnsInside = getTreasureData().isSpawnsInside() || getTreasureData().spawnToCertainCoords();
        Location loc = t.getLocation();
        Hunt h = t.getHunt();

        if(Utils.chance() < chance)
        {

            if(isMythicMob()) {

                fetchMythicMob(getKeeperIdentifier());
                MythicMob mob = getMythicMob();

                if (mob != null) {

                    for(int i = 0; i < getAmount(); i++) {

                        ActiveMob knight = mob.spawn(BukkitAdapter.adapt(pickLocation(spawnsInside, loc)), 1);
                        Entity entity = knight.getEntity().getBukkitEntity();
                        entity.setMetadata("treasure-mob-" + loc.getWorld().getName(), new FixedMetadataValue(plugin, "treasure-mob-" + loc.getWorld().getName()));
                        entity.setMetadata("treasure-mob-" + getTreasureData().getIdentifier(), new FixedMetadataValue(plugin, "treasure-mob-" + getTreasureData().getIdentifier()));
                        entity.setMetadata("treasure-hunt-id", new FixedMetadataValue(plugin, h.getHuntId().toString()));

                        if (isAnimatedSpawn()) smoothEntitySpawnFromGrave(entity);

                        addUUID(entity.getUniqueId());
                        spawnedEntityRegister.add(entity);

                    }
                }
                else
                {
                    plugin.getLogger().warning("Mythic Mob not found in your configuration. Make sure if you don't use MythicMobs to set the config option to false!");
                }


            }
            else
            {

                fetchEntityType(getKeeperIdentifier());

                for(int i = 0; i < getAmount(); i++) {


                    LivingEntity e = (LivingEntity) loc.getWorld().spawnEntity(pickLocation(spawnsInside, loc), getEntityType());
                    e.setMetadata("treasure-mob-" + loc.getWorld().getName(), new FixedMetadataValue(plugin, "treasure-mob-" + loc.getWorld().getName()));
                    e.setMetadata("treasure-keeper-" + getMobId(), new FixedMetadataValue(plugin, "treasure-keeper-" + getMobId()));
                    e.setMetadata("treasure-mob-" + getTreasureData().getIdentifier(), new FixedMetadataValue(plugin, "treasure-mob-" + getTreasureData().getIdentifier()));
                    e.setMetadata("treasure-hunt-id", new FixedMetadataValue(plugin, h.getHuntId().toString()));

                    e.addScoreboardTag("treasureKeeper");

                    e.setRemoveWhenFarAway(false);

                    e.setMaxHealth(getMaxHealth());
                    e.setHealth(getMaxHealth());
                    e.setCustomName(Utils.format(getCustomName()));
                    e.setCustomNameVisible(true);

                    equip(e);

                    if (isAnimatedSpawn()) smoothEntitySpawnFromGrave(e);

                    addUUID(e.getUniqueId());
                    spawnedEntityRegister.add(e);

                }

            }

            setSpawned(true);

        }

    }

    /**
     * Decodes the String into a valid (hopefully)
     * EntityType or MythicMob
     */
    private void decodeKeeperIdentifier()
    {

        boolean mythicsEnabled = Utils.isEnabled("MythicMobs");

        if(mythicsEnabled) {

            if (isMythicMob()) {

                loadMythicMob(fetchMythicMob(keeperIdentifier));
                loadEntityType(null);
                return;
            }

        }

        loadEntityType(fetchEntityType(keeperIdentifier));
        loadMythicMob(null);

    }

    private final ArrayList<Entity> spawningEntities = new ArrayList<>();
    private void smoothEntitySpawnFromGrave(final Entity ent) {

        final Location particleLocation = ent.getLocation();
        final Location entLoc = particleLocation.clone();
        final Entity passenger = ent.getPassenger();

        if (!entLoc.clone().add(0.0, -1.0, 0.0).getBlock().getType().isSolid() || entLoc.getBlock().getType().toString().contains("WATER")) {
            if (!entLoc.clone().add(0.0, -1.0, 0.0).getBlock().getType().isAir())
            {
                return;
            }
            else
            {
                entLoc.add(0.0, -3.0, 0.0);
            }
        }
        else
        {
            entLoc.add(0.0, -2.0, 0.0);
        }

        spawningEntities.add(ent);
        ent.teleport(entLoc);

        ent.setInvulnerable(true);

        Block blockUnderEntity = particleLocation.clone().add(0.0, -1.0, 0.0).getBlock();
        final Material particleMaterial = blockUnderEntity.getType();
        final float step = 1.0f / 85f * 2.0f;

        BukkitRunnable run = new BukkitRunnable() {

            public void run() {

                if (ent.isDead() || !ent.isValid() || !entLoc.getChunk().isLoaded()) {

                    if (passenger != null) {
                        ent.setPassenger(passenger);
                    }

                    spawningEntities.remove(ent);
                    ent.remove();
                    this.cancel();
                    return;
                }

                if (entLoc.getBlock().getType().isSolid() || entLoc.clone().add(0.0, 1.0, 0.0).getBlock().getType().isSolid()) {

                    spawnGraveParticles(entLoc.getWorld(), entLoc, particleMaterial);

                } else {

                    if (passenger != null) {
                        ent.setPassenger(passenger);
                    }

                    ent.setInvulnerable(false);
                    spawningEntities.remove(ent);
                    this.cancel();
                    return;
                }

                entLoc.add(0.0, step, 0.0);
                ent.teleport(entLoc);
            }
        };

        run.runTaskTimer(plugin, 1L, 1L);

    }

    private static final Particle LAVA = Settings.getCompatParticle("lava");
    private static final Particle SOUL = Settings.getCompatParticle("soul");
    private static final Particle SOUL_FIRE_FLAME = Settings.getCompatParticle("soul-fire-flame");

    private void spawnGraveParticles(World w, Location entLoc, Material particleMaterial)
    {
        w.playEffect(entLoc, Effect.STEP_SOUND, particleMaterial);
        w.spawnParticle(LAVA, entLoc, 1);
        w.spawnParticle(SOUL, entLoc, 1);
        w.spawnParticle(SOUL_FIRE_FLAME, entLoc, 1);
    }


}
