package com.Moshu.Misc;

import com.Moshu.TreasureHunt.objects.TreasureData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter, Listener {

    public static final ArrayList<String> empty = new ArrayList<>();


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("hunt")) {

            if (!(sender instanceof Player)) {

                return empty;
            }

            Player p = (Player) sender;

            if (args.length == 1) {

                ArrayList<String> c = new ArrayList<>();

                if(p.hasPermission("mystictreasures.admin"))
                {
                    c.add("help");
                    c.add("start");
                    c.add("stop");
                    c.add("key");
                    c.add("reload");
                    c.add("debug");
                    c.add("clear");
                }

                ArrayList<String> completions = new ArrayList<>();

                return StringUtil.copyPartialMatches(args[0], c, completions);

            }

            if (args.length == 2) {

                if (args[0].equalsIgnoreCase("start")) {

                    ArrayList<String> c = new ArrayList<>();

                    c.add("here");
                    c.addAll(TreasureData.getTreasureIdentifiers());

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[1], c, completions);

                }
                else if(args[0].equalsIgnoreCase("stop"))
                {
                    ArrayList<String> c = new ArrayList<>();
                    c.addAll(TreasureData.getTreasureIdentifiers());

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[1], c, completions);
                }
                else if(args[0].equalsIgnoreCase("key"))
                {
                    ArrayList<String> completions = new ArrayList<>();
                    return StringUtil.copyPartialMatches(args[1], Utils.getOnlinePlayersNames(), completions);
                }

            }

            if(args.length == 3) {
                if (args[0].equalsIgnoreCase("key")) {
                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[2], TreasureData.getTreasureIdentifiers(), completions);
                }
                else if(args[0].equalsIgnoreCase("start"))
                {

                    if(args[1].equalsIgnoreCase("here"))
                    {
                        ArrayList<String> completions = new ArrayList<>();
                        return StringUtil.copyPartialMatches(args[2], TreasureData.getTreasureIdentifiers(), completions);
                    }

                }
            }

            if(args.length == 4) {
                if (args[0].equalsIgnoreCase("key")) {
                    ArrayList<String> c = new ArrayList<>();

                    c.add("1");
                    c.add("2");
                    c.add("3");
                    c.add("4");
                    c.add("5");
                    c.add("10");
                    c.add("16");
                    c.add("32");

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[3], c, completions);
                }
            }

            return empty;
        } else {
            return empty;
        }

    }



}
