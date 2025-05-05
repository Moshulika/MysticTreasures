package com.Moshu.Misc;

import com.Moshu.Main;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.annotation.Nullable;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities class, lots of useful stuff
 */
public class Utils
{

    public static Main plugin;
    public static File kitsf;

    /**
     * @hidden
     */
    public Utils(Main plugin)
    {

        Utils.plugin = plugin;
    }

    public static double randDouble(double min, double max)
    {
        double x = ThreadLocalRandom.current().nextDouble(min, max);

        return x;
    }

    public static double round(double value)
    {
        String mny = new DecimalFormat("##.##").format(value);
        double d = Double.parseDouble(mny);
        return d;
    }

    public static float round(float value)
    {
        String mny = new DecimalFormat("##.#").format(value);
        float d = Float.parseFloat(mny);
        return d;
    }

    public static void addItemFlags(ItemMeta meta)
    {
        meta.addItemFlags(ItemFlag.values());
    }

    /**
     * Gets the location for the particles
     * @return the location for the particles
     */
    public static Location getParticleLocation(Location loc)
    {

        double x,y,z;

        x = randDouble(0.1, 0.9);
        y = randDouble(1, 1.9);
        z = randDouble(0.1, 0.9);

        return new Location(loc.getWorld(), loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
    }

    /**
     * Checks if the server is running Paper or spigot.
     * @return true/false
     */
    public static boolean isPaper()
    {

        boolean isPaper = false;
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            isPaper = true;
        } catch (ClassNotFoundException ignored) {
        }

        return isPaper;

    }

    /**
     * Get a list of the materials of the nearby blocks
     * @param location the location you want to seach
     * @param radius the radius
     * @return the list containing the materials
     */
    public static List<Material> getNearbyBlocks(Location location, int radius)
    {
        List<Material> blocks = new ArrayList();
        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z).getType());
                }
            }
        }
        return blocks;
    }

    /**
     * Get a list of the nearby blocks
     * @param location the location you want to seach
     * @param radius the radius
     * @return the list containing the blocks
     */
    public static List<Block> getNearbyBlocks2(Location location, int radius)
    {
        List<Block> blocks = new ArrayList();
        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    /**
     * Parse a ChatColor object from a string
     * @param s the string
     * @return a ChatColor
     */
    public static ChatColor getColor(String s)
    {

        char[] a = s.toCharArray();
        int i = 0;

        for(char c : a)
        {

            if(i + 1 < a.length) {

                if (c == '&') {

                    return ChatColor.getByChar(a[i + 1]);
                }

                i++;
            }

        }

        return ChatColor.WHITE;

    }

    /**
     * Extracts an integer from a string
     * @param s the string
     * @return the found integer
     */
    public static int extractInt(String s)
    {

        s = s.replaceAll("[^\\d]", " ");
        s = s.trim();
        s = s.replaceAll(" +", " ");

        //Main.consoleMessage("RESULT: " + s);

        if(s.equals("") || s.isEmpty()) return 0;
        if(isInt(s)) return Integer.parseInt(s);
        return 0;

    }

    public static ItemMeta setMeta(ItemMeta meta, String displayName, List<String> lore)
    {

        if(displayName.isEmpty() || lore.isEmpty()) return meta;

        meta.setDisplayName(Utils.format(displayName));

        ArrayList<String> coloredLore = new ArrayList<>();

        for(String s : lore)
        {
            coloredLore.add(Utils.format(s));
        }

        meta.setLore(coloredLore);

        return meta;

    }

    /**
     * Get a list of all the online players name
     * @return a list with all the online players namr
     */
    public static ArrayList<String> getOnlinePlayersNames()
    {
        ArrayList<String> x = new ArrayList<>();

        for(Player k : Bukkit.getOnlinePlayers())
        {
            x.add(k.getName());
        }

        return x;
    }

    /**
     * Get a list of all the online players name
     * @return a list with all the online players namr
     */
    public static ArrayList<String> getWorldsNames()
    {
        ArrayList<String> x = new ArrayList<>();

        for(World k : Bukkit.getWorlds())
        {
            x.add(k.getName());
        }

        return x;
    }

    /**
     * Transforms the auto-message into a normal string
     * @param r the message
     * @return the formatted string
     */
    public static String formatAutoMessage(String r)
    {



        return Utils.setCapitals(ChatColor.stripColor(r).trim().replace("\n", "")
                .replace("Stiai ca? ", "").replace("&e", "").replace("&l", "")
                .replace("&f", ""));
    }

    /**
     * Check if the player has at least one of the ItemStack provided
     * @param p the player
     * @param is the itemstack
     * @return true/false
     */
    public static boolean hasItem(Player p, ItemStack is)
    {

        for(ItemStack i : p.getInventory().getContents())
        {

            if(i == null || i.getType() == Material.AIR) continue;

            if(i.getType().equals(is.getType()))
            {

                if(i.getItemMeta() == null || is.getItemMeta() == null) return true;

                if(i.getItemMeta().getDisplayName().equals(is.getItemMeta().getDisplayName()))
                {
                    return true;
                }

            }

        }

        return false;

    }



    /**
     * Removes all items of that type from the player's inventory
     * @param p the player
     * @param is the item to be removed
     */
    public static void removeItem(Player p, ItemStack is)
    {
        for(ItemStack i : p.getInventory().getContents())
        {

            if(i == null || i.getType() == Material.AIR) continue;

            if(i.getType().equals(is.getType()))
            {

                if(i.getItemMeta() == null || is.getItemMeta() == null) {
                    p.getInventory().remove(i);
                    break;
                }

                if(i.getItemMeta().getDisplayName().equals(is.getItemMeta().getDisplayName()))
                {
                    p.getInventory().remove(i);
                    break;
                }

            }

        }

    }



    /**
     * Substracts the amount provided of item from the player's inventory
     * @param p the player
     * @param is the item to be substracted
     * @param amount the amount of item to be substracted
     */
    public static void substractItem(Player p, ItemStack is, int amount)
    {

        ItemStack item;
        int a;

        for(int i = 0; i < p.getInventory().getContents().length; i++)
        {

            if(p.getInventory().getContents()[i] == null) continue;

            item = p.getInventory().getContents()[i];
            if(item.getType() != is.getType()) continue;
            a = item.getAmount();

            if(a - amount <= 0)
            {
                p.getInventory().setItem(i, null);
            }
            else
            {
                item.setAmount(a - amount);
            }

            break;

        }

    }

    /**
     * Substracts the amount provided of item from the player's inventory
     * @param p the player
     * @param is the item to be substracted
     * @param amount the amount of item to be substracted
     */
    public static void substractItemUnlimited(Player p, ItemStack is, int amount)
    {

        ItemStack item;
        int a;

        int remaining = amount;

        for(int i = 0; i < p.getInventory().getContents().length; i++)
        {

            if(remaining <= 0) break;
            if(p.getInventory().getContents()[i] == null) continue;

            item = p.getInventory().getContents()[i];
            if(item.getType() != is.getType()) continue;
            a = item.getAmount();

            if(a - amount <= 0)
            {
                p.getInventory().setItem(i, null);
            }
            else
            {
                item.setAmount(a - remaining);
            }

            remaining = remaining - a;

        }

    }

    /**
     * Substracts the amount of the item from the inventory
     * @param inv the inventory where the item is located
     * @param is the item to be substracted
     * @param amount the amount of item to be substracted
     */
    public static void substractItem(Inventory inv, ItemStack is, int amount)
    {

        ItemStack item;
        int a;

        for(int i = 0; i < inv.getContents().length; i++)
        {

            if(inv.getItem(i) == null) continue;

            item = inv.getItem(i);

            if(item.getType() != is.getType()) continue;
            a = item.getAmount();

            if(a - amount <= 0)
            {
                inv.setItem(i, null);
            }
            else
            {
                item.setAmount(a - amount);
            }

            break;


        }

    }

    /**
     * Counts identical items
     * @param p the player to count the items from
     * @param is the item to be counted
     * @return how many items are there
     */
    public static int countItemsOfType(Player p, ItemStack is)
    {

        int x = 0;

        for(ItemStack i : p.getInventory().getContents())
        {

            if(i == null || i.getType() == Material.AIR) continue;

            if(i.getType() == is.getType())
            {

                x += i.getAmount();

            }

        }

        return x;

    }

    /**
     * @return a checkmark
     */
    public static String succesSymbol()
    {
        return "&8(&a✔&8) &f";
    }

    /**
     * @return an x
     */
    public static String errorSymbol()
    {
        return "&8(&c❌&8) &f";
    }


    /**
     * Adds the item to the player's inventory
     * @param p the player
     * @param a the items to be added
     */
    public static void addToInventory(Player p, ItemStack a) {


        if (!Utils.hasFullInventory(p)) {

            if (a == null || a.getType() == Material.AIR) return;

            p.getInventory().addItem(a);

        } else {

            if (a == null || a.getType() == Material.AIR) return;

            p.getWorld().dropItemNaturally(p.getLocation(), a);

        }


    }

    /**
     * Adds all the items provided in the array to the player's inventory
     * @param p the player
     * @param is the items to be added
     */
    public static void addToInventory(Player p, ItemStack[] is)
    {

        if (Utils.getFreeSlots(p.getInventory()) >= is.length) {

            for (ItemStack a : is) {

                if (a == null || a.getType().equals(Material.AIR)) {
                    continue;
                }

                p.getInventory().addItem(a);


            }

        }
        else
        {

            for(ItemStack a : is)
            {

                if (!Utils.hasFullInventory(p)) {

                    if(a == null || a.getType() == Material.AIR)
                    {
                        continue;
                    }

                    p.getInventory().addItem(a);

                } else {

                    if(a == null || a.getType() == Material.AIR)
                    {
                        continue;
                    }

                    p.getWorld().dropItemNaturally(p.getLocation(), a);

                }

            }

        }
    }

    /**
     * Calculates the milliseconds until the next sharp hour (e.g. 1:00)
     * @param calendar an instance of Calendar
     * @return how many millies until the next hour
     */
    public static long millisToNextHour(Calendar calendar) {
        int minutes = calendar.get(12);
        int seconds = calendar.get(13);
        int millis = calendar.get(14);
        int minutesToNextHour = 60 - minutes;
        int secondsToNextHour = 60 - seconds;
        int millisToNextHour = 1000 - millis;
        return minutesToNextHour * 60 * 1000 + secondsToNextHour * 1000 + millisToNextHour;
    }


    /**
     * Fill an inventory with colored glass, based on holiday
     * @param inv the inventory to be filled
     */
    public static void fillWithGlass(Inventory inv)
    {

        Material mat;

        try
        {
            mat = Material.valueOf(plugin.getConfig().getString("settings.menu.glass" , "BLACK_STAINED_GLASS_PANE"));
        }
        catch (Exception e)
        {
            mat = Material.BLACK_STAINED_GLASS_PANE;
            plugin.getLogger().log(Level.SEVERE, "No such material for glass!");
        }

        ItemStack sticla = new ItemStack(mat);

        ItemMeta sticlam = sticla.getItemMeta();
        sticlam.setDisplayName(" ");
        sticlam.getItemFlags().add(ItemFlag.HIDE_ATTRIBUTES);
        sticla.setItemMeta(sticlam);

        ItemStack sticlafinal = sticla;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {
            for(int i = 0; i < inv.getSize(); i++)
            {

                if(inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR)
                {

                    inv.setItem(i, sticlafinal);

                }

            }
        });
    }

    /**
     * Get the color of the glass
     * @return an ItemStack with the glass having the appropriate color for the holiday
     */
    public static ItemStack getGlass()
    {

        ItemStack sticla = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

        ItemMeta sticlam = sticla.getItemMeta();
        sticlam.setDisplayName(" ");
        sticlam.getItemFlags().add(ItemFlag.HIDE_ATTRIBUTES);
        sticla.setItemMeta(sticlam);

        return sticla;

    }

    /**
     * Fills the inventory with black glass
     * @param inv the inventory to be filled
     */
    @Deprecated
    public static void fillWithGlassLegacy(Inventory inv)
    {

        ItemStack sticla = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

        ItemMeta sticlam = sticla.getItemMeta();
        sticlam.setDisplayName(" ");
        sticlam.getItemFlags().add(ItemFlag.HIDE_ATTRIBUTES);
        sticla.setItemMeta(sticlam);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {
            for(int i = 0; i < inv.getSize(); i++)
            {

                if(inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR)
                {

                    inv.setItem(i, sticla);

                }

            }
        });
    }


    /**
     * Calculates the middle of 2 locations
     * @param l1 first location
     * @param l2 second location
     * @return the middle location
     */
    public static Location middle(Location l1, Location l2)
    {

        if(l1.getWorld() != l2.getWorld()) return l1;

        double x1, x2, y1, y2, z1, z2, x, y, z;

        x1 = l1.getBlockX();
        x2 = l2.getBlockX();

        y1 = l1.getBlockY();
        y2 = l2.getBlockY();

        z1 = l1.getBlockZ();
        z2 = l2.getBlockZ();

        x = (x1 + x2) / 2;
        y = (y1 + y2) / 2;
        z = (z1 + z2) / 2;

        return new Location(l1.getWorld(), x + 0.5, y, z + 0.5);
    }

    /**
     * Format time in string
     * @param minute time to be formatted
     * @return the formatted string
     */
    public static String formatRemainingTime(long minute)
    {

        long ore = minute / 60;
        long zile = ore / 24;

        minute = minute - (ore * 60);
        ore = ore - (zile * 24);

        //Daca exista mai multe zile
        if(zile != 0)
        {
            return zile + " day(s), " + ore + " hour(s), " + minute + " minutes";
        }
        //Daca e mai putin de o zi
        else
        {
            //Daca e mai mult de o ora
            if(ore != 0)
            {
                return ore + " hour(s), " + minute + " minutes";
            }

            return minute + " minutes";

        }

    }

    /**
     * Check a string for numbers
     * @param s the string
     * @return true/false
     */
    public static boolean containsNumbers(String s)
    {

        for(char c : s.toCharArray())
        {

            if(isInt(c))
            {
                return true;
            }

        }

        return false;
    }

    public static ItemStack addEnchants(ItemStack itemStack, List<String> enchantments)
    {

        String[] args;
        String enchantment;
        Enchantment enchant;
        int level;

        for(String s : enchantments)
        {

            args = s.split(":");
            enchantment = args[0];

            if(!Utils.isInt(args[1]))
            {
                plugin.getLogger().warning("Enchantment level needs to be a number! Affected enchantment: " +  s);
                continue;
            }

            level = Integer.parseInt(args[1]);
            enchant = Enchantment.getByName(enchantment);

            if(enchant == null)
            {
                plugin.getLogger().warning("Enchantment is invalid! Affected enchantment: " +  s);
                continue;
            }

            if(!enchant.canEnchantItem(itemStack))
            {
                plugin.getLogger().warning("Enchantment " + enchantment + " can not be used on " + itemStack.getType().toString());
                continue;
            }

            if(level > enchant.getMaxLevel())
            {
                plugin.getLogger().warning("Max level for enchantment " + enchantment + " is " + level);
                itemStack.addEnchantment(enchant, enchant.getMaxLevel());
            }
            else
            {
                itemStack.addEnchantment(enchant, level);
            }


        }

        return itemStack;

    }

    public static ItemStack addUnsafeEnchants(ItemStack itemStack, List<String> enchants)
    {

        String[] args;
        String enchantment;
        Enchantment enchant;
        int level;

        for(String s : enchants)
        {

            args = s.split(":");
            enchantment = args[0];

            if(!Utils.isInt(args[1]))
            {
                plugin.getLogger().warning("Enchantment level needs to be a number! Affected enchantment: " +  s);
                continue;
            }

            level = Integer.parseInt(args[1]);
            enchant = Enchantment.getByName(enchantment);

            if(enchant == null)
            {
                plugin.getLogger().warning("Enchantment is invalid! Affected enchantment: " +  s);
                continue;
            }

            itemStack.addUnsafeEnchantment(enchant, level);

        }

        return itemStack;
    }


    /**
     * Send an error as an item with custom display name.
     * When a player clicks on an item in an inventory and an error happens,
     * the item will transform into a Material.BARRIER and the error message will be displayed
     * for a brief period of time, after which the item will reappear in the menu as before.
     * @param is the inventory holding the item
     * @param error the error (should be as short as possible)
     */
    public static void errorAsItem(ItemStack is, String error)
    {

            Material initialmat = is.getType();
            String initialname = is.getItemMeta().getDisplayName();

            ItemMeta im = is.getItemMeta();

            is.setType(Material.BARRIER);
            im.setDisplayName(Utils.format( "&c" + error));
            is.setItemMeta(im);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
            {
                is.setType(initialmat);
                im.setDisplayName(format(initialname));
                is.setItemMeta(im);
            }, 100);

    }

    public static ArrayList<String> formatList(List<String> list)
    {

        ArrayList<String> newList = new ArrayList<>();

        for(String s : list)
        {
            newList.add(Utils.format(s));
        }

        return newList;

    }

    /**
     * Sends a permission error like #errorAsItem(ItemStack, String)
     * @param p the player's inventory
     * @param permission the permission he needs to have
     * @param is the itemstack to affect
     */
    public static void setBarrier(Player p, String permission, ItemStack is)
    {
        if(!p.hasPermission(permission))
        {
            Material initialmat = is.getType();
            String initialname = is.getItemMeta().getDisplayName();

            ItemMeta im = is.getItemMeta();

            is.setType(Material.BARRIER);
            im.setDisplayName(Utils.format( "&cNo permission!"));
            is.setItemMeta(im);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
            {
                is.setType(initialmat);
                im.setDisplayName(format( initialname));
                is.setItemMeta(im);
            }, 100);

        }
    }


    /**
     * Formats a message with legacy color codes and also HEX
     * @param message the message you want to apply colors to
     * @return the formatted string
     */
    public static String format(String message) {
//ceva nu merge aici daca e stringu prea mic cred si de aici se fute globalu??

        if (message == null || message.length() == 0) return message;

        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);

        String color;

        while (matcher.find()) {
            color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, net.md_5.bungee.api.ChatColor.of(color) + "");
            matcher = pattern.matcher(message);
        }


        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Get a ChatColor from a message
     * @param message the message
     * @return the ChatColor, or white if the method doesn't find any colors
     */
    public static net.md_5.bungee.api.ChatColor getColorFromHex(String message)
    {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);

        net.md_5.bungee.api.ChatColor x = net.md_5.bungee.api.ChatColor.WHITE;

        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            x = net.md_5.bungee.api.ChatColor.of(color);
        }

        return x;
    }

    /**
     * Get a HEX code from a message
     * @param message the message
     * @return the HEX code, or white if the method doesn't find any colors
     */
    public static String getHex(String message)
    {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);

        net.md_5.bungee.api.ChatColor x = net.md_5.bungee.api.ChatColor.WHITE;

        String color = "";

        while (matcher.find()) {
            color = message.substring(matcher.start(), matcher.end());
        }

        return color;
    }

    /**
     * Removes all HEX colors from a string
     * @param s the string
     * @return the string without the hex code
     */
    public static String removeHex(String s)
    {

        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(s);

        String color = "";

        while(matcher.find())
        {
            color = s.substring(matcher.start(), matcher.end());
        }

        return color;

    }

    /**
     * Check if a plugin is enabled
     * @param plugin the plugin to check
     * @return true/false
     */
   public static boolean isEnabled(String plugin)
   {

       return Bukkit.getPluginManager().getPlugin(plugin) != null && Bukkit.getPluginManager().getPlugin(plugin).isEnabled();

   }

    /**
     * Sets a capital letter on the first word of the string
     * @param s the string
     * @return the formatted string
     */
    public static String setCapitals(String s)
    {

        if(s.length() < 1) return s;

        String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
        return cap;
    }

    /**
     * Generate a random number 1-100
     * @return a random number 1-100
     */
    public static int chance()
    {

        Random r = new Random();
        return r.nextInt(101);

    }

    /**
     * Get a list of all the entities near the location in a radius in a chunk
     * @param l the location
     * @param radius the radius
     * @return the list containing all the entities
     */
    public static ArrayList<Entity> getNearbyEntities(Location l, int radius)
    {
        ArrayList<Entity> en = new ArrayList<>();

        if(l.getWorld().getEntities().size() != 0) {


            for (Entity e : l.getWorld().getEntities()) {


                    if (l.distance(e.getLocation()) <= radius) {

                        en.add(e);

                    }

            }
        }
        return en;
    }

    /**
     * Get a list of all the entities near the location in a radius in a chunk
     * @param l the location
     * @param radius the radius
     * @return the list containing all the entities
     */
    public static ArrayList<Entity> getNearbyEntities(Location l, EntityType et, int radius)
    {
        ArrayList<Entity> en = new ArrayList<>();

        if(l.getWorld().getEntities().size() != 0) {

            for (Entity e : l.getWorld().getEntities()) {

                if(e.getType() != et) continue;

                if (l.distance(e.getLocation()) <= radius) {

                    en.add(e);

                }

            }
        }
        return en;
    }

    /**
     * Get a list of all the entities near the location in a radius in a chunk
     * @param l the location
     * @param radius the radius
     * @return the list containing all the entities
     */
    public static ArrayList<Entity> getNearbyItemsOfType(Location l, Material mat, int radius)
    {
        ArrayList<Entity> en = new ArrayList<>();

        Bukkit.getScheduler().runTask(plugin, () ->
        {

            Item i;

            if(l.getWorld().getEntities().size() != 0) {


                for (Entity e : l.getWorld().getEntities()) {

                    if(e instanceof Item) {

                        i = (Item) e;

                        if(i.getItemStack().getType() == mat) {

                            if (l.distance(e.getLocation()) <= radius) {

                                en.add(e);

                            }

                        }

                    }

                }
            }

        });

        return en;
    }

    /**
     * Get a list of all the entities near the location in a radius in a chunk
     * @param l the location
     * @param radius the radius
     * @return the list containing all the entities
     */
    public static ArrayList<Entity> getNearbyItems(Location l, int radius)
    {
        ArrayList<Entity> en = new ArrayList<>();

        l.getChunk().getEntities();
        for (Entity e : l.getChunk().getEntities()) {

            if (e instanceof Item) {

                if (l.distance(e.getLocation()) <= radius) {

                    en.add(e);

                }

            }

        }
        return en;
    }


    /**
     * Get a list of all the living entities near the location in a radius
     * @param l the location
     * @param radius the radius
     * @return the list containing all the entities
     */
    public static ArrayList<Entity> getAllNearbyEntities(Location l, int radius)
    {
        ArrayList<Entity> en = new ArrayList<>();

        if(l.getWorld().getEntities().size() != 0) {


            for (Entity e : l.getWorld().getLivingEntities()) {

                if (l.distance(e.getLocation()) <= radius) {

                    en.add(e);

                }

            }
        }
        return en;
    }

    /**
     * Makes an ItemStack's name much nicer
     * @param is the itemstack
     * @return the formatted string
     */
    public static String formatItemStack(ItemStack is)
    {
        return is.getType().toString().toLowerCase().replace("_", " ");
    }

    /**
     * Get all players in a radius around the location
     * @param l the location
     * @param radius the radius
     * @return a list containing all the players near that locations in the specified radius
     */
    public static ArrayList<Player> getNearbyPlayers(Location l, int radius)
    {
        ArrayList<Player> en = new ArrayList<>();

            for (Player p : l.getWorld().getPlayers()) {

                    if (l.distance(p.getLocation()) <= radius) {
                        en.add(p);
                    }

                }

        return en;
    }

    /**
     * Generates a random integer between two values
     * @param min the min value
     * @param max the max value
     * @return the random integer
     */
    public static int randInt(int min, int max)
    {
        int x = ThreadLocalRandom.current().nextInt(min, max + 1);

        return x;
    }

    /**
     * Gets the location in front of the player
     * @param loc the location
     * @param distance how far away the new location should be
     * @return the location in front of the player
     */
    public static Location getPositionInFrontOfPlayer(Location loc, int distance)
    {
        return loc.add(loc.getDirection().multiply(distance));
    }

    /**
     * Get the highest block at a location
     * @param world the world
     * @param x the x coordinate
     * @param z the z coordinate
     * @return the location with the highest block
     */
    public static Location getHighestBlock(World world, int x, int z, Location backup)
    {

        int i = 255;

            while (i >= 0) {
                if (!new Location(world, x, i, z).getBlock().isEmpty())
                {
                    return new Location(world, x, i, z).add(0.0D, 1.0D, 0.0D);
                }
                i--;
            }

        return backup;
    }

    /**
     * Get the highest block at a location for a nether world
     * @param world the world
     * @param x the x coordinate
     * @param z the z coordinate
     * @return the location with the highest block
     */
    public static Location getHighestBlockNether(World world, int x, int z, Location backup)
    {

        int i = 31;

        while (i <= 120) {
            if (new Location(world, x, i, z).getBlock().isEmpty()) {
                return new Location(world, x, i, z);
            }
            i++;
        }

        return backup;
    }

    /**
     * Get the highest block at a location for an end world
     * @param world the world
     * @param x the x coordinate
     * @param z the z coordinate
     * @return the location with the highest block
     */
    public static Location getHighestBlockEnd(World world, int x, int z)
    {

        int i = 15;

        while (i <= 255) {
            if (new Location(world, x, i, z).getBlock().isEmpty()) {
                return new Location(world, x, i, z);
            }
            i++;
        }

        return world.getSpawnLocation();
    }

    /**
     * A countdown of type mm:SS
     * @param l the number of minutes
     * @return the formatted string
     */
    public static String getCountDown(long l)
    {
        //15:59
        int minutes = (int) TimeUnit.MILLISECONDS.toSeconds(l) / 60;
        int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(l) - TimeUnit.MINUTES.toSeconds(minutes));

        return minutes + ":" + seconds;
    }

    /**
     * @hidden
     */
    public static void readClassName()
    {

        String className = "com.Moshu.Main";
        String path = "/" + className.replace('.', '/') + ".class";

        try (InputStream in = Main.class.getResourceAsStream(path);

             DataInputStream dis = new DataInputStream(in)) {

            int magic = dis.readInt(); // 0xCAFEBABE
            int minor = dis.readUnsignedShort();
            int major = dis.readUnsignedShort();

            plugin.getLogger().log(Level.INFO, "Class file version: " + major + "." + minor + ", running on: " + System.getProperty("java.class.version"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @hidden
     */
    public static void sendNotPlayer()
    {
        Bukkit.getConsoleSender().sendMessage(Utils.format( "&c&lConsole > &fYou need to be a player in order to use this command."));
    }

    /**
     * @hidden
     */
    public static void sendSound(Player p)
    {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
    }

    /**
     * Send a player a certain sound
     * @param p the player
     * @param s the sound
     */
    public static void sendSound(Player p, Sound s)
    {
        p.playSound(p.getLocation(), s, 1.0F, 1.0F);
    }

    /**
     * @hidden
     */
    public static void sendSoundHigh(Player p)
    {
        p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
    }

    /**
     * @hidden
     */
    public static void sendBreakSound(Player p)
    {

        String originalName = plugin.getConfig().getString("settings.negative-sound", "BLOCK_ANVIL_BREAK");
        String soundName = originalName.toLowerCase().replace("_", ".");
        Sound sound = null;

        try {

            NamespacedKey key = NamespacedKey.minecraft(soundName);
            sound = Registry.SOUNDS.get(key);

        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            try {
                sound = Sound.valueOf(originalName);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid sound: " + soundName);
            }
        }

        if (sound != null) {
            p.playSound(p.getLocation(), sound, 1.0F, 1.0F);
        }

        /*
        Sound s;

        try
        {
            String soundString = plugin.getConfig().getString("settings.negative-sound", "BLOCK_ANVIL_BREAK");
            s = Registry.SOUNDS.get(Key.key(soundString));
            p.playSound(p.getLocation(), s, 1.0F, 1.0F);
        }
        catch (IllegalArgumentException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Invalid negative sound", e);
        }

         */

    }

    /**
     * @hidden
     */
    public static void sendLevelupSound(Player p)
    {

        String originalName = plugin.getConfig().getString("settings.positive-sound", "ENTITY_PLAYER_LEVELUP");
        String soundName = originalName.toLowerCase().replace("_", ".");
        Sound sound = null;

        try {

            NamespacedKey key = NamespacedKey.minecraft(soundName);
            sound = Registry.SOUNDS.get(key);

        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            try {
                sound = Sound.valueOf(originalName);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid sound: " + soundName);
            }
        }

        if (sound != null) {
            p.playSound(p.getLocation(), sound, 1.0F, 1.0F);
        }

    }

    /**
     * Check if a string contains only letters
     * @param s the string
     * @return true/false
     */
    public static boolean validString(String s)
    {
        String regex = "^[a-zA-Z]*";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);

        return matcher.matches();
    }

    /**
     * Check if a string contains only alphanumerical characters
     * @param s the string
     * @return true/false
     */
    public static boolean validAlphanumericString(String s)
    {
        String regex = "^[a-zA-Z0-9]+$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);

        return matcher.matches();
    }

    /**
     * Checks if the player has all the slots of the inventory occupied
     * @param p the player
     * @return true/false
     */
    public static boolean hasFullInventory(Player p)
    {
        return p.getInventory().firstEmpty() == -1;
    }

    /**
     * Checks if the player has all the slots of the inventory occupied
     * @param p the player
     * @return true/false
     */
    public static boolean hasEmptySlot(Player p)
    {

        for(ItemStack is : p.getInventory().getContents())
        {

            if(is == null || is.getType() == Material.AIR) return true;

        }

        return false;

    }

    /**
     * Temporarily gives invulnerability to a player
     * @param p the player
     * @param seconds how many seconds should the effect last
     */
    public static void tempGod(Player p, int seconds)
    {

        if(p.isInvulnerable()) return;

        p.setInvulnerable(true);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
        {

            if(Bukkit.getPlayer(p.getName()) != null)
            {
                p.setInvulnerable(false);
            }

        }, seconds * 20L);

    }

    /**
     * Get how many free slots the inventory has
     * @param inv the inventory
     * @return how many free slots are in the inventory
     */
    public static int getFreeSlots(Inventory inv)
    {

        int x = 0;

        for(int i = 0; i < inv.getSize(); i++)
        {

            if(inv.getItem(i) == null || inv.getItem(i).getType().equals(Material.AIR))
            {
                x++;
            }


        }

        return x;

    }

    public static String setInternalPlaceholders(Player p, String s)
    {

        s = s.replace("%player%", p.getName());
        return s;

    }

    public static void trySpawningParticle(Location location, Particle p)
    {
        try
        {
            location.getWorld().spawnParticle(p, location.clone().add(0, 3, 0), 1);
        }
        catch (Exception e)
        {
            plugin.getLogger().log(Level.SEVERE, "Invalid particle for falling treasure!");
        }
    }

    public static ItemStack checkMaterial(String mat)
    {

        Material m;

        try {

            m = Material.getMaterial(mat);
            return new ItemStack(m);

        }
        catch (NullPointerException e) {
            plugin.getLogger().log(Level.SEVERE, "Invalid material " + mat  , e);
        }

        return new ItemStack(Material.STONE);

    }

    /**
     *
     * @param world, world to teleport into
     * @param max, max coordinates to teleport
     * @return the highest block at a locations
     */

    public static Location randomCoordonatesMoreThan(World world, double max, double min)
    {

        //num = (Math.random() * (2 * MAX + 1)) - MAX;
        int x = (int) ((Math.random() * 2 * max + 1) - min);
        int z = (int) ((Math.random() * 2 * max + 1) - min);

        //plugin.getLogger().log(Level.INFO, "Debug: Initial spawn location candidate - X: " + x + ", Z: " + z);

        double negativeMin = -1 * min;

        while(x < negativeMin || z < negativeMin || x > max || z > max)
        {
            //plugin.getLogger().log(Level.INFO, "Debug: Spawn location candidate - X: " + x + ", Z: " + z);
            x = (int) ((Math.random() * 2 * max + 1) - min);
            z = (int) ((Math.random() * 2 * max + 1) - min);
        }

        return getHighestBlock(world, x, z, world.getSpawnLocation());
    }


    /**
     * Check if an inventory is empty
     * @param inv the inventory
     * @return true/false
     */
    public static boolean isEmpty(Inventory inv)
    {
        for(ItemStack it : inv.getContents())
        {
            if(it != null) return false;
        }

        return true;
    }

    /**
     * Get the last char of the string
     * @param s the string
     * @return the last character
     */
    public static String lastChar(String s)
    {
        return s.substring(s.length() - 1);
    }

    /**
     *
     * @param s The time you wish to transform, formatted. (ex: 1d, 3h, 30m)
     * @return The millies corresponding to the value, defaults at minutes
     */
    public static long getMillies(String s)
    {

        if(!s.isEmpty())
        {

            if(isInt(s)) return 0;
            if(!containsNumbers(s)) return 0;

            long l = Long.parseLong(s.replaceAll("[a-zA-z]", "").trim());

            if(lastChar(s).equals("d"))
            {
                return TimeUnit.DAYS.toMillis(l);
            }
            else if(lastChar(s).equals("h"))
            {
                return TimeUnit.HOURS.toMillis(l);

            }
            else if(lastChar(s).equals("m"))
            {
                return TimeUnit.MINUTES.toMillis(l);
            }
            else
            {
                return 0;
            }

        }

        return 0;

    }

    /**
     * Check if a string ends with a caracter used to distinguish time (like m for minutes, h for hours, d for days, etc)
     * @param s the string
     * @return true/false
     */
    public static boolean endsWithSpecialCharacter(String s)
    {
        return s.endsWith("m") || s.endsWith("h") || s.endsWith("d");
    }

    /**
     * Check if a String is actually an int
     * @param str the string
     * @return true/false
     */
    public static boolean isInt(String str)
    {
        try
        {
            Integer.parseInt(str);
            return true;
        }
        catch (NumberFormatException e) {}
        return false;
    }

    /**
     * Check if a char is actually an int
     * @param c the char
     * @return true/false
     */
    public static boolean isInt(char c)
    {
        try
        {
            Integer.parseInt(String.valueOf(c));
            return true;
        }
        catch (NumberFormatException e) {}
        return false;
    }


    /**
     * Check if a String is actually a double
     * @param str the string
     * @return true/false
     */
    public static boolean isDouble(String str)
    {
        try
        {
            Double.parseDouble(str);
            return true;
        }
        catch (NumberFormatException e) {}
        return false;
    }


    /**
     * Convert seconds to hours
     * @param seconds the time in seconds
     * @return the rounded hours
     */
    public static int hoursFromSeconds(int seconds)
    {
        return seconds / 3600;
    }

    /**
     * Get a property from server.properties file
     * @param s the string you want to get
     * @param f the file you wish to access
     * @return the value you wish to get
     */
    public static String getProperty(String s, File f)
    {
        Properties pr = new Properties();

        try
        {
            FileInputStream in = new FileInputStream(f);
            pr.load(in);
            String string = pr.getProperty(s);
            return string;
        }

        catch (IOException e)
        { }

        return "";
    }

    /**
     * Get the main world of the server
     * @return the main world's name
     */
    public static String getMainWorld()
    {

        File s = new File("server.properties");
        return getProperty("level-name", s);

    }

}

