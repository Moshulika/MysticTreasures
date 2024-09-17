package com.Moshu.Misc;

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

    private boolean isPlugin(String s) {

        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {

            if (s.toLowerCase().contains(p.getName().toLowerCase())) return true;

            if (s.length() >= 6) {
                if (p.getName().toLowerCase().contains(s.toLowerCase())) return true;
            }

        }

        return false;

    }


    private boolean staffOnly(String s) {

        if (Bukkit.getServer().getPluginCommand(s) == null) return true;

        String name = Bukkit.getServer().getPluginCommand(s).getPlugin().getName();
        return name.equals("LiteBans") || name.equals("KiteBoard") || name.equals("LuckPerms") || name.equals("Multiverse-Core") || name.equals("PlaceholderAPI");
    }


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
                    c.add("reload");
                    c.add("stop");
                    c.add("debug");
                    c.add("clear");
                }

                ArrayList<String> completions = new ArrayList<>();

                return StringUtil.copyPartialMatches(args[0], c, completions);

            }

            if (args.length == 2) {

                if (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("stop")) {

                    ArrayList<String> c = new ArrayList<>();

                    c.add("here");

                    for(World w : Bukkit.getWorlds())
                    {
                        c.add(w.getName());
                    }

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[1], c, completions);

                }

            }

            return empty;
        } else {
            return empty;
        }

    }



}
