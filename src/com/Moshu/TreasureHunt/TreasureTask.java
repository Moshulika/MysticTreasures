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

    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");
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

            BukkitRunnable run = new BukkitRunnable()
            {

                World w;

                @Override
                public void run() {

                    if(Bukkit.getOnlinePlayers().size() < Settings.getWorldInt(s, "min-players-online")) return;

                    int chance = Utils.chance();

                    if(chance < Settings.getWorldInt(s, "chance-for-treasure"))
                    {

                        if(Hunt.isActive()) return;
                        if(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastHunt) < Settings.getWorldInt(s, "cooldown")) return;

                        w = Bukkit.getWorld(Settings.getWorldString(s, "world-name"));

                        if(w == null)
                        {
                            plugin.getLogger().log(Level.SEVERE, "Invalid world name: " + Settings.getWorldString(s, "world-name"));
                            return;
                        }

                        Hunt h = new Hunt(w, Settings.getWorldInt(s, "duration"));

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

            run.runTaskTimer(plugin, delay, (long) Settings.getWorldInt(s, "chance-for-treasure") * 1200);

        }

    }

}
