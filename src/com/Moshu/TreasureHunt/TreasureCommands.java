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

import java.util.ArrayList;

public class TreasureCommands implements CommandExecutor {

    private static Main plugin;

    public TreasureCommands(Main plugin) {
        this.plugin = plugin;
    }

    private static ArrayList<Player> debugging = new ArrayList<Player>();

    public static boolean isDebugging(Player p) {
        return debugging.contains(p);
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

                    Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fHunts are active in the following worlds"));
                    Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fUse /hunt start/stop (World) to control the hunts"));

                    for(World w : Bukkit.getWorlds())
                    {
                        Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &f" + w.getName() + " - " + Hunt.isActive(w)));
                    }

                }

            }
            else if(args.length == 1) {

                if(args[0].equalsIgnoreCase("start"))
                {

                    if(sender instanceof Player p) {

                        if(!p.hasPermission("mystictreasures.admin")) {
                            p.sendMessage(Messages.get("no-permission"));
                            return true;
                        }

                        World w = p.getWorld();

                        if(Hunt.isActive(w) || Hunt.huntStarting(w))
                        {
                            sender.sendMessage(Messages.get("hunt-already-active"));
                            return true;
                        }

                        Hunt.addHunt(w);
                        Hunt h = new Hunt(w, Settings.getWorldIntUnknown(w.getName(), "duration"));
                        sender.sendMessage(Messages.get("generating-treasure"));

                        BukkitRunnable run = new BukkitRunnable()
                        {

                            @Override
                            public void run() {

                                if(h.getLocation() == null) return;

                                sender.sendMessage(Messages.get("treasure-generated"));
                                h.start();
                                this.cancel();

                            }
                        };

                        run.runTaskTimerAsynchronously(plugin, 0, 1);

                    }
                    else
                    {
                        sender.sendMessage(Messages.get("wrong-command"));
                    }

                }
                else if(args[0].equalsIgnoreCase("stop"))
                {

                    if(sender instanceof Player p) {

                        if(!p.hasPermission("mystictreasures.admin")) {
                            p.sendMessage(Messages.get("no-permission"));
                            return true;
                        }

                        World w = p.getWorld();
                        if(!Hunt.isActive(w))
                        {
                            sender.sendMessage(Messages.get("hunt-not-active"));
                            return true;
                        }

                        Hunt.getHunt(w).stop();
                        sender.sendMessage(Messages.get("hunt-stopped"));

                    }
                    else
                    {
                        sender.sendMessage(Messages.get("wrong-command"));
                    }

                }
                else if(args[0].equalsIgnoreCase("debug"))
                {

                    if(sender instanceof Player p) {

                        if(debugging.contains(p))
                        {
                            p.sendMessage(Utils.format("&6&lTreasure&e&lHunt &fYou've stopped debugging"));
                            debugging.remove(p);
                        }
                        else
                        {
                            p.sendMessage(Utils.format("&6&lTreasure&e&lHunt &fYou started debugging. Right click an ArmorStand (the falling treasure) to gain more information and remove it"));
                            debugging.add(p);
                        }

                    }
                    else
                    {
                        Utils.sendNotPlayer();
                    }

                }
                else if (args[0].equalsIgnoreCase("reload")) {

                    sender.sendMessage(Messages.get("config-reload"));
                    plugin.reloadFiles();

                }
                else if(args[0].equalsIgnoreCase("help"))
                {

                    sender.sendMessage(Utils.format("&6&lTreasure&e&lHunt &fHelp page"));
                    sender.sendMessage(" ");
                    sender.sendMessage(Utils.format("  &6/hunt start &8(&fStarts a hunt at a random location in the player's world&8)"));
                    sender.sendMessage(Utils.format("  &6/hunt stop &8(&fStops the hunt in the player's world&8)"));
                    sender.sendMessage(Utils.format("  &6/hunt start &eworld &8(&fStarts a hunt at a random location in the player's world&8)"));
                    sender.sendMessage(Utils.format("  &6/hunt start &eworld &8(&fStops the hunt in that world&8)"));
                    sender.sendMessage(Utils.format("  &6/hunt start &ehere &8(&fStarts a hunt at the player's location&8)"));
                    sender.sendMessage(Utils.format("  &6/hunt reload &8(&fReloads the config & messages- not all config values can be reloaded&8)"));
                    sender.sendMessage(" ");


                }
                else {
                    sender.sendMessage(Messages.get("wrong-command"));
                }

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

                    if(sender instanceof Player p) {

                        if(args[1].equalsIgnoreCase("here"))
                        {

                            World w = p.getWorld();

                            if(Hunt.isActive(w) || Hunt.huntStarting(w))
                            {
                                sender.sendMessage(Messages.get("hunt-already-active"));
                                return true;
                            }

                            Hunt.addHunt(w);
                            Hunt h = new Hunt(p.getLocation(), Settings.getWorldIntUnknown(w.getName(), "duration"));
                            sender.sendMessage(Messages.get("generating-treasure"));

                            h.start();
                            sender.sendMessage(Messages.get("treasure-generated"));

                            return true;
                        }

                    }

                    World w = Bukkit.getWorld(args[1]);

                    if(w == null)
                    {
                        sender.sendMessage(Messages.get("inexistent-world"));
                        return true;
                    }

                    if(Hunt.isActive(w) || Hunt.huntStarting(w))
                    {
                        sender.sendMessage(Messages.get("hunt-already-active"));
                        return true;
                    }

                    Hunt.addHunt(w);
                    Hunt h = new Hunt(w, Settings.getWorldIntUnknown(w.getName(), "duration"));
                    sender.sendMessage(Messages.get("generating-treasure"));

                    BukkitRunnable run = new BukkitRunnable()
                    {

                        @Override
                        public void run() {

                            if(h.getLocation() == null) return;

                            sender.sendMessage(Messages.get("treasure-generated"));
                            h.start();
                            this.cancel();

                        }
                    };

                    run.runTaskTimerAsynchronously(plugin, 0, 1);


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
