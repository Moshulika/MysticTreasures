package com.Moshu.Misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.Moshu.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Updater implements Listener {

    private static Main plugin;

    public Updater(Main plugin)
    {
        this.plugin = plugin;
    }


    private String url = "https://api.spigotmc.org/legacy/update.php?resource=";
    private String id = "118535";

    private boolean isAvailable;

    public Updater()
    {

    }

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

        Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fChecking for updates.."));

        try {
            String localVersion = plugin.getDescription().getVersion();
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url + id).openConnection();
            connection.setRequestMethod("GET");
            String raw = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();

            String remoteVersion;
            if(raw.contains("-")) {
                remoteVersion = raw.split("-")[0].trim();
            } else {
                remoteVersion = raw;
            }

            if(!localVersion.equalsIgnoreCase(remoteVersion))
            {
                Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fAn update is ready for you:"));
                Bukkit.getConsoleSender().sendMessage(Utils.format("&fhttps://www.spigotmc.org/resources/mystic-treasures-animated-feature-packed-and-lightweight.118535/updates"));
                Bukkit.getConsoleSender().sendMessage(Utils.format("&5Your version: &f" + localVersion + "&5, remote version: &f" + remoteVersion));

                return true;
            }
            else
            {
                Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fYour version is up to date"));
                return false;
            }

        } catch (IOException e)
        {
            Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fThere was a problem checking the updates."));
            return false;
        }
    }




}
