package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Messages;
import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
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

            World w;

            @Override
            public void run() {

                if(Bukkit.getOnlinePlayers().isEmpty()) return;

                for(Player p : Bukkit.getOnlinePlayers())
                {

                    w = p.getWorld();

                    if(Hunt.isActive(w) && Hunt.getHunt(w) != null &&
                            Hunt.getHunt(w).getTreasure() != null &&
                            Hunt.getHunt(w).getTreasure().isActive())
                    {

                        if(Hunt.getHunt(w) == null) break;

                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Messages.get("actionbar")
                                .replace("{time}", Utils.getCountDown(Hunt.getHunt(w).getRemainingTime()))
                                .replace("{x}", Hunt.getHunt(w).getLocation().getBlockX() + "")
                                .replace("{z}", Hunt.getHunt(w).getLocation().getBlockZ() + "")));
                    }

                }

            }

        };

        run.runTaskTimerAsynchronously(plugin, 0, Settings.actionbarRefresh());


    }

}
