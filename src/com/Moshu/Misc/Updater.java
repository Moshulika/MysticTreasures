/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.Moshu.Misc;

import com.Moshu.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Updater implements Listener {

    private final Main plugin;

    public Updater(Main plugin) {
        this.plugin = plugin;
    }


    private static final String URL_BASE = "https://api.spigotmc.org/legacy/update.php?resource=";
    private static final String RESOURCE_ID = "118535";

    private boolean isAvailable;

    public boolean isAvailable() {
        return isAvailable;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if (event.getPlayer().hasPermission("mystictreasures.admin")) {

            if (isAvailable) {
                event.getPlayer().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fAn update is ready for you:"));
                event.getPlayer().sendMessage(Utils.format("&fhttps://www.spigotmc.org/resources/mystic-treasures-animated-feature-packed-and-lightweight.118535/updates"));
                Utils.sendSound(event.getPlayer());
            }

        }
    }

    public void check() {
        isAvailable = checkUpdate();
    }

    private boolean checkUpdate() {

        if (!plugin.getConfigFile().getBoolean("settings.updater", true)) {
            return false;
        }

        Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fChecking for updates.."));

        try {
            String localVersion = plugin.getDescription().getVersion();
            HttpsURLConnection connection = (HttpsURLConnection) new URL(URL_BASE + RESOURCE_ID).openConnection();
            connection.setRequestMethod("GET");

            String raw;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                raw = reader.readLine();
            }

            if (raw == null) {
                return false;
            }

            String remoteVersion;
            if (raw.contains("-")) {
                remoteVersion = raw.split("-")[0].trim();
            } else {
                remoteVersion = raw;
            }

            if (!localVersion.equalsIgnoreCase(remoteVersion)) {
                Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fAn update is ready for you:"));
                Bukkit.getConsoleSender().sendMessage(Utils.format("&fhttps://www.spigotmc.org/resources/mystic-treasures-animated-feature-packed-and-lightweight.118535/updates"));
                Bukkit.getConsoleSender().sendMessage(Utils.format("&5Your version: &f" + localVersion + "&5, remote version: &f" + remoteVersion));

                return true;
            } else {
                Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fYour version is up to date"));
                return false;
            }

        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fThere was a problem checking the updates."));
            return false;
        }
    }


}

