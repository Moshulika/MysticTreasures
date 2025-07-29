package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Locations;
import com.Moshu.Misc.Messages;
import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.objects.TreasureData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Hunt {

    private Treasure treasure;
    private Location l;
    private final int duration;
    private long startTime = 0;
    private String treasureTypeString;
    private TreasureData treasureData;

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private static final HashMap<String, Hunt> activeHunts = new HashMap<>();

    /**
     * Gets called on removal of treasure
     */
    public void setInactive()
    {
        activeHunts.remove(treasureTypeString);
    }

    /**
     * The hunt is active but the treasure is not yet generated and active
     * @param id
     */
    private void setActive(String id)
    {
        activeHunts.put(id, this);
    }

    public static Hunt getHuntByIdentifier(String id)
    {
        return activeHunts.get(id);
    }

    public static boolean huntActiveInWorld(World w)
    {
        return !getHuntsInWorld(w).isEmpty();
    }

    public static ArrayList<Hunt> getHuntsInWorld(World w)
    {

        ArrayList<Hunt> hunts = new ArrayList<>();

        for(Hunt h : activeHunts.values())
        {

            if(h.getTreasureData().getWorld().getName().equals(w.getName())) hunts.add(h);

        }

        return hunts;

    }

    Hunt(String treasureTypeString, int duration)
    {

        this.treasureTypeString = treasureTypeString;
        this.duration = duration;

        deserializeTreasureData();

        if(getTreasureData().spawnToCertainCoords()) {


                List<Location> locations = getTreasureData().getSpawnCoords();

                if(!locations.isEmpty()) {
                    this.l = locations.get(Utils.randInt(0, locations.size() - 1));
                }
                else
                {
                    World w = Bukkit.getServer().getWorlds().get(0);
                    this.l = new Location(w, w.getSpawnLocation().getX(), w.getSpawnLocation().getY(), w.getSpawnLocation().getZ());
                }


        }
        else {

            World w = getTreasureData().getWorld();
            double distance = Math.min(Locations.getBorder(w) - 10, getTreasureData().getMaxTreasureDistance());
            int maxTreasureDistance = getTreasureData().getMaxTreasureDistance();

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
            {
                CompletableFuture<Location> loc = CompletableFuture.supplyAsync(() -> Locations.getRandomLocationMoreThan(w, maxTreasureDistance, distance, distance));
                this.l = loc.join();
            });
        }
    }

    Hunt(Location location, String treasureTypeString, int duration)
    {
        this.treasureTypeString = treasureTypeString;
        this.duration = duration;
        this.l = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());

        deserializeTreasureData();

    }

    public void start() {

        if (this.l == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&5&lMystic&d&lTreasures: &fLocation is null, something went wrong."));
            return;
        }

        treasure = new Treasure(this, getTreasureData());

        if(!Locations.isSafeEnough(this.l))
        {
            plugin.getLogger().severe("Spawn location for treasure `" + getTreasureData().getTreasureName() + "` is unsafe. Modify your location!");
            return;
        }

        this.startTime = System.currentTimeMillis();
        setActive(getTreasureData().getIdentifier());
        treasure.create();

        Bukkit.getConsoleSender().sendMessage(Messages.get("treasure-generated-confirmation")
                .replace("{x}", getLocation().getBlockX() + "")
                .replace("{z}", getLocation().getBlockZ() + "")
                .replace("{alias}", getTreasureData().getTreasureName())
                .replace("{world}", getLocation().getWorld().getName()));


    }

    private void deserializeTreasureData()
    {
        this.treasureData = TreasureData.getByIdentifier(treasureTypeString);
    }

    public TreasureData getTreasureData()
    {
        return treasureData;
    }

    public static List<Hunt> getActiveTreasures()
    {

        List<Hunt> h = Collections.synchronizedList(new ArrayList<Hunt>());

        for(Hunt x : activeHunts.values())
        {

            if(x.getTreasure().isActive())
            {
                h.add(x);
            }

        }

        return h;
    }

    /**
     * Hunt here are guaranteed to be active
     * @return a list of all the hunts active
     */
    public static List<Hunt> getHunts()
    {
        List<Hunt> h = Collections.synchronizedList(new ArrayList<Hunt>());
        h.addAll(activeHunts.values());
        return h;
    }

    /**
     * Hunt here are guaranteed to be active
     * @return a list of all the hunts active
     */
    public static List<String> getHuntsIdentifiers()
    {
        List<String> h = Collections.synchronizedList(new ArrayList<>());
        h.addAll(activeHunts.keySet());
        return h;
    }


    public static List<String> getActiveTreasureIdentifiers()
    {

        List<String> h = Collections.synchronizedList(new ArrayList<>());

        for(String x : activeHunts.keySet())
        {

            if(activeHunts.get(x).getTreasure().isActive())
            {
                h.add(x);
            }

        }

        return h;
    }

    public static Hunt getNearestHunt(Location loc)
    {

        Location huntLoc;

        int minDistance = Integer.MAX_VALUE;
        Hunt closestHunt = null;
        Hunt backupHunt = null;

        for(Hunt h : getActiveTreasures())
        {
            huntLoc = h.getLocation();

            if(!huntLoc.getWorld().getName().equals(loc.getWorld().getName()))
            {
                backupHunt = h;
                continue;
            }

            if(huntLoc.distance(loc) < minDistance)
            {
                closestHunt = h;
            }

        }

        if(closestHunt == null && Settings.getBoolean("broadcast-to-all-worlds"))
        {
            return backupHunt;
        }

        return closestHunt;

    }

    public void stop()
    {

        if(getTreasure() == null) return;
        if(!getTreasure().isActive()) return;

        Bukkit.getConsoleSender().sendMessage(Messages.get("treasure-stopped-confirmation")
                .replace("{x}", getLocation().getBlockX() + "")
                .replace("{z}", getLocation().getBlockZ() + "")
                .replace("{alias}", getTreasureData().getTreasureName())
                .replace("{world}", getLocation().getWorld().getName()));

        getTreasure().remove();
    }

    public long getRemainingTime()
    {
        return getStartTime() + TimeUnit.MINUTES.toMillis(getDuration()) - System.currentTimeMillis();
    }

    public static boolean isHuntActive(Location loc)
    {

        Location treasureLoc;

        for(Hunt h : getActiveTreasures())
        {

            treasureLoc = h.getLocation();

            if(treasureLoc.getWorld().getName().equals(loc.getWorld().getName()) &&
            treasureLoc.getBlockX() == loc.getBlockX() && treasureLoc.getBlockY() == loc.getBlockY() &&
            treasureLoc.getBlockZ() == loc.getBlockZ()) return true;

        }

        return false;

    }

    public boolean isHuntActive()
    {
        return treasure.isActive();
    }

    public static boolean isHuntActive(String id)
    {
        return getHuntsIdentifiers().contains(id);
    }

    public static String isActiveString(String id)
    {

        if(isHuntActive(id)) return Messages.get("menu-yes");
        return Messages.get("menu-no");

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

    public long getElapsedTime()
    {
        return getStartTime() - getRemainingTime();
    }

    public long getStartTime()
    {
        return startTime;
    }

    private static ArrayList<String> menuItemLore(List<String> list, Hunt h)
    {

        ArrayList<String> newList = new ArrayList<>();

        for(String s : list)
        {
            newList.add(s.replace("{time}", Utils.getCountDown(h.getRemainingTime()))
                    .replace("{keepers}", "" + h.getTreasure().getRemainingMobs().size())
                    .replace("{x}", "" + h.getLocation().getBlockX())
                    .replace("{y}", "" + h.getLocation().getBlockY())
                    .replace("{world}", h.getLocation().getWorld().getName())
                    .replace("{participants}", "" + h.getTreasure().getParticipants().size())
                    .replace("{key}", "" + h.getTreasureData().getTreasureKey().requiresKey()));
        }

        return newList;
    }

    public static void activeHuntsMenu(Player p)
    {

        Inventory inv = Bukkit.createInventory(null, 27, Messages.get("active-hunts-menu.title"));
        ItemMeta meta;

        String name = Utils.format(Messages.get("active-hunts-menu.name"));

        int i = 0;
        for(Hunt h : getActiveTreasures())
        {

            ItemStack item = new ItemStack(Utils.checkMaterial(h.getTreasureData().getMenuItem()));
            meta = item.getItemMeta();
            if(meta == null) continue;

            meta.setDisplayName(name.replace("{treasure_name}", h.getTreasure().getTreasureData().getTreasureName()));
            meta.setLore(menuItemLore(Messages.getAndFormatList("messages.active-hunts-menu.lore"), h));
            item.setItemMeta(meta);

            inv.setItem(i, item);
            i++;
        }


        Utils.fillWithGlass(inv);
        p.openInventory(inv);

    }

}
