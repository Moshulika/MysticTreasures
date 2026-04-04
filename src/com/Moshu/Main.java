package com.Moshu;

import com.Moshu.Misc.*;
import com.Moshu.Misc.Hooks.DiscordWebhook;
import com.Moshu.Misc.Hooks.Metrics;
import com.Moshu.Misc.Hooks.PacketEventsUtils;
import com.Moshu.Misc.Hooks.Placeholders;
import com.Moshu.Misc.Storage.FileUpdater;
import com.Moshu.Misc.Storage.Messages;
import com.Moshu.Misc.Storage.Settings;
import com.Moshu.TreasureHunt.Components.Rewards.RewardObfuscator;
import com.Moshu.TreasureHunt.Components.TreasureData;
import com.Moshu.TreasureHunt.Core.Interaction.External.TreasureItemsAdderInteractionEvent;
import com.Moshu.TreasureHunt.Core.Interaction.External.TreasureNexoInteractionEvent;
import com.Moshu.TreasureHunt.Core.Interaction.External.TreasureOraxenInteractionEvent;
import com.Moshu.TreasureHunt.Core.Interaction.TreasureCommands;
import com.Moshu.TreasureHunt.Core.Interaction.TreasureEvents;
import com.Moshu.TreasureHunt.Core.Interaction.TreasureMenu;
import com.Moshu.TreasureHunt.Core.Treasure;
import com.Moshu.TreasureHunt.Handlers.ActionBar;
import com.Moshu.TreasureHunt.Handlers.TreasureEffects;
import com.Moshu.TreasureHunt.TreasureTask;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Main plugin class for MysticTreasures.
 * This is the primary entry point for the treasure hunting plugin that manages
 * scheduled treasure hunts, player interactions, and all plugin functionality.
 * 
 * The plugin provides a comprehensive treasure hunting system with:
 * - Scheduled treasure spawns
 * - Player interaction and rewards
 * - Integration with various Bukkit plugins
 * - Configuration management
 * - Cooldown systems
 * 
 * @author Moshu
 * @version 1.0
 */
public class Main extends JavaPlugin {



    public static Main plugin;
    public static boolean isLoaded = false;

    Updater updater = new Updater(this);
    Settings settings = new Settings(this);
    Messages messagesClass = new Messages(this);
    Utils utils = new Utils(this);
    Cooldown cooldown = new Cooldown(this);
    TreasureCommands treasureCommands = new TreasureCommands(this);
    FileUpdater fileUpdater = new FileUpdater(this);

    /**
     * Called when the plugin is enabled.
     * Initializes all plugin components, registers events and commands,
     * sets up integrations with other plugins, and starts the treasure task.
     */
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

        PluginDescriptionFile pdf = getDescription();
        s.sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fEnabling plugin version " + pdf.getVersion() + ".."));

        HuntTabCompleter tabc = new HuntTabCompleter();

        getCommand("hunt").setExecutor(treasureCommands);
        getCommand("hunt").setTabCompleter(tabc);

        Bukkit.getServer().getPluginManager().registerEvents(TreasureEvents.getInstance(), this);
        Bukkit.getServer().getPluginManager().registerEvents(updater, this);
        Bukkit.getServer().getPluginManager().registerEvents(new TreasureMenu(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new RewardObfuscator(), this);

        registerExternalEvents();

        s.sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fHooking into WorldGuard"));
        getWorldGuard();

        checkCustomItemsDependencies();

        createDataFiles();
        FileUpdater.update();

        if(Utils.isEnabled("PlaceholderAPI")) {
            s.sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fHooking into PAPI"));
            new Placeholders().register();
        }

        Locations.init();

        delayedHooks();

        metrics();
        Utils.readClassName();

        if(Utils.isEnabled("packetevents")) {
            s.sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fHooking into PacketEvents"));
            PacketEventsUtils.initPacketEvents();
            s.sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fPacketEvents ready: " + PacketEventsUtils.isReady()));
        }
    }

    /**
     * Called when the plugin is loaded.
     * Performs early initialization tasks that need to happen before enable.
     */
    @Override
    public void onLoad() {

        if(Utils.isLoaded("packetevents")) {
            Bukkit.getConsoleSender().sendMessage("Loading PacketEvents..");
            PacketEventsUtils.loadPacketEvents();
        }

    }

    /**
     * Called when the plugin is disabled.
     * Performs cleanup operations and saves any pending data.
     */
    @Override
    public void onDisable()
    {
        Treasure.removeAll();

        if(Utils.isEnabled("packetevents"))
        {
            PacketEventsUtils.disablePacketEvents();
        }

    }

    /**
     * Register external events to avoid missing dependency errors
     */
    private void registerExternalEvents()
    {

        if(Utils.isEnabled("Oraxen"))
        {
            Bukkit.getServer().getPluginManager().registerEvents(new TreasureOraxenInteractionEvent(), this);
        }

        if(Utils.isEnabled("Nexo"))
        {
            Bukkit.getServer().getPluginManager().registerEvents(new TreasureNexoInteractionEvent(), this);
        }

        if(Utils.isEnabled("ItemsAdder"))
        {
            Bukkit.getServer().getPluginManager().registerEvents(new TreasureItemsAdderInteractionEvent(), this);
        }

    }

    /**
     * Sets up delayed hooks and initialization tasks that need to run after the server has fully started.
     * This includes loading treasure data, starting tasks, and initializing various components.
     */
    private void delayedHooks()
    {

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () ->
        {

            CommandSender s = Bukkit.getConsoleSender();

            s.sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fStarting post-load setup"));

            getLogger().log(Level.INFO, "Loading treasure data..");
            TreasureData.load();
            Settings.recacheSettings();
            RewardObfuscator.load();
            TreasureEffects.check();
            TreasureTask.task();
            TreasureTask.schedulerTask();
            ActionBar.start();

            DiscordWebhook webhook = DiscordWebhook.getInstance();
            webhook.init();

            Treasure.cleanup();

            updater.check();

        }, 1);


    }

    /**
     * Checks for custom item plugin dependencies and logs their status.
     * Currently checks for ItemsAdder, Oraxen, and Nexo plugins.
     */
    private void checkCustomItemsDependencies()
    {
        boolean itemsAdder = Utils.isEnabled("ItemsAdder");
        boolean oraxen = Utils.isEnabled("Oraxen");
        boolean nexo = Utils.isEnabled("Nexo");

        getLogger().log(Level.INFO, "Checking items dependencies");
        getLogger().log(Level.INFO, "ItemsAdder: " + itemsAdder + ", Oraxen: " + oraxen + ", Nexo: " + nexo);

    }


    private File configf, messagesf, cooldowndsf, discord_webhookf;

    private FileConfiguration config, messages, cooldowns;

    /**
     * Gets the cooldowns configuration file.
     * 
     * @return The FileConfiguration object for cooldowns
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public FileConfiguration getCooldownsFile()
    {
        return cooldowns;
    }

    /**
     * Gets the messages configuration file.
     * 
     * @return The FileConfiguration object for messages
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public FileConfiguration getMessages()
    {
        return messages;
    }

    /**
     * Gets the main configuration file.
     * 
     * @return The FileConfiguration object for the main config
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
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
            Settings.recacheSettings();
            RewardObfuscator.load();
            DiscordWebhook.getInstance().init();

        }
        catch (IOException | InvalidConfigurationException e)
        {
            e.printStackTrace();
        }

    }

    /** @hidden */
    public static void consoleMessage(String s)
    {
        Bukkit.getConsoleSender().sendMessage(Utils.format(s));
    }

    /**
     * Initializes bStats metrics collection if enabled in configuration.
     * Sends plugin usage statistics to bStats for analytics.
     */
    private void metrics()
    {

        if(getConfigFile().getBoolean("settings.bstats", true))
        {
            int pluginId = 23859;
            new Metrics(this, pluginId);
            Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fbStats is enabled"));
        }

    }

    /**
     * Gets world guard instance
     * @return WorldGuard instance or null
     */
    private WorldGuardPlugin getWorldGuard()
    {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        if ((plugin == null) || (!(plugin instanceof WorldGuardPlugin)))
        {
            consoleMessage("&c&lDependency Check: &fYou don't have WorldGuard installed.");
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }

    /**
     * Creates and loads all necessary configuration files.
     * Creates config.yml, messages.yml, and cooldowns.yml if they don't exist.
     */
    private void createDataFiles() {

        configf = new File(getDataFolder(), "config.yml");
        messagesf = new File(getDataFolder(), "messages.yml");
        cooldowndsf = new File(getDataFolder(), "cooldowns.yml");
        discord_webhookf = new File(getDataFolder(), "discord-webhook.json");

        if (!configf.exists())
        {
            saveDefaultConfig();
            Bukkit.getConsoleSender().sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fConfig.yml &fnot found, creating."));
        }

        if (!messagesf.exists())
        {
            if (messagesf.getParentFile() != null && !messagesf.getParentFile().exists()) {
                if (!messagesf.getParentFile().mkdirs()) {
                    getLogger().warning("Could not create directories for messages.yml");
                }
            }
            saveResource("messages.yml", false);
            Bukkit.getConsoleSender().sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fMessages.yml &fnot found, creating."));
        }

        if(!cooldowndsf.exists())
        {
            if (cooldowndsf.getParentFile() != null && !cooldowndsf.getParentFile().exists()) {
                if (!cooldowndsf.getParentFile().mkdirs()) {
                    getLogger().warning("Could not create directories for cooldowns.yml");
                }
            }
            saveResource("cooldowns.yml", false);
            Bukkit.getConsoleSender().sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fCooldowns.yml &fnot found, creating."));
        }

        if(!discord_webhookf.exists())
        {
            if (discord_webhookf.getParentFile() != null && !discord_webhookf.getParentFile().exists()) {
                if (!discord_webhookf.getParentFile().mkdirs()) {
                    getLogger().warning("Could not create directories for discord-webhook.json");
                }
            }
            saveResource("discord-webhook.json", false);
            Bukkit.getConsoleSender().sendMessage(Utils.format( "&5&lMystic&d&lTreasures: &fdiscord-webhook.json &fnot found, creating."));
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