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

    //TODO: Replace the getInt, etc. methods with cached setting values
    public Settings(Main plugin)
    {
        Settings.plugin = plugin;
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

    public static Particle getCompatParticle(String setting) {


        try {
            return Particle.valueOf(plugin.getConfig().getString("settings.effects-particles." + setting, "COMPOSTER"));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not get Particle " + setting + " it is either missing or not compatible with your version of minecraft. Please make sure your particle name exists on the version you're playing!");
        }

        return Particle.CRIT;

    }

    public static int getInt(String path)
    {
        try {
            return plugin.getConfig().getInt("settings." + path);

        }
        catch (Exception e)
        {
            plugin.getLogger().log(Level.SEVERE, "Could not get value: " + path);
        }

        return 0;

    }

    public static boolean getBoolean(String path)
    {
        try {
            return plugin.getConfig().getBoolean("settings." + path);
        }
        catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not get value: " + path);
        }

        return false;

    }

    public static String getString(String path)
    {
        try
        {
            return plugin.getConfig().getString("settings." + path);
        }
        catch (Exception e)
        {
            plugin.getLogger().log(Level.SEVERE, "Could not get value: " + path);
        }

        return "Null String";

    }

    public static int getCooldown()
    {
        try
        {
            return plugin.getConfig().getInt("settings.winner-cooldown");
        }
        catch (Exception e)
        {
            plugin.getLogger().log(Level.SEVERE, "Could not get value: winner-cooldown");
        }

        return 1440;

    }

}
