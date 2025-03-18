package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Locations;
import com.Moshu.Misc.Messages;
import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class ActionBar {

    public static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public static void start()
    {

        if(!Settings.actionbar()) return;

        BukkitRunnable run = new BukkitRunnable() {

            Hunt h;

            @Override
            public void run() {

                if(Bukkit.getOnlinePlayers().isEmpty()) return;
                if(Hunt.getActiveHunts().isEmpty()) return;

                for(Player p : Bukkit.getOnlinePlayers()) {

                    h = Hunt.getNearestHunt(p.getLocation());
                    if (h == null) continue;

                        //Sunt in aceeasi lume Hunt-ul si Player-ul
                        if (Locations.distanceTo(h.getLocation(), p.getLocation()) < h.getTreasure().getTreasureData().getMobWanderingDistance()) //Inside the mob area
                        {

                            if (h.getTreasure().getRemainingMobs().isEmpty()) //All the mobs are dead
                            {

                                if (h.getTreasure().haveTheMobsSpawned()) {

                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar-all-mobs-dead")
                                            .replace("{time}", Utils.getCountDown(h.getRemainingTime()))
                                            .replace("{world}", h.getLocation().getWorld().getName())
                                            .replace("{x}", h.getLocation().getBlockX() + "")
                                            .replace("{z}", h.getLocation().getBlockZ() + "")));
                                } else {

                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar")
                                            .replace("{time}", Utils.getCountDown(h.getRemainingTime()))
                                            .replace("{world}", h.getLocation().getWorld().getName())
                                            .replace("{x}", h.getLocation().getBlockX() + "")
                                            .replace("{z}", h.getLocation().getBlockZ() + "")));
                                }

                            } else //There are mobs remaining
                            {

                                if (h.getTreasure().getTreasureData().enableMobTracker()) {

                                    Entity first = h.getTreasure().getRemainingMobs().get(0);

                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar-mobs-tracker")
                                            .replace("{remaining_mobs}", h.getTreasure().getRemainingMobs().size() + "")
                                            .replace("{world}", h.getLocation().getWorld().getName())
                                            .replace("{x}", first.getLocation().getBlockX() + "")
                                            .replace("{y}", first.getLocation().getBlockY() + "")
                                            .replace("{z}", first.getLocation().getBlockZ() + "")));

                                } else {
                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar-mobs-remaining")
                                            .replace("{remaining_mobs}", h.getTreasure().getRemainingMobs().size() + "")));
                                }

                            }

                        }
                        else //Outside of the mob area
                        {
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar")
                                    .replace("{remaining_mobs}", h.getTreasure().getRemainingMobs().size() + "")
                                    .replace("{time}", Utils.getCountDown(h.getRemainingTime()))
                                    .replace("{world}", h.getLocation().getWorld().getName())
                                    .replace("{x}", h.getLocation().getBlockX() + "")
                                    .replace("{z}", h.getLocation().getBlockZ() + "")));
                        }
                    }

            }

        };

        run.runTaskTimerAsynchronously(plugin, 0, Settings.actionbarRefresh());


    }

}
