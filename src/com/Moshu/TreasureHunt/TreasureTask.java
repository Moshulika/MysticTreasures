package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.objects.TreasureData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class TreasureTask {

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");
    private static long lastHunt;

    public static void updateLastHunt()
    {
        lastHunt = System.currentTimeMillis();
    }

    public static void schedulerTask() {

        BukkitRunnable task = new BukkitRunnable() {

            HashMap<String, Long> timestamps = new HashMap<String, Long>();

            @Override
            public void run() {

                for (TreasureScheduler s : TreasureData.getAllTreasureSchedulers()) {

                    if (s.shouldSpawn()) {

                        if(timestamps.containsKey(s.getId()))
                        {
                            if(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - timestamps.get(s.getId())) < 5) continue;
                        }

                        if (s.spawn())
                        {
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

    private static boolean preventConcurrentSpawn()
    {

        for(TreasureScheduler s : TreasureData.getAllTreasureSchedulers())
        {
            if(s.shouldSpawn()) return true;
        }

        return false;
    }

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
