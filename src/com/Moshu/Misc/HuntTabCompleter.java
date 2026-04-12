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

package com.Moshu.Misc;

import com.Moshu.TreasureHunt.Components.TreasureData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HuntTabCompleter implements org.bukkit.command.TabCompleter, Listener {

    private static final List<String> EMPTY = Collections.emptyList();


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("hunt")) {

            if (!(sender instanceof Player)) {

                return EMPTY;
            }

            Player p = (Player) sender;

            if (args.length == 1) {

                ArrayList<String> c = new ArrayList<>();

                if (p.hasPermission("mystictreasures.admin")) {
                    c.add("help");
                    c.add("start");
                    c.add("stop");
                    c.add("key");
                    c.add("reload");
                    c.add("debug");
                    c.add("clear");
                }

                c.add("showcase");

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

                } else if (args[0].equalsIgnoreCase("stop")) {
                    ArrayList<String> c = new ArrayList<>();
                    c.addAll(TreasureData.getTreasureIdentifiers());

                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[1], c, completions);
                } else if (args[0].equalsIgnoreCase("key")) {
                    ArrayList<String> completions = new ArrayList<>();
                    return StringUtil.copyPartialMatches(args[1], Utils.getOnlinePlayersNames(), completions);
                }

            }

            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("key")) {
                    ArrayList<String> completions = new ArrayList<>();

                    return StringUtil.copyPartialMatches(args[2], TreasureData.getTreasureIdentifiers(), completions);
                } else if (args[0].equalsIgnoreCase("start")) {

                    if (args[1].equalsIgnoreCase("here")) {
                        ArrayList<String> completions = new ArrayList<>();
                        return StringUtil.copyPartialMatches(args[2], TreasureData.getTreasureIdentifiers(), completions);
                    }

                }
            }

            if (args.length == 4) {
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

            return EMPTY;
        } else {
            return EMPTY;
        }

    }


}

