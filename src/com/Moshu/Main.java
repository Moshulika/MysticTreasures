package com.Moshu;
import com.Moshu.Misc.*;
import com.Moshu.TreasureHunt.*;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main extends JavaPlugin {



    public static Main plugin;
    public static boolean isLoaded = false;

    Updater updater = new Updater(this);
    FilesUpdater filesUpdater = new FilesUpdater(this);
    Settings settings = new Settings(this);
    Messages messagesClass = new Messages(this);
    Utils utils = new Utils(this);
    Cooldown cooldown = new Cooldown(this);
    TreasureCommands treasureCommands = new TreasureCommands(this);

    @Override
    public void onEnable()
    {

        CommandSender s = Bukkit.getConsoleSender();
        s.sendMessage(ChatColor.DARK_PURPLE + "\n" +
                "  __  __           _   _        _______                                     \n" +
                " |  \\/  |         | | (_)      |__   __|                                    \n" +
                " | \\  / |_   _ ___| |_ _  ___     | |_ __ ___  __ _ ___ _   _ _ __ ___  ___ \n" +
                " | |\\/| | | | / __| __| |/ __|    | | '__/ _ \\/ _` / __| | | | '__/ _ \\/ __|\n" +
                " | |  | | |_| \\__ \\ |_| | (__     | | | |  __/ (_| \\__ \\ |_| | | |  __/\\__ \\\n" +
                " |_|  |_|\\__, |___/\\__|_|\\___|    |_|_|  \\___|\\__,_|___/\\__,_|_|  \\___||___/\n" +
                "          __/ |                                                             \n" +
                "         |___/                                                              \n");

        s.sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fEnabling plugin.."));

        TabCompleter tabc = new TabCompleter();

        getCommand("hunt").setExecutor(treasureCommands);
        getCommand("hunt").setTabCompleter(tabc);

        Bukkit.getServer().getPluginManager().registerEvents(new TreasureEvents(), this);
        Bukkit.getServer().getPluginManager().registerEvents(updater, this);

        s.sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fHooking into WorldGuard"));
        getWorldGuard();

        createDataFiles();
        FilesUpdater.update();

        if(Utils.isEnabled("PlaceholderAPI")) {
            new Placeholders().register();
        }

        delayedHooks();

        TreasureEffects.check();

        Hunt.initialize();
        TreasureTask.task();
        ActionBar.start();

        metrics();

    }


    @Override
    public void onDisable()
    {

        Treasure.removeAll();

    }

    public void delayedHooks()
    {

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () ->
        {

            CommandSender s = Bukkit.getConsoleSender();

            s.sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fStarting post-load setup"));
            updater.check();

        }, 1);


    }


    private File configf, messagesf, cooldowndsf;

    private FileConfiguration config, messages, cooldowns;

    public FileConfiguration getCooldownsFile()
    {
        return cooldowns;
    }

    public FileConfiguration getMessages()
    {
        return messages;
    }

    public FileConfiguration getConfigFile()
    {
        return config;
    }

    /**
     * Reload non-essential files
     */
    public void reloadFiles()
    {

        try
        {

            config.load(configf);
            messages.load(messagesf);

        }
        catch (IOException | InvalidConfigurationException e)
        {
            e.printStackTrace();
        }

    }

    /** @hidden */
    public static void consoleMessage(String s)
    {
        Bukkit.getConsoleSender().sendMessage(Utils.format( s));
    }

    public void metrics()
    {

        if(getConfigFile().getBoolean("settings.bstats", true))
        {
            int pluginId = 23859;
            Metrics metrics = new Metrics(this, pluginId);
            Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fbStats is enabled"));
        }

    }

    /**
     * Gets world guard instance
     * @return WorldGuard instance or null
     */
    public WorldGuardPlugin getWorldGuard()
    {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        if ((plugin == null) || (!(plugin instanceof WorldGuardPlugin)))
        {
            consoleMessage("&c&lDependency Check: &fYou don't have WorldGuard installed.");
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }

    public void createDataFiles() {

        configf = new File(getDataFolder(), "config.yml");
        messagesf = new File(getDataFolder(), "messages.yml");
        cooldowndsf = new File(getDataFolder(), "cooldowns.yml");

        if (!configf.exists())
        {
            saveDefaultConfig();
            Bukkit.getConsoleSender().sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fConfig.yml &fnot found, creating."));
        }

        if (!messagesf.exists())
        {
            messagesf.getParentFile().mkdirs();
            saveResource("messages.yml", false);
            Bukkit.getConsoleSender().sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fMessages.yml &fnot found, creating."));
        }

        if(!cooldowndsf.exists())
        {
            cooldowndsf.getParentFile().mkdirs();
            saveResource("cooldowns.yml", false);
            Bukkit.getConsoleSender().sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fCooldowns.yml &fnot found, creating."));
        }

        config = new YamlConfiguration();
        messages = new YamlConfiguration();
        cooldowns = new YamlConfiguration();

        try {

            config.load(configf);
            messages.load(messagesf);
            cooldowns.load(cooldowndsf);

        }
        catch (IOException | InvalidConfigurationException e)
        {
            e.printStackTrace();
        }

    }

}