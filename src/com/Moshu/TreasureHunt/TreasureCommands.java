package com.Moshu.TreasureHunt;

import com.Moshu.Main;
import com.Moshu.Misc.Messages;
import com.Moshu.Misc.SendCenteredMessage;
import com.Moshu.Misc.Settings;
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

    private static Main plugin;

    public TreasureCommands(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {

        if(cmd.getName().equalsIgnoreCase("hunt"))
        {

            if(args.length == 0) {

                if (sender instanceof Player p) {

                    if (p.hasPermission("mystictreasures.hunt")) {

                        if (Hunt.isActive(p.getWorld())) {

                            Hunt h = Hunt.getHunt(p.getWorld());
                            SendCenteredMessage scm = new SendCenteredMessage();

                            for (String s : Messages.getAndFormatList("messages.hunt-message")) {
                                scm.sendCenteredMessage(p, s.replace("{x}", h.getLocation().getBlockX() + "")
                                        .replace("{z}", h.getLocation().getBlockZ() + "")
                                        .replace("{world}", h.getWorld() + "")
                                        .replace("{duration}", h.getDuration() + ""));
                            }

                            return true;

                        } else {
                            p.sendMessage(Messages.get("no-hunt-in-this-world"));
                        }

                        return true;
                    } else {
                        p.sendMessage(Messages.get("no-permission"));
                    }

                }
                else
                {

                    Bukkit.getConsoleSender().sendMessage(Utils.format("&5Treasures: &fHunts are active in the following worlds"));
                    Bukkit.getConsoleSender().sendMessage(Utils.format("&5Treasures: &fUse /hunt start/stop (World) to control the hunts"));

                    for(World w : Bukkit.getWorlds())
                    {
                        Bukkit.getConsoleSender().sendMessage(Utils.format("&5Treasures: &f" + w.getName() + " - " + Hunt.isActive(w)));
                    }

                }

            }
            else if(args.length == 1)
            {
                sender.sendMessage(Messages.get("wrong-command"));
                return true;
            }
            else if(args.length == 2)
            {

                if(sender instanceof Player p) {

                    if(!p.hasPermission("mystictreasures.admin")) {
                        p.sendMessage(Messages.get("no-permission"));
                        return true;
                    }

                }

                if(args[0].equalsIgnoreCase("start"))
                {

                    World w = Bukkit.getWorld(args[1]);

                    if(w == null)
                    {
                        sender.sendMessage(Messages.get("inexistent-world"));
                        return true;
                    }

                    if(Hunt.isActive(w))
                    {
                        sender.sendMessage(Messages.get("hunt-already-active"));
                        return true;
                    }

                    Hunt h = new Hunt(w, Settings.getWorldIntUnknown(w.getName(), "duration"));
                    sender.sendMessage(Messages.get("generating-treasure"));

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
                    sender.sendMessage(Messages.get("treasure-generated"));

                }
                else if(args[0].equalsIgnoreCase("stop"))
                {

                    World w = Bukkit.getWorld(args[1]);

                    if(w == null)
                    {
                        sender.sendMessage(Messages.get("inexistent-world"));
                        return true;
                    }

                    if(!Hunt.isActive(w))
                    {
                        sender.sendMessage(Messages.get("hunt-not-active"));
                        return true;
                    }

                    Hunt.getHunt(w).stop();
                    sender.sendMessage(Messages.get("hunt-stopped"));

                }
                else if(args[0].equalsIgnoreCase("reload"))
                {

                    sender.sendMessage(Utils.format("&5Treasures: &fConfig reloaded!"));
                    plugin.reloadFiles();

                }
                else
                {
                    sender.sendMessage(Messages.get("wrong-command"));
                }
            }
            else
            {
                sender.sendMessage(Messages.get("wrong-command"));
            }

        }

        return true;
    }

}
