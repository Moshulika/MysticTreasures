package com.Moshu.TreasureHunt.Handlers;

import com.Moshu.Misc.Locations;
import com.Moshu.Misc.Storage.Settings;
import com.Moshu.TreasureHunt.Core.Hunt;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class TreasureEffects {

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private static final ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();

    /*
    Cumva sa fie pentru toti jucatorii, daca sunt in raza unui treasure activ
     */
    public static void check() {

        BukkitRunnable run = new BukkitRunnable() {

            Hunt h;
            int distance;

            @Override
            public void run() {


                if (Bukkit.getOnlinePlayers().isEmpty()) return;
                if (Hunt.getActiveTreasures().isEmpty()) return;

                for (Player p : Bukkit.getOnlinePlayers()) {

                    h = Hunt.getNearestHunt(p.getLocation());
                    if (h == null) continue;

                    distance = Settings.getInt("potion-effect-radius");

                    if (Locations.distanceSquaredTo(p.getLocation(), h.getLocation()) <= (double) distance * distance) {

                        Bukkit.getScheduler().runTask(plugin, () ->
                        {

                            for (PotionEffect effect : effects) {
                                p.addPotionEffect(effect);
                            }

                        });

                    }

                }

            }
        };

        run.runTaskTimerAsynchronously(plugin, 0, 40);


    }

}
