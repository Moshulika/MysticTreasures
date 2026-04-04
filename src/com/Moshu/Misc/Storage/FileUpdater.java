package com.Moshu.Misc.Storage;

import com.Moshu.Main;
import java.io.File;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class FileUpdater {

    private static Main plugin;

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public FileUpdater(Main plugin)
    {
        FileUpdater.plugin = plugin;
    }

    /**
     * Updates the main configuration files by merging them with defaults from the JAR.
     */
    public static void update()
    {
        FileHandler fileHandler = FileHandler.getInstance();
        
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        File cooldownsFile = new File(plugin.getDataFolder(), "cooldowns.yml");
        
        fileHandler.mergeWithDefault(configFile, "config.yml");
        fileHandler.mergeWithDefault(messagesFile, "messages.yml");
        fileHandler.mergeWithDefault(cooldownsFile, "cooldowns.yml");
        
        plugin.reloadFiles();
    }

}
