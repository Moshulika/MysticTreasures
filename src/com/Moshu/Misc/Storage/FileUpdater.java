/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.Moshu.Misc.Storage;

import com.Moshu.Main;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;

public class FileUpdater {

    private static Main plugin;

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public FileUpdater(Main plugin) {
        FileUpdater.plugin = plugin;
    }

    /**
     * Updates the main configuration files by merging them with defaults from the JAR.
     */
    public static void update() {
        FileHandler fileHandler = FileHandler.getInstance();

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        File cooldownsFile = new File(plugin.getDataFolder(), "cooldowns.yml");

        // Protect subjective sections in config.yml
        java.util.List<String> configProtected = java.util.Arrays.asList("settings.obfuscated-reward-item");

        fileHandler.mergeWithDefault(configFile, "config.yml", configProtected);
        fileHandler.mergeWithDefault(messagesFile, "messages.yml");
        fileHandler.mergeWithDefault(cooldownsFile, "cooldowns.yml");

        plugin.reloadFiles();
    }

}

