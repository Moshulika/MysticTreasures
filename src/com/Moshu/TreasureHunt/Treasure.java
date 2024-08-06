package com.Moshu.TreasureHunt;

import com.Moshu.Misc.*;
import com.google.common.base.Joiner;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.*;
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
import org.checkerframework.checker.units.qual.A;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Treasure {

    private Location l;
    private ArrayList<ItemStack> items;
    private Hunt h;
    private boolean isactive;

    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    Treasure(Hunt h, ArrayList<ItemStack> items)
    {

        this.h = h;
        this.l = h.getLocation();
        this.items = items;
        this.isactive = false;

    }

    private ArrayList<Player> participants = new ArrayList<>();

    public Location getLocation()
    {
        return l;
    }

    public ArrayList<ItemStack> getItems()
    {
        return items;
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

            if(h.getTreasure().getLocation().getWorld() == loc.getWorld() && h.getTreasure().getLocation().getBlockX() == loc.getBlockX() && h.getTreasure().getLocation().getBlockZ() == loc.getBlockZ())
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

        int x = Utils.randInt(1, 6);
        int z = Utils.randInt(1, 6);

        return Utils.getHighestBlock(l.getWorld(), l.getBlockX(), l.getBlockZ(), l.getWorld().getSpawnLocation()).add(x, 0, z);

    }

    private static HashMap<String, ArrayList<String>> command_rewards = new HashMap<>();

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

    private static HashMap<String, ArrayList<TreasureKeeper>> mobs = new HashMap<>();

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

                    if (!Utils.isInt(args[1])) {
                        plugin.getLogger().log(Level.SEVERE, "Invalid amount in treasure configuration: " + args[1]);
                        continue;
                    }

                    amount = Integer.parseInt(args[1]);

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

    public void spawnMobs()
    {

        LivingEntity e;

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
                                spawnEntities.add(entity);
                            }

                        }
                        else
                        {
                            e = (LivingEntity) l.getWorld().spawnEntity(getNearLocation(), t.getType());
                            e.setMetadata("treasure-mob-" + l.getWorld().getName(), new FixedMetadataValue(plugin, "treasure-mob-" + l.getWorld().getName()));
                            e.setRemoveWhenFarAway(false);
                            spawnEntities.add(e);
                        }

                    }
                    else
                    {

                        e = (LivingEntity) l.getWorld().spawnEntity(getNearLocation(), t.getType());
                        e.setMetadata("treasure-mob-" + l.getWorld().getName(), new FixedMetadataValue(plugin, "treasure-mob-" + l.getWorld().getName()));
                        e.setRemoveWhenFarAway(false);
                        spawnEntities.add(e);

                    }



                }
            }
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

        if(!Utils.isEnabled("DecentHolograms"))
        {
            createItem(getItemLocation(l));
            return;
        }

        DHAPI.createHologram("treasurehunt_" + loc.getWorld().getName(), loc.clone().add(0.5,1.5,0.5), false).setDownOrigin(true);

        Hologram h = DHAPI.getHologram("treasurehunt_" + loc.getWorld().getName());

        ArrayList<String> lines = new ArrayList<>();

        for(String s : Messages.getAndFormatList("messages.treasure-hologram"))
        {
            lines.add(s.replace("{time}", Utils.getCountDown(Hunt.getHunt(loc.getWorld()).getRemainingTime())));
        }

        h.enable();
        h.setUpdateInterval(20);

        DHAPI.setHologramLines(h, lines);
        h.updateAll();

    }

    public void flare()
    {

            String flare = Settings.getWorldStringUnknown(l.getWorld().getName(), "flare-type");
            int refresh = flare.equalsIgnoreCase("few") ? 20 : 2;
            Location loc = getLocation().clone();

            if (!flare.equalsIgnoreCase("none")) {

                BukkitRunnable run = new BukkitRunnable() {

                    Particle p = Settings.getWorldParticleUnknown(loc.getWorld().getName(), "flare-particle");

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
            Effects.runCircle(l);
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
            
            Location location = getLocation().clone();
            World w = location.getWorld();

            Material mat = Settings.getWorldMaterialUnknown(w.getName(), "treasure-block");
            Particle part = Settings.getWorldParticleUnknown(w.getName(), "treasure-particles");

            int distance_to_spawn = Settings.getWorldIntUnknown(w.getName(), "distance-from-player-to-spawn-mobs");

            location.getBlock().setType(mat);
            location.getWorld().playEffect(location, Effect.STEP_SOUND, Material.DIRT);

            Bukkit.getScheduler().runTaskLater(plugin, () ->
            {

                if(distance_to_spawn <= 0)
                {
                    spawnMobs();
                }

            }, 1);

            this.isactive = true;
            
            BukkitRunnable run = new BukkitRunnable() {

                boolean spawned = false;

                @Override
                public void run() {

                    try {

                        if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - h.getStartTime()) >= h.getDuration()) {
                            Bukkit.getScheduler().runTask(plugin, () -> remove());
                        }

                        if (location.getBlock().getType() == mat) {

                            w.spawnParticle(part, Utils.getParticleLocation(location), 3);
                            w.spawnParticle(part, Utils.getParticleLocation(location), 3);

                            if (Utils.isEnabled("DecentHolograms")) {

                                if (DHAPI.getHologram("treasurehunt_" + w.getName()) != null) {

                                    Hologram h = DHAPI.getHologram("treasurehunt_" + w.getName());

                                    ArrayList<String> lines = new ArrayList<>();

                                    for (String s : Messages.getAndFormatList("messages.treasure-hologram")) {
                                        lines.add(s.replace("{time}", Utils.getCountDown(Hunt.getHunt(w).getRemainingTime())));
                                    }

                                    DHAPI.setHologramLines(h, lines);
                                    h.updateAll();

                                }

                            }

                            if(distance_to_spawn > 0 && !Utils.getNearbyPlayers(location, distance_to_spawn).isEmpty() && !spawned) {
                                Bukkit.getScheduler().runTaskLater(plugin, () ->
                                {
                                    spawnMobs();
                                    spawned = true;
                                }, 1);
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

            SendCenteredMessage scm = new SendCenteredMessage();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
            {

                for (Player p : Bukkit.getOnlinePlayers()) {

                    for (String s : Messages.getAndFormatList("messages.hunt-message")) {
                        scm.sendCenteredMessage(p, s.replace("{x}", h.getLocation().getBlockX() + "")
                                .replace("{z}", h.getLocation().getBlockZ() + "")
                                .replace("{world}", h.getWorld() + "")
                                .replace("{duration}", h.getDuration() + ""));
                    }

                }

            });

        });


    }

    public void create() {

        Location location = getLocation().clone();
        World w = location.getWorld();
        int delay = Settings.getWorldIntUnknown(w.getName(), "delay");

        if(delay > 0)
        {

            SendCenteredMessage scm = new SendCenteredMessage();

            for(Player p : Bukkit.getOnlinePlayers())
            {

                for (String s : Messages.getAndFormatList("messages.announce-treasure")) {
                    scm.sendCenteredMessage(p, s.replace("{x}", location.getBlockX() + "")
                            .replace("{z}", location.getBlockZ() + "")
                            .replace("{world}", w + "")
                            .replace("{duration}", h.getDuration() + ""));
                }

            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->
        {

            location.getChunk().load();
            location.getChunk().setForceLoaded(true);

            Material mat = Settings.getWorldMaterialUnknown(w.getName(), "treasure-block");

            ArmorStand animation = (ArmorStand) l.getWorld().spawnEntity(location.clone().add(0, 50, 0), EntityType.ARMOR_STAND);

            animation.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 600, 1));
            animation.setBasePlate(false);
            animation.setHelmet(new ItemStack(mat));
            animation.setInvulnerable(true);
            animation.setVisible(false);

            BukkitRunnable run_falling_particles = new BukkitRunnable() {

                @Override
                public void run() {

                    //Cand a ajuns jos sa spawneze chestu automat
                    if(animation.isOnGround())
                    {

                        Bukkit.getScheduler().runTask(plugin, animation::remove);

                        w.spawnParticle(Particle.EXPLOSION, animation.getLocation(), 1);

                        spawnTreasure();
                        this.cancel();
                        return;
                    }

                    w.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, animation.getLocation().add(0, 3, 0), 1);

                }
            };

            run_falling_particles.runTaskTimerAsynchronously(plugin, 0, 1);

        },  Math.abs(delay));

    }

    public static boolean isNearTreasure(Player p)
    {

        if(Hunt.isActive(p.getWorld()))
        {

                for (Hunt h : Hunt.getActiveHunts()) {

                    if (h.getLocation().getWorld() == p.getWorld())
                    {

                        if (h.getLocation().distance(p.getLocation()) <= 50)
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

        for(Player p : getParticipants())
        {
            if(Cooldown.hasCooldown(p.getUniqueId(), "treasure-winner"))
            {
                p.sendMessage(Messages.get("winner-cooldown").replace("{time}", Utils.formatRemainingTime(Cooldown.getRemainingTimeMinutes(p.getUniqueId(), "treasure-winner"))));
                return;
            }

            if(Settings.getCooldown() != 0)
            {
                Cooldown cd = new Cooldown(p.getUniqueId(), "treasure-winner", Settings.getCooldown() * 60);
                cd.set();
            }

            for(String s : getCommandRewards())
            {
                s = Utils.setInternalPlaceholders(p, s);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
            }

            for (ItemStack a : getItems()) {

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

        if(Cooldown.hasCooldown(p.getUniqueId(), "treasure-winner"))
        {
            p.sendMessage(Messages.get("winner-cooldown").replace("{time}", Utils.formatRemainingTime(Cooldown.getRemainingTimeMinutes(p.getUniqueId(), "treasure-winner"))));
            return;
        }

        if(Settings.getCooldown() != 0)
        {
            Cooldown cd = new Cooldown(p.getUniqueId(), "treasure-winner", Settings.getCooldown() * 60);
            cd.set();
        }

        launchFireworks();

        for(String s : getCommandRewards())
        {
            s = Utils.setInternalPlaceholders(p, s);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
        }

        for (ItemStack a : getItems()) {

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
            if (e instanceof Item) {
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
        getLocation().getBlock().setType(Material.AIR);
        getLocation().getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, getLocation(), 3);
        getLocation().getWorld().strikeLightning(getLocation());

        if(Utils.isEnabled("DecentHolograms"))
        {
            Hologram h = DHAPI.getHologram("treasurehunt_" + getLocation().getWorld().getName());
            if(h != null) h.delete();
        }

        removeItem();
        clearMobs();
        isactive = false;

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

        Item i = l.getWorld().dropItem(l, itm);

        i.setVelocity(new Vector(0, 0, 0));
        i.setInvulnerable(true);
        i.setPickupDelay(32767);
        i.setCustomName(Messages.get("treasure-icon-text"));
        i.setCustomNameVisible(true);
        i.setPersistent(true);

    }

}
