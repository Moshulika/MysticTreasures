package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TreasureKey {

    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public static boolean requiresKey(String world)
    {
        for(String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            if(Settings.getWorldString(s, "world-name").equals(world))
            {

                try
                {
                    return plugin.getConfig().getBoolean("settings.enabled-worlds." + s + ".treasure-key.enabled", false);
                }
                catch(Exception e)
                {
                    plugin.getLogger().log(Level.SEVERE, "Could not get treasure-key setting");
                }

            }

        }

        return false;

    }

    public static ItemStack getTreasureKey(World world)
    {
        return getTreasureKey(world, 1);
    }

    public static ItemStack getTreasureKey(World world, int amount)
    {

        for(String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            if(Settings.getWorldString(s, "world-name").equals(world.getName()))
            {

                try
                {

                    Material material = Material.valueOf(plugin.getConfig().getString("settings.enabled-worlds." + s + ".treasure-key.item", "TRIPWIRE_HOOK"));
                    String name = plugin.getConfig().getString("settings.enabled-worlds." + s + ".treasure-key.name", "&c&l&oTREASURE KEY");
                    List<String> lore = plugin.getConfig().getStringList("settings.enabled-worlds." + s + ".treasure-key.lore");

                    ItemStack item = new ItemStack(material, amount);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(Utils.format(name));

                    ArrayList<String> loreColored = new ArrayList<>();

                    for(String l : lore)
                    {
                        loreColored.add(Utils.format(l));
                    }

                    meta.setLore(loreColored);
                    item.setItemMeta(meta);

                    return item;

                }
                catch(Exception e)
                {
                    plugin.getLogger().log(Level.SEVERE, "Could not get treasure-key setting");
                }

            }

        }

        return new ItemStack(Material.TRIPWIRE_HOOK);

    }

    public static boolean isTreasureKey(World world, ItemStack apparentKey)
    {

        ItemStack realKey = getTreasureKey(world);

        if(realKey.getType() == apparentKey.getType())
        {

            if(apparentKey.getItemMeta() == null) return false;
            if(apparentKey.getItemMeta() == null && realKey.getItemMeta() == null) return true;

            if(realKey.getItemMeta().getDisplayName().equals(apparentKey.getItemMeta().getDisplayName()))
            {

                List<String> lore_real = realKey.getItemMeta().getLore();
                List<String> lore_apparent =  apparentKey.getItemMeta().getLore();

                if(lore_apparent.containsAll(lore_real)) return true;

            }

        }

        return false;

    }

}
