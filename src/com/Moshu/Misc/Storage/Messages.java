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

