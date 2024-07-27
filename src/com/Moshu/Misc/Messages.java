package com.Moshu.Misc;


import com.Moshu.Main;

import java.util.ArrayList;
import java.util.List;

public class Messages {

    private static Main plugin;

    public Messages(Main plugin)
    {
        this.plugin = plugin;
    }

    public static String get(String path)
    {
        return Utils.format(plugin.getMessages().getString("messages." + path, "Null message, check messages.yml"));
    }

    public static ArrayList<String> getAndFormatList(String path)
    {

        List<String> list = plugin.getMessages().getStringList(path);
        ArrayList<String> newList = new ArrayList<>();

        for(String s : list)
        {
            newList.add(Utils.format(s));
        }

        return newList;

    }

    public static ArrayList<String> getAndFormatLore(String path)
    {

        List<String> list = plugin.getConfig().getStringList(path);
        ArrayList<String> newList = new ArrayList<>();

        for(String s : list)
        {
            newList.add(Utils.format(s));
        }

        return newList;

    }

}
