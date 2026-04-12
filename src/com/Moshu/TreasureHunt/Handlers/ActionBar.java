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
import com.Moshu.Misc.Storage.Messages;
import com.Moshu.Misc.Storage.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Core.Hunt;
import com.Moshu.TreasureHunt.Core.Treasure;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

//Add bossbar support too
public class ActionBar {

    public static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public static void start() {

        if (!Settings.actionbar()) return;

        BukkitRunnable run = new BukkitRunnable() {

            Hunt h;

            @Override
            public void run() {

                if (Bukkit.getOnlinePlayers().isEmpty()) return;
                if (Hunt.getActiveTreasures().isEmpty()) return;

                for (Player p : Bukkit.getOnlinePlayers()) {

                    h = Hunt.getNearestHunt(p.getLocation());
                    if (h == null) continue;

                    Treasure treasure = h.getTreasure();
                    if (treasure == null) continue;

                    List<Entity> remainingMobs = treasure.getRemainingMobs();
                    int remainingMobsSize = remainingMobs.size();

                    int x = h.getLocation().getBlockX();
                    int z = h.getLocation().getBlockZ();
                    int offset = treasure.getTreasureData().getCoordsNearTreasure();
                    int x_offset = x + Utils.randInt(-offset, offset);
                    int z_offset = z + Utils.randInt(-offset, offset);
                    String remainingTime = Utils.getCountDown(h.getRemainingTime());
                    String worldName = h.getLocation().getWorld().getName();

                    //Sunt in aceeasi lume Hunt-ul si Player-ul
                    double mobWanderingDistance = treasure.getTreasureData().getMobWanderingDistance();
                    if (Locations.distanceSquaredTo(h.getLocation(), p.getLocation()) < mobWanderingDistance * mobWanderingDistance) //Inside the mob area
                    {

                        if (remainingMobsSize == 0) //All the mobs are dead
                        {

                            if (treasure.haveTheMobsSpawned()) {

                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar-all-mobs-dead")
                                        .replace("{time}", remainingTime)
                                        .replace("{world}", worldName)
                                        .replace("{x}", x + "")
                                        .replace("{z}", z + "")
                                        .replace("{x-offset}", x_offset + "")
                                        .replace("{z-offset}", z_offset + "")
                                ));
                            } else {

                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar")
                                        .replace("{time}", remainingTime)
                                        .replace("{world}", worldName)
                                        .replace("{x}", x + "")
                                        .replace("{z}", z + "")
                                        .replace("{x-offset}", x_offset + "")
                                        .replace("{z-offset}", z_offset + "")
                                ));
                            }

                        } else //There are mobs remaining
                        {

                            if (treasure.getTreasureData().enableMobTracker()) {

                                Entity first = remainingMobs.get(0);

                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar-mobs-tracker")
                                        .replace("{remaining_mobs}", remainingMobsSize + "")
                                        .replace("{world}", worldName)
                                        .replace("{x}", first.getLocation().getBlockX() + "")
                                        .replace("{y}", first.getLocation().getBlockY() + "")
                                        .replace("{z}", first.getLocation().getBlockZ() + "")));

                            } else {
                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar-mobs-remaining")
                                        .replace("{remaining_mobs}", remainingMobsSize + "")));
                            }

                        }

                    } else //Outside of the mob area
                    {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar")
                                .replace("{remaining_mobs}", remainingMobsSize + "")
                                .replace("{time}", remainingTime)
                                .replace("{world}", worldName)
                                .replace("{x}", x + "")
                                .replace("{z}", z + "")
                                .replace("{x-offset}", x_offset + "")
                                .replace("{z-offset}", z_offset + "")
                        ));
                    }
                }

            }

        };

        run.runTaskTimerAsynchronously(plugin, 0, Settings.actionbarRefresh());


    }

}

