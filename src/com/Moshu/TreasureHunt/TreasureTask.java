package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Storage.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Components.TreasureData;
import com.Moshu.TreasureHunt.Core.Hunt;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Manages the scheduling and execution of treasure hunt tasks.
 * This class handles both scheduled treasure spawns and random treasure generation
 * based on configured intervals and conditions.
 * 
 * The class provides two main task types:
 * - Scheduler tasks: Execute based on predefined schedules
 * - Random tasks: Execute based on chance and cooldown systems
 * 
 * @author Moshu
 * @version 1.0
 */
public class TreasureTask {

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");
    private static long lastHunt;

    /**
     * Updates the timestamp of the last treasure hunt.
     * Used to track cooldown periods between hunts.
     */
    public static void updateLastHunt()
    {
        lastHunt = System.currentTimeMillis();
    }

    /**
     * Starts the scheduler task that checks for scheduled treasure spawns.
     * Runs every 15 seconds (300 ticks) and processes all configured treasure schedulers.
     * Prevents spawning treasures too frequently by maintaining timestamps.
     */
    public static void schedulerTask() {

        BukkitRunnable task = new BukkitRunnable() {

            final HashMap<String, Long> timestamps = new HashMap<String, Long>();

            @Override
            public void run() {

                for (TreasureScheduler s : TreasureData.getAllTreasureSchedulers()) {

                    if (s.shouldSpawn()) {

                        if(timestamps.containsKey(s.getId()))
                        {
                            if(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - timestamps.get(s.getId())) < 5)
                            {
                                plugin.getLogger().log(Level.WARNING, "Skipping scheduled treasure for being to close to previous treasure!");
                                continue;
                            }
                        }

                        if (s.spawn())
                        {
                            plugin.getLogger().log(Level.INFO, "Spawning scheduled treasure: " + s.getId());
                            timestamps.put(s.getId(), System.currentTimeMillis());
                            break;
                        }
                        else
                            plugin.getLogger().log(Level.SEVERE, "Something went wrong while trying to spawn scheduled treasure treasure!");

                    }

                }

            }


        };

        task.runTaskTimer(plugin, 0, 300);

    }

    /**
     * Checks if any scheduled treasure should spawn to prevent concurrent spawns.
     * 
     * @return True if a scheduled treasure is about to spawn, false otherwise
     */
    private static boolean preventConcurrentSpawn()
    {

        for(TreasureScheduler s : TreasureData.getAllTreasureSchedulers())
        {
            if(s.shouldSpawn()) return true;
        }

        return false;
    }

    /**
     * Starts the random treasure generation task.
     * Creates tasks for each treasure data configuration with random delays
     * and processes them based on chance and cooldown systems.
     */
    public static void task()
    {

        int delay;

        for(TreasureData d : TreasureData.getTreasureData())
        {

            delay = ThreadLocalRandom.current().nextInt(200, 1200);
            final String identifier = d.getIdentifier();

            World w = Bukkit.getWorld(d.getWorldName());

            if(w == null)
            {
                plugin.getLogger().log(Level.SEVERE, "Invalid world name inside " + identifier + "'s treasure configuration. Make sure the world declared in `world-name` exists on your server!");
                continue;
            }

            BukkitRunnable run = new BukkitRunnable()
            {

                @Override
                public void run() {

                    if(preventConcurrentSpawn()) return;
                    if(Bukkit.getOnlinePlayers().size() < Settings.getInt("min-players-online")) return;

                    if(Utils.chance() < d.getChanceForTreasure())
                    {

                        if(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastHunt) < d.getCooldown()) return;
                        if(Hunt.isHuntActive(identifier)) return;

                        Hunt h = new Hunt(identifier, d.getDuration());

                        BukkitRunnable run = new BukkitRunnable()
                        {

                            @Override
                            public void run() {

                                if(h.getLocation() == null) return;

                                h.start();
                                this.cancel();

                            }
                        };

                        run.runTaskTimerAsynchronously(plugin, 0, 1);


                    }

                }
            };

            run.runTaskTimer(plugin, delay, (long) d.getInterval() * 1200);

        }

    }

}
