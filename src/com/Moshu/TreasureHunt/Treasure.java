package com.Moshu.TreasureHunt;

import com.Moshu.Misc.*;
import com.Moshu.TreasureHunt.objects.CommandReward;
import com.Moshu.TreasureHunt.objects.ItemReward;
import com.Moshu.TreasureHunt.objects.TreasureData;
import com.Moshu.TreasureHunt.objects.TreasureKeeper;
import com.google.common.base.Joiner;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomEntity;
import dev.lone.itemsadder.api.CustomFurniture;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import io.th0rgal.oraxen.api.OraxenFurniture;
import org.bukkit.*;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Treasure {

    private Location l;
    private final Hunt h;
    private boolean isActive;
    private Entity furnitureEntity;
    private final HashMap<UUID, Double> playerDamage = new HashMap<>();


    private TreasureData treasureData;

    private boolean spawned = false;
    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    ArrayList<Entity> spawnedTreasureKeepers = new ArrayList<>();

    private static final Particle EXPLOSION = Settings.getCompatParticle("treasure-spawn-particle");
    private static final Particle EXPLOSION_EMITTER = Settings.getCompatParticle("treasure-remove-particle");
    private static final Particle CAMPFIRE_SIGNAL_SMOKE = Settings.getCompatParticle("treasure-fall-particle");

    Treasure(Hunt h, TreasureData d)
    {
        this.h = h;
        this.l = h.getLocation();
        this.treasureData = d;
        this.isActive = false;
    }

    public TreasureData getTreasureData()
    {
        return treasureData;
    }

    private final ArrayList<Player> participants = new ArrayList<>();

    public Location getLocation()
    {
        return l;
    }

    public boolean haveTheMobsSpawned()
    {
        return spawned;
    }

    public boolean isActive()
    {
        return isActive;
    }

    public double getDamageGiven(Player p)
    {
        return playerDamage.getOrDefault(p.getUniqueId(), 0.0);
    }

    public void addDamageGiven(Player p, double damage)
    {
        playerDamage.put(p.getUniqueId(), getDamageGiven(p) + damage);
    }

    public boolean wereTreasureKeepersDamaged()
    {
        return !playerDamage.isEmpty();
    }

    @Nullable
    public Player getPlayerWithMostDamage()
    {

        double max = 0;
        UUID maxUUID = null;

        for(UUID uuid : playerDamage.keySet())
        {

            if(playerDamage.get(uuid) > max)
            {

                if(Bukkit.getPlayer(uuid) == null) continue;

                max = playerDamage.get(uuid);
                maxUUID = uuid;
            }

        }

        if(maxUUID == null) return null;
        return Bukkit.getPlayer(maxUUID);

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

    public boolean timePassedBeforePickup()
    {
        return getHunt().getElapsedTime() >= getTreasureData().getMilliesBeforePickup();
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

        for(Hunt h : Hunt.getActiveTreasures())
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

    public TreasureKeeper getTreasureKeeper(LivingEntity e)
    {

        UUID uuid = e.getUniqueId();

        for(TreasureKeeper t :  getTreasureData().getTreasureKeepers())
        {

            if(t.isSpawned())
            {
                if(t.getUUID().toString().equals(uuid.toString())) return t;
            }

        }

        return null;

    }

    public static Treasure getTreasure(Location loc)
    {

        for(Hunt h : Hunt.getActiveTreasures())
        {

            if(isTreasure(loc))
            {
                return h.getTreasure();
            }

        }

        return null;

    }


    public boolean mobsCleared()
    {
        for(Entity e : spawnedTreasureKeepers)
        {
            if(!e.isDead()) return false;
        }

        return true;
    }

    public ArrayList<Entity> getRemainingMobs()
    {

        ArrayList<Entity> entities = new ArrayList<>();

        for(Entity e : spawnedTreasureKeepers)
        {

            if(!e.isDead()) entities.add(e);

        }

        return entities;


    }

    public int remainingMobs()
    {
        return getRemainingMobs().size();
    }

    public boolean isTreasureKeeper(LivingEntity e)
    {
        return spawnedTreasureKeepers.contains(e) || e.hasMetadata("treasure-mob-" + l.getWorld().getName());
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
                lines.add(s.replace("{time}", Utils.getCountDown(getHunt().getRemainingTime())));
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
            createItem();
        }

    }

    public void flare()
    {

            String flare = getTreasureData().getFlareType();
            int refresh = flare.equalsIgnoreCase("few") ? 20 : 2;
            Location loc = getLocation().clone();

            if (!flare.equalsIgnoreCase("none")) {

                BukkitRunnable run = new BukkitRunnable() {

                    final Particle p = getTreasureData().getFlareParticle();

                    @Override
                    public void run() {

                        if(!isActive())
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

    private void spawnTreasure() {

        Bukkit.getScheduler().runTask(plugin, () ->
        {

            Location location;

            Location temp_location = getLocation().clone();
            World w = temp_location.getWorld();

            Particle part = getTreasureData().getTreasureParticles();
            int distance_to_spawn = getTreasureData().getDistanceFromPlayerToSpawnMobs();

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
                    spawnTreasureKeepers();
                }

            }, 1);

            this.isActive = true;

            hologram();
            enableEffects();
            flare();
            
            tickTreasure(location, part, distance_to_spawn);
            announceSpawnedTreasure();

        });


    }

    public Hunt getHunt()
    {
        return h;
    }

    private Location getNearLocation()
    {

        int x = Utils.randInt(-6, 6);
        int z = Utils.randInt(-6, 6);

        return Utils.getHighestBlock(l.getWorld(), l.getBlockX(), l.getBlockZ(), l.getWorld().getSpawnLocation()).add(x, 0, z);

    }

    private void spawnTreasureKeepers()
    {

        for(TreasureKeeper k : getTreasureData().getTreasureKeepers())
        {
            k.spawn(spawnedTreasureKeepers, getLocation());
        }

    }

    public boolean isLocked()
    {
        return getRemainingMobs().isEmpty() && timePassedBeforePickup();
    }

    private void tickTreasure(Location location, Particle part, int distance_to_spawn)
    {

        World w = location.getWorld();
        int wandering_distance = getTreasureData().getMobWanderingDistance();

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
                                    .replace("{time}", Utils.getCountDown(getHunt().getRemainingTime()))
                                    .replace("{status}", isLocked() ? unlocked : locked)
                                    .replace("{alias}", getTreasureData().getTreasureName())
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
                                spawnTreasureKeepers();
                                enableEffects();
                                spawned = true;
                            }, 1);
                        }

                        for(Entity e : getRemainingMobs())
                        {

                            if(!e.getLocation().getWorld().getName().equals(location.getWorld().getName()))
                            {
                                e.remove();
                                continue;
                            }

                            if(e.getLocation().distance(location) > wandering_distance)
                            {

                                Bukkit.getScheduler().runTask(plugin, ()->
                                {
                                    e.teleport(getNearLocation());
                                });
                            }

                        }


                    } else {
                        getHunt().setInactive();
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

        String s = getTreasureData().getTreasureBlockString();

        if(getTreasureData().getTreasureType() == TreasureData.TreasureType.VANILLA) {

            if(getTreasureData().fallFromTheSky())
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
        int delay = getTreasureData().getDelay();

        announceUpcomingTreasure(delay);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->
        {

            location.getChunk().load();
            location.getChunk().setForceLoaded(true);

            runAnimation(location);

        },  Math.abs(delay));

    }

    private void placeTreasureBlock(String world, Location location)
    {

        ItemStack stack;
        Material mat;

        boolean itemsAdder = Utils.isEnabled("ItemsAdder");
        boolean oraxen = Utils.isEnabled("Oraxen");
        String name = getTreasureData().getTreasureBlockString();

        TreasureData.TreasureType type = getTreasureData().getTreasureType();

        if(itemsAdder)
        {

            if(type == TreasureData.TreasureType.ENTITY)
            {

                CustomEntity entity = CustomEntity.spawn(name, location);
                furnitureEntity = entity.getEntity();

            }
            else if(type == TreasureData.TreasureType.BLOCK)
            {

                CustomBlock block = CustomBlock.place(name, location);

            }
            else if(type == TreasureData.TreasureType.FURNITURE)
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

            if(type == TreasureData.TreasureType.ORAXEN_FURNITURE)
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

    private void announceSpawnedTreasure()
    {

        SendCenteredMessage scm = new SendCenteredMessage();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {

            for (Player p : Bukkit.getOnlinePlayers()) {

                for (String s : Messages.getAndFormatList("messages.hunt-message")) {
                    scm.sendCenteredMessage(p, s.replace("{x}", h.getLocation().getBlockX() + "")
                            .replace("{z}", h.getLocation().getBlockZ() + "")
                            .replace("{world}", getTreasureData().getWorldName() + "")
                            .replace("{alias}", getTreasureData().getTreasureName())
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
                            .replace("{alias}", getTreasureData().getTreasureName())
                            .replace("{duration}", h.getDuration() + ""));
                }

            }
        }
    }

    public static boolean isNearTreasure(Player p) {


        for (Hunt h : Hunt.getActiveTreasures()) {

            if (Locations.distanceTo(p.getLocation(), h.getLocation()) <= Settings.getInt("protection-radius")) {
                return true;
            }

        }

        return false;

    }

    private void launchFireworks()
    {
        
        Location loc = getLocation().clone();

        if(getTreasureData().shootFireworks())
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

    public void awardPrizes() {

        launchFireworks();

        int cooldown = Settings.getCooldown();

        for (Player p : getParticipants()) {

            if (Cooldown.hasCooldown(p.getUniqueId(), "treasure-winner")) {
                p.sendMessage(Messages.get("winner-cooldown").replace("{time}", Utils.formatRemainingTime(Cooldown.getRemainingTimeMinutes(p.getUniqueId(), "treasure-winner"))));
                return;
            }

            if (cooldown != 0) {
                Cooldown cd = new Cooldown(p.getUniqueId(), "treasure-winner", cooldown * 60);
                cd.set();
            }

            for (CommandReward c : getTreasureData().getCommandRewards()) {
                c.run(p);
            }

            for(ItemReward i : getTreasureData().getItemRewards())
            {
                if (i.getChance() <= Utils.chance()) continue;

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
        }

    }

    public void awardPrize(Player p)
    {

        int cooldown = Settings.getCooldown();
        World w = getLocation().getWorld();

        if(cooldown != 0)
        {
            Cooldown cd = new Cooldown(p.getUniqueId(), "treasure-winner", cooldown * 60);
            cd.set();
        }

        launchFireworks();

        for (CommandReward c : getTreasureData().getCommandRewards()) {
            c.run(p);
        }

        int xOffset = (int) ((Math.random() * 2 * 5 + 1) - 5);
        int zOffset = (int) ((Math.random() * 2 * 5 + 1) - 5);
        Location dropLocation = Utils.getHighestBlock(w, getLocation().getBlockX() + xOffset, getLocation().getBlockZ() + zOffset, getLocation());

        int x = 1;
        boolean dropOnGround = getTreasureData().dropsItemsOnGround();

        for(ItemReward r : getTreasureData().getItemRewards())
        {
            if (r.getChance() <= Utils.chance()) continue;

            if(dropOnGround)
            {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->
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
            }
            else
            {
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

    public static void removeAll()
    {

        for(Hunt h : Hunt.getActiveTreasures())
        {

            h.getTreasure().remove();
        }

    }

    private void removeItem() {


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

        TreasureData.TreasureType type = getTreasureData().getTreasureType();

        if(type == TreasureData.TreasureType.BLOCK)
        {
            CustomBlock.remove(getLocation());
        }
        else if(type == TreasureData.TreasureType.ENTITY)
        {
            CustomEntity.byAlreadySpawned(furnitureEntity).destroy();
        }
        else if(type == TreasureData.TreasureType.FURNITURE)
        {
            CustomFurniture.remove(furnitureEntity, false);
        }
        else if(type == TreasureData.TreasureType.ORAXEN_FURNITURE)
        {
            OraxenFurniture.remove(furnitureEntity, null);
        }

        getLocation().getBlock().setType(Material.AIR);
        getLocation().getWorld().spawnParticle(EXPLOSION_EMITTER, getLocation(), 3);
        getLocation().getWorld().strikeLightningEffect(getLocation());

        isActive = false;

        if(Utils.isEnabled("DecentHolograms"))
        {
            Hologram h = DHAPI.getHologram("treasurehunt_" + getLocation().getWorld().getName());
            if(h != null) h.delete();
        }

        else if(Utils.isEnabled("FancyHolograms"))
        {

            HologramHandler.getInstance().delete(getLocation());

        }

        removeItem();
        clearMobs();

        getHunt().setInactive();

    }

    private void announceWinners()
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

    private void announceWinner(Player k)
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

    private Location getItemLocation(Location l)
    {

        return new Location(l.getWorld(), l.getBlockX() + 0.5, l.getBlockY() + 1, l.getBlockZ() + 0.5);
    }

    private void createItem()
    {

        Location loc = getLocation().clone();
        Location l = getItemLocation(loc);

        if(!l.getBlock().isEmpty() || l.getBlock().isLiquid())
        {
            return;
        }

        ItemStack itm = new ItemStack(getTreasureData().getTreasureIcon());
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

}
