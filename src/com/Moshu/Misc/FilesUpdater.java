package com.Moshu.Misc;

import com.Moshu.Main;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;

public class FilesUpdater {

    private static Main plugin;

    public FilesUpdater(Main plugin)
    {
        this.plugin = plugin;
    }

    public static void update()
    {

        for(String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s + ".distance-from-player-to-spawn-mobs") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s + ".distance-from-player-to-spawn-mobs", 0);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".reward-all-players-who-participated") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s +".reward-all-players-who-participated", false);
            }

        }

        if(plugin.getMessages().get("messages.config-reload") == null)
        {
            plugin.getMessages().set("messages.config-reload", "&6&lTreasure&e&lHunt &fConfig reloaded!");
        }

        if(plugin.getMessages().get("messages.participating") == null)
        {
            plugin.getMessages().set("messages.participating", "&6&lTreasure&e&lHunt &fYou are now participating in this TreasureHunt!");
        }

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
