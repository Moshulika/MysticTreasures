package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Locations;
import com.Moshu.Misc.Utils;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Hunt {

    private Treasure treasure;
    private World w;
    private Location l;
    private int duration;
    private long starttime = 0;

    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private static ArrayList<ItemStack> items = new ArrayList<>();
    private static ArrayList<Hunt> hunts = new ArrayList<>();

    public static void initialize()
    {

        String[] args;
        ItemStack item;

        for(String s : plugin.getConfig().getStringList("treasures.items"))
        {

            args = s.split(":");

            if(args.length < 2) {

                plugin.getLogger().log(Level.SEVERE, "Invalid item in treasure prize configuration: " + s);
                continue;
            }

            if(Material.matchMaterial(args[0]) == null)
            {
                plugin.getLogger().log(Level.SEVERE, "Invalid material name in treasure prize configuration: " + args[0]);
                continue;
            }

            if(!Utils.isInt(args[1]))
            {
                plugin.getLogger().log(Level.SEVERE, "Invalid amount in treasure prize configuration: " + args[1]);
                continue;
            }


            item = new ItemStack(Material.matchMaterial(args[0]), Integer.parseInt(args[1]));
            items.add(item);
        }
    }

    Hunt(World w, int duration)
    {

        this.w = w;
        this.duration = duration;

        int distance = 15000;
        int min = 2500;

        if(Utils.isEnabled("ChunkyBorder"))
        {
            distance = Locations.getBorder(w) - 10;
        }

        final int d = distance;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {

            CompletableFuture<Location> loc = CompletableFuture.supplyAsync(() -> Locations.getRandomLocationMoreThan(w, d, 2500));
            this.l = loc.join();

        });

    }

    public void start()
    {

        if(this.l == null)
        {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTreasure Hunt: Comoara nu a fost initializata inca, incearca mai tarziu."));
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () ->
        {
            treasure = new Treasure(this, items);

            this.starttime = System.currentTimeMillis();
            treasure.create();

            /*
            if(Utils.isEnabled("DiscoBot")) {
                Bot.sendTreasureHunt();
            }
             */

            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTreasure Hunt: &fA inceput vanatoarea de comori! Comoara se afla la X: " + getLocation().getBlockX() + " Z:" + getLocation().getBlockZ()));

            hunts.add(this);
            treasure.hologram();

        });

    }

    public static ArrayList<Hunt> getActiveHunts()
    {

        ArrayList<Hunt> h = new ArrayList<>();

        for(Hunt x : getHunts())
        {

            if(x.getTreasure().isActive())
            {
                h.add(x);
            }

        }

        return h;
    }

    public static Hunt getHunt(World w)
    {
        for(Hunt x : getHunts())
        {
            if(x.getWorld().equals(w)) return x;
        }

        return null;
    }

    public void stop()
    {

        getTreasure().remove();

    }

    public long getRemainingTime()
    {

        return getStartTime() + TimeUnit.MINUTES.toMillis(getDuration()) - System.currentTimeMillis();

    }

    public static boolean isActive(World w)
    {

        for(Hunt h : getActiveHunts())
        {
            if(h.getWorld() == w) return true;
        }

        return false;

    }

    public static String isActiveString(World w)
    {

        if(isActive(w)) return "Yes";
        return "No";

    }

    public static ArrayList<Hunt> getHunts()
    {
        return hunts;
    }

    public Location getLocation()
    {
        return l;
    }

    public Treasure getTreasure()
    {
        return treasure;
    }

    public int getDuration()
    {
        return duration;
    }

    public long getStartTime()
    {
        return starttime;
    }

    public World getWorld()
    {
        return this.w;
    }

}
