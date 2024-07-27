package com.Moshu.TreasureHunt;

import com.Moshu.Misc.SendCenteredMessage;
import com.Moshu.Misc.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TreasureCommands implements CommandExecutor {

    private Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {

        if(cmd.getName().equalsIgnoreCase("hunt"))
        {

            if(sender instanceof Player p)
            {

                if(!p.hasPermission("engine.treasurehunt"))
                {

                    if(Hunt.isActive(p.getWorld()))
                    {

                        SendCenteredMessage scm = new SendCenteredMessage();

                        p.sendMessage(" ");
                        scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&6&lTreasure &e&lHunt"));
                        p.sendMessage(" ");
                        scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&fO comoara misterioasa a fost descoperita in lume!"));
                        scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&fX: &7" + Hunt.getHunt(p.getWorld()).getLocation().getBlockX() + " &fZ: &7" + Hunt.getHunt(p.getWorld()).getLocation().getBlockZ()));
                        scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&fMult noroc in a o gasi!"));
                        p.sendMessage(" ");
                        scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&7&o((Tip: Comoara va disparea in 20 de minute))"));
                        p.sendMessage(" ");
                        return true;

                    }


                    Utils.sendNoAccess(p);
                    return true;
                }

            }

            if(args.length == 0)
            {

                if(sender instanceof Player p && Hunt.isActive(p.getWorld()))
                {
                    SendCenteredMessage scm = new SendCenteredMessage();

                    p.sendMessage(" ");
                    scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&6&lTreasure &e&lHunt"));
                    p.sendMessage(" ");
                    scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&fO comoara misterioasa a fost descoperita in lume!"));
                    scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&fX: &7" + Hunt.getHunt(p.getWorld()).getLocation().getBlockX() + " &fZ: &7" + Hunt.getHunt(p.getWorld()).getLocation().getBlockZ()));
                    scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&fMult noroc in a o gasi!"));
                    p.sendMessage(" ");
                    scm.sendCenteredMessage(p, ChatColor.translateAlternateColorCodes('&', "&7&o((Tip: Comoara va disparea in 20 de minute))"));
                    p.sendMessage(" ");
                    return true;
                }

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lTreasure&e&lHunt &fFoloseste /hunt start/stop"));
                return true;
            }
            else if(args.length == 1)
            {

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lTreasure&e&lHunt &fFoloseste /hunt start/stop (World)"));
                return true;

            }
            else if(args.length == 2)
            {
                if(args[0].equalsIgnoreCase("start"))
                {

                    World w = Bukkit.getWorld(args[1]);

                    if(w == null)
                    {

                        return true;
                    }

                    if(Hunt.isActive(w))
                    {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lTreasure&e&lHunt &fDeja este o vanatoare de comori in desfasurare"));
                        return true;
                    }

                    Hunt h = new Hunt(w, 20);

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lTreasure&e&lHunt &fSe genereaza comoara.."));

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

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lTreasure&e&lHunt &fAi activat o vanatoare de comori!"));

                }
                else if(args[0].equalsIgnoreCase("stop"))
                {

                    World w = Bukkit.getWorld(args[1]);

                    if(w == null)
                    {

                        return true;
                    }

                    if(!Hunt.isActive(w))
                    {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lTreasure&e&lHunt &fNu este nici o vanatoare de comori in desfasurare"));
                        return true;
                    }

                    Hunt.getHunt(w).stop();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lTreasure&e&lHunt &fAi oprit vanatoarea de comori!"));

                }
                else
                {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lTreasure&e&lHunt &fFoloseste /hunt start/stop"));
                }
            }
            else
            {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lTreasure&e&lHunt &fFoloseste /hunt start/stop"));
            }

        }

        return true;
    }

}
