package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
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

        for(String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            delay = ThreadLocalRandom.current().nextInt(200, 1200);
            World w = Bukkit.getWorld(Settings.getWorldString(s, "world-name"));

            if(w == null)
            {
                plugin.getLogger().log(Level.SEVERE, "Invalid world name inside " + s + "'s treasure configuration. Make sure the world declared in `world-name` exists on your server!");
                return;
            }

            BukkitRunnable run = new BukkitRunnable()
            {

                @Override
                public void run() {

                    String worldName = w.getName();

                    if(Bukkit.getOnlinePlayers().size() < Settings.getWorldIntUnknown(worldName, "min-players-online")) return;

                    int chance = Utils.chance();

                    if(chance < Settings.getWorldIntUnknown(worldName, "chance-for-treasure"))
                    {
                        if(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastHunt) < Settings.getWorldIntUnknown(worldName, "cooldown")) return;

                        if(Hunt.isActive(w)) return;
                        if(Hunt.huntStarting(w)) return;

                        Hunt.addHunt(w);
                        Hunt h = new Hunt(w, Settings.getWorldIntUnknown(worldName, "duration"));

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

            run.runTaskTimer(plugin, delay, (long) Settings.getWorldIntUnknown(w.getName(), "interval") * 1200);

        }

    }

}
