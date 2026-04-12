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

package com.Moshu.TreasureHunt.Components;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RoundManager {

    private static final Map<String, RoundData> roundDataMap = new HashMap<>();
    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public static void load() {
        roundDataMap.clear();

        if (plugin == null) return;

        File folder = new File(plugin.getDataFolder(), "rounds");
        if (!folder.exists()) {
            folder.mkdir();
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().info("No round files found in /rounds directory.");
            return;
        }

        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection roundsSection = config.getConfigurationSection("rounds");

            if (roundsSection == null) {
                plugin.getLogger().warning("File '" + file.getName() + "' in /rounds/ does not contain a 'rounds' key!");
                continue;
            }

            for (String roundId : roundsSection.getKeys(false)) {
                ConfigurationSection roundSection = roundsSection.getConfigurationSection(roundId);
                if (roundSection != null) {
                    RoundData data = new RoundData(roundId, roundSection);
                    roundDataMap.put(roundId, data);
                    plugin.getLogger().info("Successfully loaded round: '" + roundId + "' from file: " + file.getName());
                }
            }
        }

        plugin.getLogger().info("Loaded " + roundDataMap.size() + " rounds from " + files.length + " files.");
    }

    public static RoundData getRound(String id) {
        return roundDataMap.get(id);
    }
}

