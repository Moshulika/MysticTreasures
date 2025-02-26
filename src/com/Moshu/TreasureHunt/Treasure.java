package com.Moshu.TreasureHunt;

import com.Moshu.Misc.*;
import com.google.common.base.Joiner;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.data.HologramData;
import de.oliver.fancyholograms.api.data.TextHologramData;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomEntity;
import dev.lone.itemsadder.api.CustomFurniture;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.th0rgal.oraxen.api.OraxenFurniture;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Treasure {

    public enum TreasureType
    {

        VANILLA,
        ENTITY,
        BLOCK,
        FURNITURE,
        ORAXEN_FURNITURE,
        OTHER

    }


    private Location l;
    private final HashMap<ItemStack, Double> items;
    private final Hunt h;
    private boolean isactive;
    private TreasureType type;
    private Entity furnitureEntity;

    private boolean spawned = false;
    private String alias = "Hidden Treasure";

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private static final Particle EXPLOSION = Settings.getCompatParticle("treasure-spawn-particle");
    private static final Particle EXPLOSION_EMITTER = Settings.getCompatParticle("treasure-remove-particle");
    private static final Particle CAMPFIRE_SIGNAL_SMOKE = Settings.getCompatParticle("treasure-fall-particle");
    private static final Particle LAVA = Settings.getCompatParticle("lava");
    private static final Particle SOUL = Settings.getCompatParticle("soul");
    private static final Particle SOUL_FIRE_FLAME = Settings.getCompatParticle("soul-fire-flame");
    private static final Particle WARPED_SPORE = Settings.getCompatParticle("warped-spore");

    Treasure(Hunt h, HashMap<ItemStack, Double> items)
    {

        this.h = h;
        this.l = h.getLocation();
        this.items = items;
        this.isactive = false;
        this.type = TreasureType.VANILLA;

    }

    private final ArrayList<Player> participants = new ArrayList<>();

    public void setAlias(String s)
    {
        alias = s;
    }

    public String getAlias()
    {
        return alias;
    }

    public TreasureType getType()
    {
        return type;
    }

    public void setType(TreasureType type)
    {
        this.type = type;
    }

    public Location getLocation()
    {
        return l;
    }

    public HashMap<ItemStack, Double> getItems()
    {
        return items;
    }

    public boolean haveTheMobsSpawned()
    {
        return spawned;
    }

    public boolean isActive()
    {
        return isactive;
    }

    public ArrayList<Player> getParticipants()
    {
        return participants;
    }

    public ArrayList<String> getParticipantsNames()
    {
        ArrayList<String> names = new ArrayList<>();

        participants.forEach(p -> names.add(p.getName()));
        return names;
    }

    public void addParticipant(Player p)
    {
        participants.add(p);
    }

    public void clearParticipants()
    {
        participants.clear();
    }

    public static boolean isTreasure(Location loc)
    {

        for(Hunt h : Hunt.getActiveHunts())
        {

            if(h.getTreasure().getLocation().getWorld() == loc.getWorld()
                    && h.getTreasure().getLocation().getBlockX() == loc.getBlockX()
                    && h.getTreasure().getLocation().getBlockZ() == loc.getBlockZ()
                    && h.getTreasure().getLocation().getBlockY() == loc.getBlockY())
            {
                return true;
            }

        }

        return false;

    }

    public static Treasure getTreasure(Location loc)
    {

        for(Hunt h : Hunt.getActiveHunts())
        {

            if(isTreasure(loc))
            {
                return h.getTreasure();
            }

        }

        return null;

    }

    public Location getNearLocation()
    {

        int x = Utils.randInt(-6, 6);
        int z = Utils.randInt(-6, 6);

        return Utils.getHighestBlock(l.getWorld(), l.getBlockX(), l.getBlockZ(), l.getWorld().getSpawnLocation()).add(x, 0, z);

    }

    private static final HashMap<String, ArrayList<String>> command_rewards = new HashMap<>();

    public ArrayList<String> getCommandRewards()
    {
        return command_rewards.get(l.getWorld().getName());
    }

    public static void loadCommands()
    {

        try {

            String world_name;

            //world isn'r real world name, only config name
            for (String world : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false)) {

                ArrayList<String> local_commands = new ArrayList<>();

                world_name = plugin.getConfig().getString("settings.enabled-worlds." + world + ".world-name");
                local_commands.addAll(plugin.getConfig().getStringList("settings.enabled-worlds." + world + ".command-rewards"));

                command_rewards.put(world_name, local_commands);
            }

        }
        catch (IllegalArgumentException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Invalid entity name in treasure configuration: " + e.getMessage());
        }


    }

    private static final HashMap<String, ArrayList<TreasureKeeper>> mobs = new HashMap<>();

    public ArrayList<TreasureKeeper> getMobs(World w)
    {
        return mobs.get(w.getName());
    }

    public static void loadMobs()
    {

        String[] args;
        EntityType mob;
        int amount;

        String world_name;
        boolean mythicsEnabled = Utils.isEnabled("MythicMobs");

        try {

            for (String world : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false)) {

                world_name = plugin.getConfig().getString("settings.enabled-worlds." + world + ".world-name");
                ArrayList<TreasureKeeper> local_mobs = new ArrayList<>();

                for (String s : plugin.getConfig().getStringList("settings.enabled-worlds." + world + ".mobs")) {

                    args = s.split(":");

                    if (args.length < 2) {
                        plugin.getLogger().log(Level.SEVERE, "Invalid mob in treasure configuration: " + s);
                        continue;
                    }

                    if(!Utils.isInt(args[1]))
                    {
                        //DIAMOND:5-10
                        if(args[1].split("-").length == 2)
                        {

                            int min = Integer.parseInt(args[1].split("-")[0]);
                            int max = Integer.parseInt(args[1].split("-")[1]);

                            amount = Utils.randInt(min, max);

                            if(amount <= 0) continue;

                        }
                        else {
                            plugin.getLogger().log(Level.SEVERE, "Invalid amount in treasure prize configuration: " + args[1]);
                            continue;
                        }

                    }
                    else {
                        amount = Integer.parseInt(args[1]);
                    }

                    if(mythicsEnabled) {

                        MythicMob mythicmob = MythicBukkit.inst().getMobManager().getMythicMob(args[0]).orElse(null);

                        if(mythicmob != null) {
                            local_mobs.add(new TreasureKeeper(mythicmob, amount));
                        }
                        else
                        {
                            mob = EntityType.valueOf(args[0]);
                            local_mobs.add(new TreasureKeeper(mob, amount));
                        }

                    }
                    else
                    {
                        mob = EntityType.valueOf(args[0]);
                        local_mobs.add(new TreasureKeeper(mob, amount));
                    }

                }

                mobs.put(world_name, local_mobs);
            }

        }
        catch (IllegalArgumentException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Invalid entity name in treasure configuration: " + e.getMessage());
        }


    }

    ArrayList<Entity> spawnEntities = new ArrayList<>();

    public boolean mobsCleared()
    {
        for(Entity e : spawnEntities)
        {
            if(!e.isDead()) return false;
        }

        return true;
    }

    public ArrayList<Entity> getRemainingMobs()
    {

        ArrayList<Entity> entities = new ArrayList<>();

        for(Entity e : spawnEntities)
        {

            if(!e.isDead()) entities.add(e);

        }

        return entities;


    }

    public int remainingMobs()
    {

        int i = 0;

        for(Entity e : spawnEntities)
        {
            i++;
        }

        return i;

    }

    public void spawnMobs(boolean animate)
    {

        LivingEntity e;
        boolean animated = Settings.getWorldBooleanUnknown(l.getWorld().getName(), "animate-mob-spawning") && animate;

        try
        {

            boolean mythicsEnabled = Utils.isEnabled("MythicMobs");

            for(TreasureKeeper t : getMobs(l.getWorld()))
            {

                for(int i = 0; i < t.getAmount(); i++) {

                    if(mythicsEnabled)
                    {

                        MythicMob mob = t.getMythicMob();

                        if(t.isMythicMob())
                        {

                            if(mob != null) {
                                ActiveMob knight = mob.spawn(BukkitAdapter.adapt(getNearLocation()),1);
                                Entity entity = knight.getEntity().getBukkitEntity();
                                entity.setMetadata("treasure-mob-" + l.getWorld().getName(), new FixedMetadataValue(plugin, "treasure-mob-" + l.getWorld().getName()));

                                if(animated) smoothEntitySpawnFromGrave(entity);

                                spawnEntities.add(entity);
                            }

                        }
                        else
                        {
                            e = (LivingEntity) l.getWorld().spawnEntity(getNearLocation(), t.getType());
                            e.setMetadata("treasure-mob-" + l.getWorld().getName(), new FixedMetadataValue(plugin, "treasure-mob-" + l.getWorld().getName()));
                            e.setRemoveWhenFarAway(false);

                            if(animated) smoothEntitySpawnFromGrave(e);

                            spawnEntities.add(e);
                        }

                    }
                    else
                    {

                        e = (LivingEntity) l.getWorld().spawnEntity(getNearLocation(), t.getType());
                        e.setMetadata("treasure-mob-" + l.getWorld().getName(), new FixedMetadataValue(plugin, "treasure-mob-" + l.getWorld().getName()));
                        e.setRemoveWhenFarAway(false);

                        if(animated) smoothEntitySpawnFromGrave(e);

                        spawnEntities.add(e);

                    }



                }
            }

            spawned = true;

        }
        catch (Exception ex)
        {
            plugin.getLogger().log(Level.SEVERE, "Invalid mob in treasure configuration: " + ex.getMessage());
        }

    }

    public boolean isTreasureKeeper(LivingEntity e)
    {
        return spawnEntities.contains(e) || e.hasMetadata("treasure-mob-" + l.getWorld().getName());
    }

    public void hologram()
    {
        
        Location loc = getLocation().clone();

        if(Utils.isEnabled("DecentHolograms")) {


            if(DHAPI.getHologram("treasurehunt_" + getLocation().getWorld().getName()) != null)
            {
                DHAPI.getHologram("treasurehunt_" + getLocation().getWorld().getName()).delete();
            }

            DHAPI.createHologram("treasurehunt_" + loc.getWorld().getName(), loc.clone().add(0.5, 1.5, 0.5), false).setDownOrigin(true);
            Hologram h = DHAPI.getHologram("treasurehunt_" + loc.getWorld().getName());

            ArrayList<String> lines = new ArrayList<>();

            for (String s : Messages.getAndFormatList("messages.treasure-hologram")) {
                lines.add(s.replace("{time}", Utils.getCountDown(Hunt.getHunt(loc.getWorld()).getRemainingTime())));
            }

            h.enable();
            h.setUpdateInterval(20);

            DHAPI.setHologramLines(h, lines);
            h.updateAll();

        }
        else if(Utils.isEnabled("FancyHolograms"))
        {

            if(FancyHologramsPlugin.get().getHologramManager().getHologram("treasurehunt_" + loc.getWorld().getName()).isPresent())
            {
                HologramHandler.getInstance().delete(getLocation());
            }

            HologramHandler handler = HologramHandler.getInstance();

            handler.createFancyHologram(loc);
        }
        else
        {
            createItem(getItemLocation(loc));
        }

    }

    public void flare()
    {

            String flare = Settings.getWorldStringUnknown(l.getWorld().getName(), "flare-type");
            int refresh = flare.equalsIgnoreCase("few") ? 20 : 2;
            Location loc = getLocation().clone();

            if (!flare.equalsIgnoreCase("none")) {

                BukkitRunnable run = new BukkitRunnable() {

                    final Particle p = Settings.getWorldParticleUnknown(loc.getWorld().getName(), "flare-particle");

                    @Override
                    public void run() {

                        if(!isActive() || (Hunt.getHunt(l.getWorld()) == null) || (Hunt.getHunt(l.getWorld()) != null && !Hunt.isActive(l.getWorld())))
                        {
                            this.cancel();
                            return;
                        }

                        if (flare.equalsIgnoreCase("few")) {

                            for (int i = 0; i < 15; i++) {
                                loc.getWorld().spawnParticle(p, Utils.getParticleLocation(loc).add(0, i, 0), 1);
                            }

                        } else {

                            for (int i = 0; i < 50; i++) {
                                loc.getWorld().spawnParticle(p, Utils.getParticleLocation(loc).add(0, i, 0), 1);
                            }

                        }

                    }

                };

                run.runTaskTimerAsynchronously(plugin, 0, refresh);

            }

    }

    public void enableEffects()
    {
        boolean mobs = Settings.getWorldBooleanUnknown(l.getWorld().getName(), "require-all-mobs-dead");
        String anim = Settings.getWorldStringUnknown(l.getWorld().getName(), "treasure-animation");

        if (mobs) {
            if (anim.equalsIgnoreCase("protection")) Effects.runCircle(l);
        } else {

            if (anim.equalsIgnoreCase("orb")) {
                Effects.runOrbs(l);
            } else if (anim.equalsIgnoreCase("spiral")) {
                Effects.createDoubleSpiral(l);
            }

        }

    }

    private void spawnTreasure() {

        Bukkit.getScheduler().runTask(plugin, () ->
        {

            Location location;

            Location temp_location = getLocation().clone();
            World w = temp_location.getWorld();

            Particle part = Settings.getWorldParticleUnknown(w.getName(), "treasure-particles");
            int distance_to_spawn = Settings.getWorldIntUnknown(w.getName(), "distance-from-player-to-spawn-mobs");

            if(temp_location.getBlock().getType().isSolid())
            {
                location = temp_location.clone().add(0, 1, 0);
                this.l = location;
            }
            else location = temp_location.clone();

            placeTreasureBlock(w.getName(), location);

            Bukkit.getScheduler().runTaskLater(plugin, () ->
            {

                if(distance_to_spawn <= 0)
                {
                    spawnMobs(false);
                }

            }, 1);

            this.isactive = true;
            
            tickTreasure(location, part, distance_to_spawn);
            announceSpawnedTreasure();

        });


    }

    private void tickTreasure(Location location, Particle part, int distance_to_spawn)
    {

        World w = location.getWorld();
        int wandering_distance = Settings.getWorldIntUnknown(w.getName(), "mob-wandering-distance");

        String unlocked =  Messages.get("treasure-unlocked");
        String locked =  Messages.get("treasure-locked");

        BukkitRunnable run = new BukkitRunnable() {

            @Override
            public void run() {

                try {

                    if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - h.getStartTime()) >= h.getDuration()) {
                        Bukkit.getScheduler().runTask(plugin, () -> remove());
                    }

                    if (isActive()) {

                        w.spawnParticle(part, Utils.getParticleLocation(location), 3);
                        w.spawnParticle(part, Utils.getParticleLocation(location), 3);

                        ArrayList<String> lines = new ArrayList<>();

                        for (String s : Messages.getAndFormatList("messages.treasure-hologram")) {
                            lines.add(s
                                    .replace("{time}", Utils.getCountDown(Hunt.getHunt(w).getRemainingTime()))
                                    .replace("{status}", getRemainingMobs().isEmpty() ? unlocked : locked)
                                    .replace("{alias}", getAlias())
                                    .replace("{remaining_mobs}", getRemainingMobs().size() + ""));
                        }

                        if (Utils.isEnabled("DecentHolograms")) {

                            if (DHAPI.getHologram("treasurehunt_" + w.getName()) != null) {

                                Hologram h = DHAPI.getHologram("treasurehunt_" + w.getName());

                                Bukkit.getScheduler().runTask(plugin, ()->
                                {
                                    DHAPI.setHologramLines(h, lines);
                                    h.updateAll();
                                });

                            }

                        }

                        else if(Utils.isEnabled("FancyHolograms"))
                        {

                            HologramHandler.getInstance().update(w, lines);

                        }

                        if(distance_to_spawn > 0 && !Utils.getNearbyPlayers(location, distance_to_spawn).isEmpty() && !spawned) {
                            Bukkit.getScheduler().runTaskLater(plugin, () ->
                            {
                                w.strikeLightningEffect(location);
                                spawnMobs(true);
                                enableEffects();
                                spawned = true;
                            }, 1);
                        }

                        for(Entity e : getRemainingMobs())
                        {

                            if(e.getLocation().distance(location) > wandering_distance)
                            {

                                Bukkit.getScheduler().runTask(plugin, ()->
                                {
                                    e.teleport(getNearLocation());
                                });
                            }

                        }


                    } else {
                        Hunt.getHunts().remove(h);
                        Bukkit.getScheduler().runTask(plugin, () -> l.getChunk().setForceLoaded(false));
                        this.cancel();
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

        };

        run.runTaskTimerAsynchronously(plugin, 0, 5);
    }

    private void animate(Location location, Entity e, ItemStack is)
    {

        if(e instanceof ArmorStand animation) {

            animation.setMetadata("treasure_stand", new FixedMetadataValue(plugin, "treasure_stand"));
            animation.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 600, 1)); //Doesn't exist < 1.13
            animation.setGravity(true);

            if (Utils.isPaper()) {
                animation.setCanMove(true);
                animation.setCanTick(true);
            }

            animation.setBasePlate(false);
            animation.setHelmet(is);
            animation.setInvulnerable(true);
            animation.setVisible(false);

        }

        BukkitRunnable run_falling_particles = new BukkitRunnable() {

            @Override
            public void run() {

                if (e.isOnGround()) {

                    Bukkit.getScheduler().runTask(plugin, e::remove);
                    location.getWorld().spawnParticle(EXPLOSION, e.getLocation(), 1);

                    spawnTreasure();
                    this.cancel();
                    return;
                }

                //Daca iar nu merge de aici era
                location.getWorld().spawnParticle(CAMPFIRE_SIGNAL_SMOKE, e.getLocation().add(0, 3, 0), 1);

            }
        };

        run_falling_particles.runTaskTimerAsynchronously(plugin, 0, 1);

    }

    private void runAnimation(Location location)
    {

        String s = Settings.getWorldStringUnknown(location.getWorld().getName(), "treasure-block");

        if(getType() == TreasureType.VANILLA) {

            if(Settings.getWorldBooleanUnknown(location.getWorld().getName(), "fall-from-the-sky"))
            {
                ArmorStand animation = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, 50, 0), EntityType.ARMOR_STAND);
                ItemStack is = new ItemStack(Utils.checkMaterial(s));

                animate(location, animation, is);
            }
            else
            {
                location.getWorld().spawnParticle(EXPLOSION_EMITTER, location, 1);
                spawnTreasure();
            }

        }
        else
        {
            location.getWorld().spawnParticle(EXPLOSION_EMITTER, location, 1);
            spawnTreasure();
        }

    }

    public void create() {

        Location location = getLocation().clone();
        World w = location.getWorld();
        int delay = Settings.getWorldIntUnknown(w.getName(), "delay");

        announceUpcomingTreasure(delay);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->
        {

            location.getChunk().load();
            location.getChunk().setForceLoaded(true);

            setType(fetchTreasureType(w.getName()));

            runAnimation(location);

        },  Math.abs(delay));

    }

    private void placeTreasureBlock(String world, Location location)
    {

        ItemStack stack;
        Material mat;

        boolean itemsAdder = Utils.isEnabled("ItemsAdder");
        boolean oraxen = Utils.isEnabled("Oraxen");
        String name = Settings.getWorldStringUnknown(world, "treasure-block");

        if(itemsAdder)
        {

            if(getType() == TreasureType.ENTITY)
            {

                CustomEntity entity = CustomEntity.spawn(name, location);
                furnitureEntity = entity.getEntity();

            }
            else if(getType() == TreasureType.BLOCK)
            {

                CustomBlock block = CustomBlock.place(name, location);

            }
            else if(getType() == TreasureType.FURNITURE)
            {

                CustomFurniture furniture = CustomFurniture.spawn(name, location.getBlock());
                furnitureEntity = furniture.getEntity();

            }
            else
            {
                stack = Utils.checkMaterial(name);
                mat = stack.getType();

                location.getBlock().setType(mat);
            }

        }
        else if(oraxen)
        {

            if(getType() == TreasureType.ORAXEN_FURNITURE)
            {
                furnitureEntity = OraxenFurniture.place(name, location, Rotation.NONE, BlockFace.NORTH);
            }
            else
            {
                stack = Utils.checkMaterial(name);
                mat = stack.getType();

                location.getBlock().setType(mat);
            }

        }
        else
        {
            stack = Utils.checkMaterial(name);
            mat = stack.getType();

            location.getBlock().setType(mat);
        }

        location.getWorld().playEffect(location, Effect.STEP_SOUND, Material.DIRT);

    }

    private TreasureType fetchTreasureType(String world) {

        String name = Settings.getWorldStringUnknown(world, "treasure-block");
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

    private void announceSpawnedTreasure()
    {

        SendCenteredMessage scm = new SendCenteredMessage();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {

            for (Player p : Bukkit.getOnlinePlayers()) {

                for (String s : Messages.getAndFormatList("messages.hunt-message")) {
                    scm.sendCenteredMessage(p, s.replace("{x}", h.getLocation().getBlockX() + "")
                            .replace("{z}", h.getLocation().getBlockZ() + "")
                            .replace("{world}", h.getWorld() + "")
                            .replace("{alias}", getAlias())
                            .replace("{duration}", h.getDuration() + ""));
                }

            }

        });

    }

    private void announceUpcomingTreasure(int delay)
    {
        if(delay > 0)
        {

            SendCenteredMessage scm = new SendCenteredMessage();

            for(Player p : Bukkit.getOnlinePlayers())
            {

                for (String s : Messages.getAndFormatList("messages.announce-treasure")) {
                    scm.sendCenteredMessage(p, s.replace("{x}", getLocation().getBlockX() + "")
                            .replace("{z}", getLocation().getBlockZ() + "")
                            .replace("{world}", getLocation().getWorld().getName())
                            .replace("{alias}", getAlias())
                            .replace("{duration}", h.getDuration() + ""));
                }

            }
        }
    }

    public static boolean isNearTreasure(Player p)
    {

        if(Hunt.isActive(p.getWorld()))
        {

                for (Hunt h : Hunt.getActiveHunts()) {

                    if (h.getLocation().getWorld() == p.getWorld())
                    {

                        if (h.getLocation().distance(p.getLocation()) <= Settings.getWorldIntUnknown(p.getWorld().getName(), "protection-radius"))
                        {
                            return true;
                        }

                    }

                }

        }

        return false;

    }

    private void launchFireworks()
    {
        
        Location loc = getLocation().clone();

        if(Settings.getWorldBooleanUnknown(loc.getWorld().getName(), "fireworks"))
        {

            Firework firework_1 = loc.getWorld().spawn(loc.add(1,0,0), Firework.class);
            FireworkMeta meta_1 = firework_1.getFireworkMeta();
            meta_1.addEffects(FireworkEffect.builder().withColor(Color.PURPLE).withTrail().with(FireworkEffect.Type.BALL_LARGE).build());
            meta_1.setPower(3);
            firework_1.setFireworkMeta(meta_1);

            Firework firework_2 = loc.getWorld().spawn(loc.add(0,0,1), Firework.class);
            FireworkMeta meta_2 = firework_2.getFireworkMeta();
            meta_2.addEffects(FireworkEffect.builder().withColor(Color.WHITE).withTrail().with(FireworkEffect.Type.BALL).build());
            meta_2.setPower(1);
            firework_2.setFireworkMeta(meta_2);

            Firework firework_3 = loc.getWorld().spawn(loc.subtract(1,0,0), Firework.class);
            FireworkMeta meta_3 = firework_3.getFireworkMeta();
            meta_3.addEffects(FireworkEffect.builder().withColor(Color.ORANGE).withTrail().with(FireworkEffect.Type.STAR).build());
            meta_3.setPower(2);
            firework_3.setFireworkMeta(meta_3);

            Firework firework_4 = loc.getWorld().spawn(loc.subtract(0,0,1), Firework.class);
            FireworkMeta meta_4 = firework_4.getFireworkMeta();
            meta_4.addEffects(FireworkEffect.builder().withColor(Color.FUCHSIA).withTrail().with(FireworkEffect.Type.BURST).build());
            meta_4.setPower(2);
            firework_4.setFireworkMeta(meta_4);

        }

    }

    public void awardPrizes()
    {

        launchFireworks();

        int cooldown = Settings.getCooldown();
        World w = getLocation().getWorld();

        for(Player p : getParticipants())
        {

            if(TreasureKey.requiresKey(w.getName()))
            {
                Utils.substractItem(p, TreasureKey.getTreasureKey(w), 1);
            }

            if(Cooldown.hasCooldown(p.getUniqueId(), "treasure-winner"))
            {
                p.sendMessage(Messages.get("winner-cooldown").replace("{time}", Utils.formatRemainingTime(Cooldown.getRemainingTimeMinutes(p.getUniqueId(), "treasure-winner"))));
                return;
            }

            if(cooldown != 0)
            {
                Cooldown cd = new Cooldown(p.getUniqueId(), "treasure-winner", cooldown * 60);
                cd.set();
            }

            for(String s : getCommandRewards())
            {
                s = Utils.setInternalPlaceholders(p, s);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
            }

            for (ItemStack a : getItems().keySet()) {

                if(getItems().get(a) <= Utils.chance()) continue;

                if (!Utils.hasFullInventory(p)) {

                    if (a == null || a.getType() == Material.AIR) continue;

                    p.getInventory().addItem(a);

                } else {

                    if (a == null || a.getType() == Material.AIR) continue;

                    p.getWorld().dropItemNaturally(p.getLocation(), a);
                    p.sendMessage(Messages.get("full-inventory").replace("{amount}", a.getAmount() + "").replace("{item}", Utils.setCapitals(a.getType().toString().toLowerCase().replace("_", " "))));

                }
            }

            announceWinners();
        }

    }

    public void awardPrize(Player p)
    {

        int cooldown = Settings.getCooldown();
        World w = getLocation().getWorld();

        if(TreasureKey.requiresKey(w.getName()))
        {
            Utils.substractItem(p, TreasureKey.getTreasureKey(w), 1);
        }

        if(cooldown != 0)
        {
            Cooldown cd = new Cooldown(p.getUniqueId(), "treasure-winner", cooldown * 60);
            cd.set();
        }

        launchFireworks();

        for(String s : getCommandRewards())
        {
            s = Utils.setInternalPlaceholders(p, s);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
        }

        for (ItemStack a : getItems().keySet()) {

            if(getItems().get(a) <= Utils.chance()) continue;

            if (!Utils.hasFullInventory(p)) {

                if (a == null || a.getType() == Material.AIR) continue;

                p.getInventory().addItem(a);

            } else {

                if (a == null || a.getType() == Material.AIR) continue;

                p.getWorld().dropItemNaturally(p.getLocation(), a);
                p.sendMessage(Messages.get("full-inventory").replace("{amount}", a.getAmount() + "").replace("{item}", Utils.setCapitals(a.getType().toString().toLowerCase().replace("_", " "))));

            }
        }

        announceWinner(p);

    }

    public static void removeAll()
    {

        for(Hunt h : Hunt.getActiveHunts())
        {

            h.getTreasure().remove();
        }

    }

    public void removeItem() {


        for (Entity e : Utils.getNearbyEntities(getLocation(), 2)) {
            if (e instanceof Item || e instanceof ArmorStand) {
                e.remove();
            }
        }


    }

    public void clearMobs()
    {

        for(LivingEntity e : getLocation().getWorld().getLivingEntities()) {
            if(isTreasureKeeper(e)) e.remove();
        }

    }

    public void remove()
    {

        if(getType() == TreasureType.BLOCK)
        {
            CustomBlock.remove(getLocation());
        }
        else if(getType() == TreasureType.ENTITY)
        {
            CustomEntity.byAlreadySpawned(furnitureEntity).destroy();
        }
        else if(getType() == TreasureType.FURNITURE)
        {
            CustomFurniture.remove(furnitureEntity, false);
        }
        else if(getType() == TreasureType.ORAXEN_FURNITURE)
        {
            OraxenFurniture.remove(furnitureEntity, null);
        }

        getLocation().getBlock().setType(Material.AIR);
        getLocation().getWorld().spawnParticle(EXPLOSION_EMITTER, getLocation(), 3);
        getLocation().getWorld().strikeLightningEffect(getLocation());

        isactive = false;



        removeItem();
        clearMobs();

    }

    public void announceWinners()
    {

        SendCenteredMessage scm = new SendCenteredMessage();
        String participantsNames = Joiner.on(", ").join(getParticipantsNames());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {
            for (Player p : Bukkit.getOnlinePlayers()) {

                for (String s : Messages.getAndFormatList("messages.winner-broadcast")) {

                    scm.sendCenteredMessage(p, s.replace("{player}", participantsNames));

                }

            }


            for(Player p : getParticipants())
            {
                scm.sendCenteredMessage(p, Messages.get("winner-message"));
            }

            Bukkit.getConsoleSender().sendMessage(Messages.get("winner-console").replace("{player}", participantsNames).replace("{x}", getLocation().getBlockX() + "")
                    .replace("{z}", getLocation().getBlockZ() + ""));
        });

        TreasureTask.updateLastHunt();
    }

    public void announceWinner(Player k)
    {

        SendCenteredMessage scm = new SendCenteredMessage();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {
            for (Player p : Bukkit.getOnlinePlayers()) {

                for (String s : Messages.getAndFormatList("messages.winner-broadcast")) {

                    scm.sendCenteredMessage(p, s.replace("{player}", k.getName()));

                }

            }

            scm.sendCenteredMessage(k, Messages.get("winner-message"));

            Bukkit.getConsoleSender().sendMessage(Messages.get("winner-console").replace("{player}", k.getName()).replace("{x}", getLocation().getBlockX() + "")
                    .replace("{z}", getLocation().getBlockZ() + ""));
        });

        TreasureTask.updateLastHunt();
    }

    public static Location getItemLocation(Location l)
    {

        return new Location(l.getWorld(), l.getBlockX() + 0.5, l.getBlockY() + 1, l.getBlockZ() + 0.5);
    }

    public static void createItem(Location l)
    {

        if(!l.getBlock().isEmpty() || l.getBlock().isLiquid())
        {
            return;
        }

        ItemStack itm = new ItemStack(Settings.getWorldMaterialUnknown(l.getWorld().getName(), "treasure-icon"));
        ItemMeta meta = itm.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aAncient Treasure"));
        itm.setItemMeta(meta);

        Bukkit.getScheduler().runTask(plugin, ()->
        {

            Item i = l.getWorld().dropItem(l, itm);

            i.setVelocity(new Vector(0, 0, 0));
            i.setInvulnerable(true);
            i.setPickupDelay(32767);
            i.setCustomName(Messages.get("treasure-icon-text"));
            i.setCustomNameVisible(true);
            i.setPersistent(true);

        });

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

    private void spawnGraveParticles(World w, Location entLoc, Material particleMaterial)
    {
        w.playEffect(entLoc, Effect.STEP_SOUND, particleMaterial);
        w.spawnParticle(LAVA, entLoc, 1);
        w.spawnParticle(SOUL, entLoc, 1);
        w.spawnParticle(SOUL_FIRE_FLAME, entLoc, 1);
    }

}
