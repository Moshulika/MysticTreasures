/*
 * Copyright 2026 Moshu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This software is also subject to the Commons Clause License Condition v1.0.
 * You may obtain a copy of the Commons Clause at:
 * https://commonsclause.com/
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

