package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.objects.TreasureData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

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
                return;
            }

            BukkitRunnable run = new BukkitRunnable()
            {

                @Override
                public void run() {

                    if(Bukkit.getOnlinePlayers().size() < Settings.getInt("min-players-online")) return;

                    if(Utils.chance() < d.getChanceForTreasure())
                    {
                        if(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastHunt) < d.getCooldown()) return;

                        if(Hunt.isActive(identifier)) return;
                        if(Hunt.huntStarting(identifier)) return;

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
