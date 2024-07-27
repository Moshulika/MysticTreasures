package com.Moshu.Misc;

import org.bukkit.Bukkit;
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


    private boolean canExecute(Player p, String command) {

        if (Utils.isPaper()) {
            if (Bukkit.getCommandMap().getCommand(command) != null) {
                return Bukkit.getCommandMap().getCommand(command).testPermission(p);
            }

        }

        return true;

    }


    private boolean staffOnly(String s) {

        if (Bukkit.getServer().getPluginCommand(s) == null) return true;

        String name = Bukkit.getServer().getPluginCommand(s).getPlugin().getName();
        return name.equals("LiteBans") || name.equals("KiteBoard") || name.equals("LuckPerms") || name.equals("Multiverse-Core") || name.equals("PlaceholderAPI");
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("turfs")) {

            if (!(sender instanceof Player)) {

                return empty;
            }

            Player p = (Player) sender;

            if (args.length == 1) {

                ArrayList<String> c = new ArrayList<>();

                c.add("menu");
                c.add("claim");
                c.add("delete");
                c.add("locate");
                c.add("visualise");
                c.add("allow");
                c.add("remove");
                c.add("help");
                c.add("map");
                c.add("flags");
                c.add("tokens");
                c.add("deny");
                c.add("denied");

                if(p.hasPermission("mysticturfs.admin"))
                {
                    c.add("admin");
                    c.add("info");
                    c.add("block");
                    c.add("reload");
                }

                ArrayList<String> completions = new ArrayList<>();

                return StringUtil.copyPartialMatches(args[0], c, completions);

            }

            if (args.length == 2) {

                if (args[0].equalsIgnoreCase("allow") || args[0].equalsIgnoreCase("remove")) {

                    ArrayList<String> c = new ArrayList<>();

                    for (Player k : Bukkit.getOnlinePlayers()) {
                        c.add(k.getName());
                    }

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[1], c, completions);

                }

                if (args[0].equalsIgnoreCase("deny")) {

                    ArrayList<String> c = new ArrayList<>();

                    for (Player k : Bukkit.getOnlinePlayers()) {
                        c.add(k.getName());
                    }

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[1], c, completions);

                }

                if (args[0].equalsIgnoreCase("locate")) {

                    ArrayList<String> c = new ArrayList<>();

                    for (Player k : Bukkit.getOnlinePlayers()) {
                        c.add(k.getName());
                    }

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[1], c, completions);

                }

                if (args[0].equalsIgnoreCase("visualise")) {

                    ArrayList<String> c = new ArrayList<>();

                    c.add("all");

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[1], c, completions);

                }

                if (args[0].equalsIgnoreCase("info")) {
                    ArrayList<String> c = new ArrayList<>();

                    for (Player k : Bukkit.getOnlinePlayers()) {
                        c.add(k.getName());
                    }

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[1], c, completions);
                }

                if (args[0].equalsIgnoreCase("claim")) {

                    ArrayList<String> c = new ArrayList<>();

                    c.add("4");
                    c.add("5");
                    c.add("6");
                    c.add("7");
                    c.add("8");
                    c.add("9");

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[1], c, completions);

                }

                if (args[0].equalsIgnoreCase("tokens")) {

                    ArrayList<String> c = new ArrayList<>();

                    for (Player k : Bukkit.getOnlinePlayers()) {
                        c.add(k.getName());
                    }

                    ArrayList<String> completions = new ArrayList<>();
                    return StringUtil.copyPartialMatches(args[1], c, completions);
                }


                return empty;

            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("allow") || args[0].equalsIgnoreCase("remove")) {

                    ArrayList<String> c = new ArrayList<>();

                    c.add("all");

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[2], c, completions);

                }

                if (args[0].equalsIgnoreCase("tokens")) {
                    ArrayList<String> c = new ArrayList<>();

                    c.add("add");
                    c.add("remove");

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[2], c, completions);
                }
            }

            return empty;
        } else {
            return empty;
        }

    }



}
