/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.Moshu.Misc.Storage;


import com.Moshu.Main;
import com.Moshu.Misc.Utils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;

public class Messages {

    private static Main plugin;

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public Messages(Main plugin) {
        Messages.plugin = plugin;
    }

    public static String get(String path) {
        return Utils.format(plugin.getMessages().getString("messages." + path, "&6&lTreasure&e&lHunt &fNull message, check messages.yml for " + path + " and add it!"));
    }

    public static ArrayList<String> getAndFormatList(String path) {

        List<String> list = plugin.getMessages().getStringList(path);
        ArrayList<String> newList = new ArrayList<>();

        for (String s : list) {
            newList.add(Utils.format(s));
        }

        return newList;

    }

    public static ArrayList<String> getAndFormatLore(String path) {

        List<String> list = plugin.getConfig().getStringList(path);
        ArrayList<String> newList = new ArrayList<>();

        for (String s : list) {
            newList.add(Utils.format(s));
        }

        return newList;

    }

}

