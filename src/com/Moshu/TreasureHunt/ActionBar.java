package com.Moshu.TreasureHunt;

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

    private static void sendNearTreasure()
    {

    }

    public static void start()
    {

        if(!Settings.actionbar()) return;

        BukkitRunnable run = new BukkitRunnable() {

            World w;

            @Override
            public void run() {

                if(Bukkit.getOnlinePlayers().isEmpty()) return;

                for(Player p : Bukkit.getOnlinePlayers())
                {

                    w = p.getWorld();
                    if(Hunt.getHunt(w) == null) break;

                    if(Hunt.isActive(w) && Hunt.getHunt(w) != null &&
                            Hunt.getHunt(w).getTreasure() != null &&
                            Hunt.getHunt(w).getTreasure().isActive())
                    {

                        Hunt h = Hunt.getHunt(w);

                        if(h.getLocation().distance(p.getLocation()) < Settings.getWorldIntUnknown(w.getName(), "mob-wandering-distance")) //Inside the mob area
                        {

                            if(h.getTreasure().getRemainingMobs().isEmpty()) //All the mobs are dead
                            {

                                if(h.getTreasure().haveTheMobsSpawned())
                                {
                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar-all-mobs-dead")
                                            .replace("{time}", Utils.getCountDown(Hunt.getHunt(w).getRemainingTime()))
                                            .replace("{x}", Hunt.getHunt(w).getLocation().getBlockX() + "")
                                            .replace("{z}", Hunt.getHunt(w).getLocation().getBlockZ() + "")));
                                }
                                else
                                {
                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar")
                                            .replace("{time}", Utils.getCountDown(Hunt.getHunt(w).getRemainingTime()))
                                            .replace("{x}", Hunt.getHunt(w).getLocation().getBlockX() + "")
                                            .replace("{z}", Hunt.getHunt(w).getLocation().getBlockZ() + "")));
                                }

                            }
                            else //There are mobs remaining
                            {

                                if(Settings.getWorldBooleanUnknown(w.getName(), "enable-mob-tracker"))
                                {

                                    Entity first = h.getTreasure().getRemainingMobs().get(0);

                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar-mobs-tracker")
                                            .replace("{remaining_mobs}", h.getTreasure().getRemainingMobs().size() + "")
                                            .replace("{x}", first.getLocation().getBlockX() + "")
                                            .replace("{y}", first.getLocation().getBlockY() + "")
                                            .replace("{z}", first.getLocation().getBlockZ() + "")));

                                }
                                else
                                {
                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar-mobs-remaining")
                                            .replace("{remaining_mobs}", h.getTreasure().getRemainingMobs().size() + "")));
                                }

                            }

                        }
                        else //Outside of the mob area
                        {
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar")
                                    .replace("{remaining_mobs}", h.getTreasure().getRemainingMobs().size() + "")
                                    .replace("{time}", Utils.getCountDown(Hunt.getHunt(w).getRemainingTime()))
                                    .replace("{x}", Hunt.getHunt(w).getLocation().getBlockX() + "")
                                    .replace("{z}", Hunt.getHunt(w).getLocation().getBlockZ() + "")));
                        }

                    }

                }

            }

        };

        run.runTaskTimerAsynchronously(plugin, 0, Settings.actionbarRefresh());


    }

}
