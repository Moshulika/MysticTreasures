package com.Moshu.Misc;


import com.Moshu.Main;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Settings {

    private static Main plugin;

    public Settings(Main plugin)
    {
        this.plugin = plugin;
    }

    public static List<String> getAllowedWorlds()
    {
        return plugin.getConfig().getStringList("settings.allowed-worlds");
    }

    public static boolean actionbar()
    {
        return plugin.getConfig().getBoolean("settings.actionbar.enabled", true);
    }

    public static int actionbarRefresh()
    {
        return plugin.getConfig().getInt("settings.actionbar.refresh", 2);
    }

    public static String getWorldStringUnknown(String world, String setting)
    {

        for(String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            if(getWorldString(s, "world-name").equals(world))
            {
                return plugin.getConfig().getString("settings.enabled-worlds." + s + "." + setting, "Null value");
            }

        }

        return "World not found";

    }

    public static Material getWorldMaterialUnknown(String world, String setting)
    {

        for(String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            if(getWorldString(s, "world-name").equals(world))
            {

                try
                {
                    return Material.valueOf(plugin.getConfig().getString("settings.enabled-worlds." + s + "." + setting, "ENDER_CHEST"));
                }
                catch(Exception e)
                {
                    plugin.getLogger().log(Level.SEVERE, "Could not get Material from " + s + "." + setting + ". It is either missing or incorrect");
                }

            }

        }

        return Material.ENDER_CHEST;

    }

    public static Particle getCompatParticle(String setting) {


        try {
            return Particle.valueOf(plugin.getConfig().getString("settings.effects-particles." + setting, "COMPOSTER"));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not get Particle " + setting + " it is either missing or not compatible with your version of minecraft. Please make sure your particle name exists on the version you're playing!");
        }

        return Particle.CRIT;

    }

    public static Particle getWorldParticleUnknown(String world, String setting)
    {

        for(String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            if(getWorldString(s, "world-name").equals(world))
            {

                try
                {
                    return Particle.valueOf(plugin.getConfig().getString("settings.enabled-worlds." + s + "." + setting, "COMPOSTER"));
                }
                catch(Exception e)
                {
                    plugin.getLogger().log(Level.SEVERE, "Could not get Particle from " + s + "." + setting + ". It is either missing or incorrect");
                }

            }

        }

        return Particle.COMPOSTER;

    }

    public static boolean getWorldBooleanUnknown(String world, String setting)
    {
        for(String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            if(getWorldString(s, "world-name").equals(world))
            {
                return plugin.getConfig().getBoolean("settings.enabled-worlds." + s + "." + setting, false);
            }

        }

        return false;

    }

    public static List<String> getWorldStringListUnknown(String world, String setting)
    {
        for(String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            if(getWorldString(s, "world-name").equals(world))
            {
                return plugin.getConfig().getStringList("settings.enabled-worlds." + s + "." + setting);
            }

        }

        return new ArrayList<>();

    }


    public static int getWorldIntUnknown(String world, String setting)
    {
        for(String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            if(getWorldString(s, "world-name").equals(world))
            {
                return plugin.getConfig().getInt("settings.enabled-worlds." + s + "." + setting, 0);
            }

        }

        return 0;

    }

    public static String getWorldString(String world, String setting)
    {
        return plugin.getConfig().getString("settings.enabled-worlds." + world + "." + setting, "Null value");
    }

    public static int getWorldInt(String world, String setting)
    {
        return plugin.getConfig().getInt("settings.enabled-worlds." + world + "." + setting, -1);
    }

    public static int getCooldown()
    {
        return plugin.getConfig().getInt("settings.winner-cooldown", 1440);
    }

}
