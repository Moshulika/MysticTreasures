package com.Moshu;
import com.Moshu.Misc.Cooldown;
import com.Moshu.Misc.TabCompleter;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Hunt;
import com.Moshu.TreasureHunt.TreasureCommands;
import com.Moshu.TreasureHunt.TreasureEvents;
import com.Moshu.TreasureHunt.TreasureTask;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


/**
 *
 * TODO: Messages
 *
 */

public class Main extends JavaPlugin {



    public static Main plugin;
    public static boolean isLoaded = false;

    Utils utils = new Utils(this);
    Cooldown cooldown = new Cooldown(this);

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

        s.sendMessage(Utils.format( "&5Treasures: &fEnabling plugin.."));

        TabCompleter tabc = new TabCompleter();

        getCommand("hunt").setExecutor(new TreasureCommands());
        getCommand("hunt").setTabCompleter(tabc);

        Bukkit.getServer().getPluginManager().registerEvents(new TreasureEvents(), this);

        s.sendMessage(Utils.format( "&5Treasures: &fHooking into Vault"));
        s.sendMessage(Utils.format( "&5Treasures: &fHooking into WorldGuard"));
        getWorldGuard();

        Hunt.initialize();
        TreasureTask.task();

    }


    @Override
    public void onDisable()
    {

    }

    public void delayedHooks()
    {

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () ->
        {

            CommandSender s = Bukkit.getConsoleSender();

            s.sendMessage(Utils.format( "&5Treasures: &fStarting post-load setup"));
            s.sendMessage(" ");

            createDataFiles();

        }, 1);


    }


    private File configf, dataf, messagesf, cooldowndsf;

    private FileConfiguration config, data, messages, cooldowns;

    public FileConfiguration getData()
    {
        return data;
    }
    public FileConfiguration getCooldownsFile()
    {
        return cooldowns;
    }

    public FileConfiguration getMessages()
    {
        return messages;
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

    /**
     * Gets world guard instance
     * @return WorldGuard instance or null
     */
    public WorldGuardPlugin getWorldGuard()
    {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        if ((plugin == null) || (!(plugin instanceof WorldGuardPlugin)))
        {
            consoleMessage("&c&lDependency Check: &fYou don't have WorldGuard installed. You'd better install it.");
            consoleMessage("&c&lDependency Check: &fPlugin is shutting down..");
            getServer().getPluginManager().disablePlugin(this);
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }

    public void createDataFiles() {

        configf = new File(getDataFolder(), "config.yml");
        dataf = new File(getDataFolder(), "data.yml");
        messagesf = new File(getDataFolder(), "messages.yml");
        cooldowndsf = new File(getDataFolder(), "cooldowns.yml");

        if (!configf.exists())
        {
            saveDefaultConfig();
            Bukkit.getConsoleSender().sendMessage(Utils.format( "&5Treasures: &fConfig.yml &fnot found, creating."));
        }

        if (!dataf.exists())
        {
            dataf.getParentFile().mkdirs();
            saveResource("data.yml", false);
            Bukkit.getConsoleSender().sendMessage(Utils.format( "&5Treasures: &fData.yml &fnot found, creating."));
        }

        if (!messagesf.exists())
        {
            messagesf.getParentFile().mkdirs();
            saveResource("messages.yml", false);
            Bukkit.getConsoleSender().sendMessage(Utils.format( "&5Treasures: &fMessages.yml &fnot found, creating."));
        }

        if(!cooldowndsf.exists())
        {
            cooldowndsf.getParentFile().mkdirs();
            saveResource("cooldowns.yml", false);
            Bukkit.getConsoleSender().sendMessage(Utils.format( "&5Treasures: &fCooldowns.yml &fnot found, creating."));
        }

        config = new YamlConfiguration();
        data = new YamlConfiguration();
        messages = new YamlConfiguration();
        cooldowns = new YamlConfiguration();

        try {

            config.load(configf);
            data.load(dataf);
            messages.load(messagesf);
            cooldowns.load(cooldowndsf);

        }
        catch (IOException | InvalidConfigurationException e)
        {
            e.printStackTrace();
        }

    }

}