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
