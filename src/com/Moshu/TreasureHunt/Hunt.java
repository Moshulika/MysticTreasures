package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Locations;
import com.Moshu.Misc.Messages;
import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
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

    private static HashMap<String, ArrayList<ItemStack>> items = new HashMap<>();
    private static ArrayList<Hunt> hunts = new ArrayList<>();

    private static HashMap<World, Long> started_hunts = new HashMap<>();

    public static void addHunt(World w)
    {
        started_hunts.put(w, System.currentTimeMillis());
    }

    public static boolean huntStarting(World w)
    {

        if(!started_hunts.containsKey(w)) return false;

        long delay = (Settings.getWorldIntUnknown(w.getName(), "delay") + 5) / 20; //In seconds
        return started_hunts.get(w) + TimeUnit.SECONDS.toMillis(delay) > System.currentTimeMillis();
    }

    public ArrayList<ItemStack> getItems()
    {
        return items.get(w.getName());
    }

    public static void initialize()
    {

        String[] args;
        ItemStack item;

        String world_name;

        for(String world : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            world_name = plugin.getConfig().getString("settings.enabled-worlds." + world + ".world-name");
            ArrayList<ItemStack> local_items = new ArrayList<>();

            for(String s : plugin.getConfig().getStringList("settings.enabled-worlds." + world + ".item-rewards"))
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
                local_items.add(item);
            }

            items.put(world_name, local_items);
        }

        Treasure.loadMobs();
        Treasure.loadCommands();

    }

    Hunt(World w, int duration)
    {

        this.w = w;
        this.duration = duration;

        double distance = Math.min(Locations.getBorder(w) - 10, Settings.getWorldIntUnknown(w.getName(), "max-treasure-distance"));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {

            CompletableFuture<Location> loc = CompletableFuture.supplyAsync(() -> Locations.getRandomLocationMoreThan(w, distance, 0));
            this.l = loc.join();

        });

    }

    Hunt(Location location, int duration)
    {
        this.w = location.getWorld();
        this.duration = duration;

        this.l = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public void start()
    {

        if(this.l == null)
        {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&5&lMystic&d&lTreasures: &fLocation is null, something went wrong."));
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () ->
        {

            treasure = new Treasure(this, getItems());

            this.starttime = System.currentTimeMillis();
            treasure.create();

            Bukkit.getConsoleSender().sendMessage(Messages.get("treasure-generated-confirmation").replace("{x}", getLocation().getBlockX() + "")
                    .replace("{z}", getLocation().getBlockZ() + "").replace("{world}", getLocation().getWorld().getName()));

            hunts.add(this);

            BukkitRunnable run = new BukkitRunnable() {
                @Override
                public void run() {

                    if(Hunt.isActive(w) && treasure.isActive()) {

                        treasure.hologram();
                        treasure.enableEffects();
                        treasure.flare();

                        this.cancel();
                    }
                }
            };

            run.runTaskTimerAsynchronously(plugin, 0, 2);

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
            if(x.getWorld().getName().equals(w.getName())) return x;
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
            if(h.getWorld().getName().equalsIgnoreCase(w.getName())) return true;
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
