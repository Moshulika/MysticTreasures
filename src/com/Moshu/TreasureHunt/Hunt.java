package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Locations;
import com.Moshu.Misc.Messages;
import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Hunt {

    private Treasure treasure;
    private final World w;
    private Location l;
    private final int duration;
    private long starttime = 0;


    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private static final HashMap<String, ArrayList<ItemStack>> items = new HashMap<>();
    private static final HashMap<String, HashMap<ItemStack, Double>> itemsWithChance = new HashMap<>();
    private static final ArrayList<Hunt> hunts = new ArrayList<>();

    private static final HashMap<World, Long> started_hunts = new HashMap<>();

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

    public Set<ItemStack> getItems()
    {
        return itemsWithChance.get(w.getName()).keySet();
    }

    public HashMap<ItemStack, Double> getItemsWithChances()
    {
        return itemsWithChance.get(w.getName());
    }

    private static void loadItemsReworked() {

        ItemStack item;
        int amount;
        double chance;
        String world_name;

        boolean itemsAdder = Utils.isEnabled("ItemsAdder");

        for (String world : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false)) {

            world_name = plugin.getConfig().getString("settings.enabled-worlds." + world + ".world-name");
            HashMap<ItemStack, Double> local_items_chance = new HashMap<>();

            for (String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds." + world + ".item-rewards").getKeys(false)) {


                try {

                    String materialName = plugin.getConfig().getString("settings.enabled-worlds." + world + ".item-rewards." + s + ".item", "STONE");
                    String amountString = plugin.getConfig().getString("settings.enabled-worlds." + world + ".item-rewards." + s + ".amount", "1-10");
                    String amountChance = plugin.getConfig().getString("settings.enabled-worlds." + world + ".item-rewards." + s + ".chance", "0.6");
                    String displayName = plugin.getConfig().getString("settings.enabled-worlds." + world + ".item-rewards." + s + ".name", "&6&l&oREWARD #1");
                    List<String> lore = plugin.getConfig().getStringList("settings.enabled-worlds." + world + ".item-rewards." + s + ".lore");

                    if (Utils.isDouble(amountChance)) {
                        chance = Double.parseDouble(amountChance);
                    } else {
                        chance = 100;
                        plugin.getLogger().log(Level.SEVERE, "Invalid chance in treasure prize configuration: " + amountChance);
                    }

                    if (!Utils.isInt(amountString)) {
                        //DIAMOND:5-10
                        if (amountString.split("-").length == 2) {

                            int min = Integer.parseInt(amountString.split("-")[0]);
                            int max = Integer.parseInt(amountString.split("-")[1]);

                            amount = Utils.randInt(min, max);

                            if (amount <= 0) continue;

                        } else {
                            plugin.getLogger().log(Level.SEVERE, "Invalid amount in treasure prize configuration: " + amountString);
                            continue;
                        }

                    } else {
                        amount = Integer.parseInt(amountString);
                    }

                    if (itemsAdder) {

                        CustomStack stack = CustomStack.getInstance(materialName);

                        if (stack != null) {
                            item = stack.getItemStack();
                            item.setAmount(amount);
                        } else {

                            if (Material.matchMaterial(materialName) == null) {
                                plugin.getLogger().log(Level.SEVERE, "Invalid material name in treasure prize configuration: " + materialName);
                                continue;
                            }

                            item = new ItemStack(Material.matchMaterial(materialName), amount);
                            item.setItemMeta(Utils.setMeta(item.getItemMeta(), displayName, lore));


                        }

                    } else {

                        if (Material.matchMaterial(materialName) == null) {
                            plugin.getLogger().log(Level.SEVERE, "Invalid material name in treasure prize configuration: " + materialName);
                            continue;
                        }

                        item = new ItemStack(Material.matchMaterial(materialName), amount);
                        item.setItemMeta(Utils.setMeta(item.getItemMeta(), displayName, lore));

                    }

                    local_items_chance.put(item, chance);

                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Invalid item in treasure prize configuration: " + s);
                }


            }

            itemsWithChance.put(world_name, local_items_chance);
        }


    }

    @Deprecated
    private static void loadItems()
    {
        String[] args;
        ItemStack item;

        Material mat;
        int amount;

        String world_name;

        boolean itemsAdder = Utils.isEnabled("ItemsAdder");

        for(String world : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            world_name = plugin.getConfig().getString("settings.enabled-worlds." + world + ".world-name");
            ArrayList<ItemStack> local_items = new ArrayList<>();
            HashMap<ItemStack, Double> local_items_chance = new HashMap<>();

            for(String s : plugin.getConfig().getStringList("settings.enabled-worlds." + world + ".item-rewards"))
            {

                args = s.split(":");

                //String-ul are prea putine informatii
                if(args.length < 2) {

                    plugin.getLogger().log(Level.SEVERE, "Invalid item in treasure prize configuration: " + s);
                    continue;
                }

                if(args.length == 2)
                {

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

                    if(itemsAdder)
                    {

                        CustomStack stack = CustomStack.getInstance(args[0]);

                        if(stack != null)
                        {
                            item = stack.getItemStack();
                            item.setAmount(amount);
                        }
                        else
                        {

                            if(Material.matchMaterial(args[0]) == null)
                            {
                                plugin.getLogger().log(Level.SEVERE, "Invalid material name in treasure prize configuration: " + args[0]);
                                continue;
                            }

                            item = new ItemStack(Material.matchMaterial(args[0]), amount);
                        }

                    }
                    else
                    {

                        if(Material.matchMaterial(args[0]) == null)
                        {
                            plugin.getLogger().log(Level.SEVERE, "Invalid material name in treasure prize configuration: " + args[0]);
                            continue;
                        }

                        item = new ItemStack(Material.matchMaterial(args[0]), amount);
                    }

                    local_items.add(item);

                }
                else if(args.length == 3)
                {

                    if(!Utils.isInt(args[2]))
                    {
                        //DIAMOND:5-10
                        if(args[2].split("-").length == 2)
                        {

                            int min = Integer.parseInt(args[2].split("-")[0]);
                            int max = Integer.parseInt(args[2].split("-")[1]);

                            amount = Utils.randInt(min, max);

                            if(amount <= 0) continue;

                        }
                        else {
                            plugin.getLogger().log(Level.SEVERE, "Invalid amount in treasure prize configuration: " + args[2]);
                            continue;
                        }

                    }
                    else {
                        amount = Integer.parseInt(args[2]);
                    }

                    if(itemsAdder)
                    {
                        CustomStack stack = CustomStack.getInstance(args[0] + ":" + args[1]);

                        if(stack != null)
                        {
                            item = stack.getItemStack();
                            item.setAmount(amount);
                            local_items.add(item);
                        }
                        else
                        {
                            plugin.getLogger().log(Level.SEVERE, "Invalid ItemsAdder item in treasure prize configuration: " + args[0] + ":" + args[1]);
                        }

                    }
                    else
                    {
                        plugin.getLogger().log(Level.SEVERE, "Invalid ItemsAdder item in treasure prize configuration: " + args[0] + ":" + args[1]);
                    }
                }
                else
                {
                    plugin.getLogger().log(Level.SEVERE, "Invalid material name in treasure prize configuration: " + s);
                }

            }

            items.put(world_name, local_items);
        }
    }

    public static void initialize()
    {

        loadItemsReworked();

        Treasure.loadMobs();
        Treasure.loadCommands();

    }

    Hunt(World w, int duration)
    {

        this.w = w;
        this.duration = duration;

        if(Settings.getWorldBooleanUnknown(w.getName(), "spawn-to-certain-coords")) {

            try
            {
                List<String> locations = Settings.getWorldStringListUnknown(w.getName(), "spawn-coords");
                String locationString = locations.get(Utils.randInt(0, locations.size() - 1));

                String[] args = locationString.split(":");
                this.l = new Location(w, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            }
            catch(Exception e)
            {
                Bukkit.getLogger().log(Level.SEVERE, "Error while getting a location from 'spawn-coords'. Check your coordonates, please: " + e.getMessage());
            }
        }
        else {

            double distance = Math.min(Locations.getBorder(w) - 10, Settings.getWorldIntUnknown(w.getName(), "max-treasure-distance"));
            double negativeDistance = -1 * distance;

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
            {

                CompletableFuture<Location> loc = CompletableFuture.supplyAsync(() -> Locations.getRandomLocationMoreThan(w, distance, distance));
                this.l = loc.join();

            });
        }
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

        initialize();

        Bukkit.getScheduler().runTask(plugin, () ->
        {

            treasure = new Treasure(this, getItemsWithChances());

            this.starttime = System.currentTimeMillis();
            treasure.setAlias(getTreasureAlias());
            treasure.create();

            Bukkit.getConsoleSender().sendMessage(Messages.get("treasure-generated-confirmation")
                    .replace("{x}", getLocation().getBlockX() + "")
                    .replace("{z}", getLocation().getBlockZ() + "")
                    .replace("{alias}", treasure.getAlias())
                    .replace("{world}", getLocation().getWorld().getName()));

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

    public String getTreasureAlias()
    {
        return Settings.getWorldStringUnknown(w.getName(), "treasure-name");
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
