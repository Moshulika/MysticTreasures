/*
 * Copyright 2026 Moshu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This software is also subject to the Commons Clause License Condition v1.0.
 * You may obtain a copy of the Commons Clause at:
 * https://commonsclause.com/
 */

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

