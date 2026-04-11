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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Cooldown {

    public static Map<String, Long> cooldowns = new HashMap<>();

    static Main plugin;

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public Cooldown(Main plugin) {
        Cooldown.plugin = plugin;
    }

    private UUID uuid;
    private String name;
    private int seconds;

    /**
     * Easily set cooldowns for anything
     *
     * @param uuid,    the player's uuid
     * @param name,    the cooldown's name
     * @param seconds, the time in seconds for the cooldown
     */
    public Cooldown(UUID uuid, String name, int seconds) {
        this.uuid = uuid;
        this.name = name;
        this.seconds = seconds;
    }


    /**
     * Sets the cooldown as active
     */
    public void set() {
        setCooldowns(uuid, name, seconds);
    }

    /**
     * Checks if the player has an active cooldown of this type
     *
     * @return true/false
     */
    public boolean has() {
        return hasCooldown(uuid, name);
    }

    /**
     * Removes the cooldown if the time ran up
     */
    public void remove() {

        String code = uuid.toString() + name;

        if (plugin.getCooldownsFile().get(uuid.toString()) == null) {
            return;
        }

        if (plugin.getCooldownsFile().get(uuid.toString() + "." + code) != null) {
            plugin.getCooldownsFile().set(uuid.toString() + "." + code, null);
        }

        try {
            File dataf = new File(plugin.getDataFolder(), "cooldowns.yml");
            plugin.getCooldownsFile().save(dataf);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    public String getName() {
        return name;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMinutes() {
        return (int) TimeUnit.SECONDS.toMinutes(seconds);
    }

    public int getHours() {
        return (int) TimeUnit.SECONDS.toHours(seconds);
    }

    public int getDays() {
        return (int) TimeUnit.SECONDS.toDays(seconds);
    }

    /**
     * Get remaining time
     *
     * @return remaining time in minutes
     */
    public int remainingTime() {
        return getRemainingTimeMinutes(uuid, name);
    }

    /**
     * Sends an error message to the player.
     */
    public void error() {
        getPlayer().sendMessage(Utils.format("&c&lHey! &fMai ai de asteptat &c" + remainingTime() + "&f minute."));
        Utils.sendSound(getPlayer());
    }


    /**
     * This can be used for every case, while
     * the old method works only for Kits.
     *
     * @param uuid    player's uuid
     * @param name    cooldown name
     * @param seconds the cooldown time in seconds
     */

    public static void setCooldowns(UUID uuid, String name, int seconds) {

        String id = uuid.toString();
        long time = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds);
        String code = id + name;


        //cooldowns.put(code, time);

        if (plugin.getCooldownsFile().get(id) == null) {

            plugin.getCooldownsFile().addDefault(id, "");
            plugin.getCooldownsFile().set(id + "." + code, time);

        } else {
            plugin.getCooldownsFile().set(id + "." + code, time);
        }

        try {
            File dataf = new File(plugin.getDataFolder(), "cooldowns.yml");
            plugin.getCooldownsFile().save(dataf);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    /**
     *
     * @param uuid the player's uuid
     * @param name cooldown name
     * @return true if the player has a cooldown active
     * and false if he doesn't
     */

    public static boolean hasCooldown(UUID uuid, String name) {

        String id = uuid.toString();
        String code = id + name;

        if (plugin.getCooldownsFile().get(id) == null) {
            return false;
        }

        if (plugin.getCooldownsFile().get(id + "." + code) == null) {
            return false;
        } else {
            long l = plugin.getCooldownsFile().getLong(id + "." + code);
            return System.currentTimeMillis() < l;
        }

    }


    /**
     *
     * @param uuid player's uuid
     * @param kit  the kit you want to get the cooldown of
     * @return the remaining cooldown in hours
     */

    public static double getRemainingTime(UUID uuid, String kit) {
        String code = uuid.toString() + kit;
        //return (double) TimeUnit.MILLISECONDS.toHours(cooldowns.get(code) - System.currentTimeMillis());
        return (double) TimeUnit.MILLISECONDS.toHours(plugin.getCooldownsFile().getLong(uuid + "." + code) - System.currentTimeMillis());
    }


    /**
     *
     * @param uuid player's uuid
     * @param name the cooldown you want to get the cooldown of
     * @return the remaining cooldown in minutes
     */

    public static int getRemainingTimeMinutes(UUID uuid, String name) {
        String code = uuid.toString() + name;
        //return (int) TimeUnit.MILLISECONDS.toMinutes(cooldowns.get(code) - System.currentTimeMillis());
        return (int) TimeUnit.MILLISECONDS.toMinutes(plugin.getCooldownsFile().getLong(uuid + "." + code) - System.currentTimeMillis());
    }

    public static int getRemainingTimeSeconds(UUID uuid, String name) {
        String code = uuid.toString() + name;
        //return (int) TimeUnit.MILLISECONDS.toMinutes(cooldowns.get(code) - System.currentTimeMillis());
        return (int) TimeUnit.MILLISECONDS.toSeconds(plugin.getCooldownsFile().getLong(uuid + "." + code) - System.currentTimeMillis());
    }

}

