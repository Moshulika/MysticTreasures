package com.Moshu.TreasureHunt;

import com.Moshu.Misc.SendCenteredMessage;
import com.Moshu.Misc.Utils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Treasure {

    private Location l;
    private ArrayList<ItemStack> items;
    private Hunt h;
    private boolean isactive;

    private Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    Treasure(Hunt h, ArrayList<ItemStack> items)
    {

        this.h = h;
        this.l = h.getLocation();
        this.items = items;
        this.isactive = false;

    }

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

    public void spawnMobs()
    {

        LivingEntity e;

        for(int i = 0; i < 5; i++)
        {

            e = (LivingEntity) getLocation().getWorld().spawnEntity(getNearLocation(), EntityType.PILLAGER);
            e.setRemoveWhenFarAway(false);
            mobs.add(e);

        }

        for(int i = 0; i < 2; i++)
        {

            e = (LivingEntity) getLocation().getWorld().spawnEntity(getNearLocation(), EntityType.VINDICATOR);
            e.setRemoveWhenFarAway(false);
            mobs.add(e);

        }

        for(int i = 0; i < 1; i++)
        {

            e = (LivingEntity) getLocation().getWorld().spawnEntity(getNearLocation(), EntityType.RAVAGER);
            e.setRemoveWhenFarAway(false);
            mobs.add(e);

        }


    }

    private ArrayList<Entity> mobs = new ArrayList<>();

    public void hologram()
    {

        if(!Utils.isEnabled("DecentHolograms")) return;

        DHAPI.createHologram("treasurehunt", l.clone().add(0.5,1.5,0.5), false).setDownOrigin(true);

        Hologram h = DHAPI.getHologram("treasurehunt");

        ArrayList<String> lines = new ArrayList<>();

        lines.add(ChatColor.translateAlternateColorCodes('&', "&6&lTreasure"));
        lines.add(ChatColor.translateAlternateColorCodes('&', ""));
        lines.add(ChatColor.translateAlternateColorCodes('&', "&fDespawning in " + Utils.getCountDown(Hunt.getHunt().getRemainingTime())));
        lines.add(ChatColor.translateAlternateColorCodes('&', "&fClick to open"));
        lines.add("#ICON: EMERALD");


        h.enable();
        h.setUpdateInterval(20);

        DHAPI.setHologramLines(h, lines);
        h.updateAll();

    }

    public void create() {

        l.getChunk().load();
        l.getChunk().setForceLoaded(true);

        l.getBlock().setType(Material.ENDER_CHEST);
        //createItem(getItemLocation(l));
        l.getWorld().playEffect(l, Effect.STEP_SOUND, Material.DIRT);

        Bukkit.getScheduler().runTaskLater(plugin, () -> spawnMobs(), 1);

        this.isactive = true;

        Location loc = l;

        BukkitRunnable run = new BukkitRunnable() {

            public void run() {

                if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - h.getStartTime()) >= h.getDuration()) {
                    Bukkit.getScheduler().runTask(plugin, () -> remove());
                }

                if (loc.getBlock().getType() == Material.ENDER_CHEST)
                {
                    loc.getWorld().spawnParticle(Particle.COMPOSTER, Utils.getParticleLocation(loc), 3);
                    loc.getWorld().spawnParticle(Particle.COMPOSTER, Utils.getParticleLocation(loc), 3);

                    if(Utils.isEnabled("DecentHolograms"))
                    {

                        if(DHAPI.getHologram("treasurehunt") != null)
                        {

                            Hologram h = DHAPI.getHologram("treasurehunt");

                            ArrayList<String> lines = new ArrayList<>();

                            lines.add(ChatColor.translateAlternateColorCodes('&', "&6&lTreasure"));
                            lines.add(ChatColor.translateAlternateColorCodes('&', ""));
                            lines.add(ChatColor.translateAlternateColorCodes('&', "&fDespawning in " + Utils.getCountDown(Hunt.getHunt().getRemainingTime())));
                            lines.add(ChatColor.translateAlternateColorCodes('&', "&fClick to open"));
                            lines.add("#ICON: EMERALD");

                            DHAPI.setHologramLines(h, lines);
                            h.updateAll();

                        }

                    }


                } else {
                    Hunt.getHunts().remove(h);
                    Bukkit.getScheduler().runTask(plugin, () -> l.getChunk().setForceLoaded(false));
                    this.cancel();
                }
            }

        };

        run.runTaskTimerAsynchronously(plugin, 0, 5);

        SendCenteredMessage scm = new SendCenteredMessage();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {

            for (Player p : Bukkit.getOnlinePlayers()) {


                p.sendMessage(" ");
                scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&6&lTreasure &e&lHunt"));
                p.sendMessage(" ");
                scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&fO comoara misterioasa a fost descoperita in lume!"));
                scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&fX: &7" + l.getBlockX() + " &fZ: &7" + l.getBlockZ()));
                scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&fMult noroc in a o gasi!"));
                p.sendMessage(" ");
                scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&7&o((Tip: Comoara va disparea in 20 de minute))"));
                p.sendMessage(" ");


            }

        });

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

    public void awardPrize(Player p)
    {

        for(String s : plugin.getConfig().getStringList("treasures.commands"))
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
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8(&c✖&8) &fNu ai mai avut loc in inventar, deci ai lasat pe jos x" + a.getAmount() + " " + Utils.setCapitals(a.getType().toString().toLowerCase().replace("_", " ")) + "."));


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

    public void remove()
    {
        getLocation().getBlock().setType(Material.AIR);
        getLocation().getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, getLocation(), 3);
        getLocation().getWorld().strikeLightning(getLocation());

        if(Utils.isEnabled("DecentHolograms"))
        {
            DHAPI.getHologram("treasurehunt").delete();
        }

        removeItem();

        /*
        for(Entity e : mobs)
        {
            e.remove();
        }

         */

    }

    public void announceWinner(Player k)
    {

        SendCenteredMessage scm = new SendCenteredMessage();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(" ");
                scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&6&lTreasure &e&lHunt"));
                p.sendMessage(" ");
                scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&fComoara misterioasa a fost gasita de catre"));
                scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&6" + k.getName()));
                scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&fFelicitari, aventurierule!"));
                p.sendMessage(" ");
            }

            scm.sendCenteredMessage(k, ChatColor.translateAlternateColorCodes('&', "&fAi primit $2500, 5 puncte, x6 Diamond si x6 Emerald"));
            k.sendMessage(" ");

            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTreasure Hunt: &f" + k.getName() + " a descoperit comoara de la X: " + getLocation().getBlockX() + " Z:" + getLocation().getBlockZ()));
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

        ItemStack itm = new ItemStack(Material.EMERALD);
        ItemMeta meta = itm.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aAncient Treasure"));
        itm.setItemMeta(meta);

        Item i = l.getWorld().dropItem(l, itm);

        i.setVelocity(new Vector(0, 0, 0));
        i.setInvulnerable(true);
        i.setPickupDelay(32767);
        i.setCustomName(ChatColor.translateAlternateColorCodes('&', "&8(&2❖&8) &aTreasure &7 | Click"));
        i.setCustomNameVisible(true);
        i.setPersistent(true);

    }

}
