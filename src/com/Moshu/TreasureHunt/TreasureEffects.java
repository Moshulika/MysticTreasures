package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Settings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.logging.Level;

public class TreasureEffects {

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private static final ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();

    private static void loadPotionEffects() {

        String effect;
        int mul;

        for (String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false)) {

            for(String potion : plugin.getConfig().getStringList("settings.enabled-worlds." + s + ".potion-effects")) {

                try {

                    effect = potion.split(":")[0];
                    mul = Integer.parseInt(potion.split(":")[1]);

                    effects.add(new PotionEffect(PotionEffectType.getByName(effect), 60, mul));

                } catch (Exception e) {

                    plugin.getLogger().log(Level.SEVERE, "No such potion effect: " + potion);

                }
            }

        }

    }

    public static void check()
    {

        loadPotionEffects();

        BukkitRunnable run = new BukkitRunnable() {

            World w;
            Hunt h;
            int distance;

            @Override
            public void run() {


                    for (Player p : Bukkit.getOnlinePlayers()) {

                        w = p.getWorld();

                        if(Hunt.isActive(w))
                        {

                            h = Hunt.getHunt(w);
                            distance = Settings.getWorldIntUnknown(w.getName(), "potion-effect-radius");

                            if(p.getLocation().distance(h.getLocation()) <= distance) {

                                Bukkit.getScheduler().runTask(plugin, ()->
                                {

                                    for(PotionEffect effect : effects) {
                                        p.addPotionEffect(effect);
                                    }

                                });

                            }


                    }

                }


            }
        };

        run.runTaskTimerAsynchronously(plugin, 0, 40);


    }

}
