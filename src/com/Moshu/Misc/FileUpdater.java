package com.Moshu.Misc;

import com.Moshu.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileUpdater {

    private static Main plugin;

    public FileUpdater(Main plugin)
    {
        this.plugin = plugin;
    }

    public static void update()
    {

        FileConfiguration config = plugin.getConfigFile();
        FileConfiguration messages = plugin.getMessages();
        
        if(config.get("settings.blacklisted-biomes") == null)
        {
            ArrayList<String> biomes = new ArrayList<>();
            biomes.add("DEEP_FROZEN_OCEAN");
            biomes.add("FROZEN_OCEAN");
            biomes.add("COLD_OCEAN");
            biomes.add("DEEP_COLD_OCEAN");
            biomes.add("OCEAN");
            biomes.add("DEEP_OCEAN");
            biomes.add("LUKEWARM_OCEAN");
            biomes.add("DEEP_LUKEWARM_OCEAN");
            biomes.add("RIVER");

            config.set("settings.blacklisted-biomes", biomes);

        }

        if(config.get("settings.blacklisted-commands") == null)
        {
            ArrayList<String> commands = new ArrayList<>();
            commands.add("heal");

            config.set("settings.blacklisted-commands",  commands);
        }

        if(config.get("settings.fall-protection") == null)
        {
            config.addDefault("settings.fall-protection", "");
            config.set("settings.fall-protection.duration", 1200);
            config.set("settings.fall-protection.level", 2);
        }

        if(messages.get("messages.blacklisted-command") == null)
        {
            messages.set("messages.blacklisted-command", "&6&lTreasure&e&lHunt &fThis command cannot be used while near a treasure!");
        }

        if(messages.get("messages.days") == null)
        {
            messages.set("messages.days", "day(s)");
        }

        if(messages.get("messages.hours") == null)
        {
            messages.set("messages.hours", "hour(s)");
        }

        if(messages.get("messages.minutes") == null)
        {
            messages.set("messages.minutes", "minute(s)");
        }

        commit();
        
    }

    private static void commit()
    {
        try
        {

            File configf = new File(plugin.getDataFolder(), "config.yml");
            File messagesf = new File(plugin.getDataFolder(), "messages.yml");
            plugin.getConfigFile().save(configf);
            plugin.getMessages().save(messagesf);

        } catch (IOException e1) {
            e1.printStackTrace();
        }

        plugin.reloadFiles();
    }

}
