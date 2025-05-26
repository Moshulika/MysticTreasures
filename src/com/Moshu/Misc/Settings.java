package com.Moshu.Misc;


import com.Moshu.Main;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
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

    private static int SLOW_FALLING_DURATION = 1200;
    private static int SLOW_FALLING_LEVEL = 2;
    private static List<String> BLACKLISTED_COMMANDS = new ArrayList<>();

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

    public static void recacheSettings()
    {
        FileConfiguration config = plugin.getConfig();
        SLOW_FALLING_DURATION = config.getInt("settings.fall-protection.duration", 1200);
        SLOW_FALLING_LEVEL = config.getInt("settings.fall-protection.level", 2);
        BLACKLISTED_COMMANDS = config.getStringList("settings.blacklisted-commands");
    }

    public static int getSlowFallingDuration() {
        return SLOW_FALLING_DURATION;
    }

    public static int getSlowFallingLevel() {
        return SLOW_FALLING_LEVEL - 1;
    }

    public static List<String> getBlacklistedCommands() {
        return BLACKLISTED_COMMANDS;
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

    public static List<String> getStringList(String path)
    {
        try
        {
            return plugin.getConfig().getStringList("settings." + path);
        }
        catch (Exception e)
        {
            plugin.getLogger().log(Level.SEVERE, "Could not get value: " + path);
        }

        return new ArrayList<>();

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
