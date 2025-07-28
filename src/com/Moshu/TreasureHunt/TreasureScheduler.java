package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Messages;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.objects.TreasureData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;
import java.util.Date;

public class TreasureScheduler {

    private String id;
    private String day;
    private String time;
    private String world;
    private String encodedCoords;
    private TreasureData data;
    private boolean enabled;

    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public TreasureScheduler()
    {}

    public void setId(String id) {
        this.id = id;
    }

    public void setDay(String day) {
         this.day = day;
     }

     public void setTime(String time) {
         this.time = time;
     }

     public void setWorld(String world) {
         this.world = world;
     }

     public void setEncodedCoords(String encodedCoords) {
         this.encodedCoords = encodedCoords;
     }

     public void setData(TreasureData data) {
        this.data = data;
     }

     public void setEnabled(boolean enabled) {
        this.enabled = enabled;
     }

    public boolean isEnabled() {
        return enabled;
    }

    private int getDayNumber()
    {
        switch (this.day.toLowerCase())
        {
            case "monday": return 1;
            case "tuesday": return 2;
            case "wednesday": return 3;
            case "thursday": return 4;
            case "friday": return 5;
            case "saturday": return 6;
            case "sunday": return 7;
            case "daily": return -1;

            default: return 0;
        }
    }

    public Location getLocation()
    {

        if(Bukkit.getWorld(world) == null)
        {
            Bukkit.getConsoleSender().sendMessage("&5MysticTreasures: &fScheduler '" + id + "' isn't configured properly!");
            return null;
        }

        World w =  Bukkit.getWorld(world);

        int i = 0;
        for(String s : encodedCoords.split(":"))
        {

            if(Utils.isInt(s))
            {
                i++;
            }

        }

        if(i != 3)
        {
            Bukkit.getConsoleSender().sendMessage("&5MysticTreasures: &fScheduler '" + id + "' isn't configured properly!");
            return null;
        }

        int x = Integer.parseInt(encodedCoords.split(":")[0]);
        int y = Integer.parseInt(encodedCoords.split(":")[1]);
        int z = Integer.parseInt(encodedCoords.split(":")[2]);

        return new Location(w,x,y,z);

    }

    public String getDay()
    {
        return day;
    }

    public String getTime()
    {
        return time;
    }

    public String getWorld()
    {
        return world;
    }

    public String getEncodedCoords()
    {
        return encodedCoords;
    }

    public TreasureData getData()
    {
        return data;
    }

    public String getId() {
        return id;
    }

    public boolean shouldSpawn()
    {

        if(!isEnabled()) return false;

        if(getDayNumber() == 0)
        {
            Bukkit.getConsoleSender().sendMessage("&5MysticTreasures: &fScheduler '" + id + "' isn't configured properly!");
            return false;
        }

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        if(!Utils.isInt(time.split(":")[0]) || !Utils.isInt(time.split(":")[1]))
        {
            Bukkit.getConsoleSender().sendMessage("&5MysticTreasures: &fScheduler '" + id + "' isn't configured properly!");
            return false;
        }

        int configHour = Integer.parseInt(time.split(":")[0]);
        int configMinute = Integer.parseInt(time.split(":")[1]);

        boolean condition = (day == getDayNumber() || getDayNumber() == -1) && hour == configHour && minute == configMinute;
        return condition;

    }

    public boolean spawn()
    {

        CommandSender sender = Bukkit.getConsoleSender();

        if (Hunt.isHuntActive(data.getIdentifier())) {
            sender.sendMessage(Messages.get("hunt-already-active"));
            return false;
        }

        if(getLocation() == null) {

            if(getEncodedCoords().equalsIgnoreCase("random")) {

                Hunt h = new Hunt(data.getIdentifier(), data.getDuration());
                sender.sendMessage(Messages.get("generating-treasure"));

                BukkitRunnable run = new BukkitRunnable() {

                    @Override
                    public void run() {

                        if (h.getLocation() == null) return;

                        h.start();
                        this.cancel();

                    }
                };

                run.runTaskTimerAsynchronously(plugin, 0, 1);

                return true;
            }

            return false;
        }

        Hunt h = new Hunt(getLocation(), data.getIdentifier(), data.getDuration());
        sender.sendMessage(Messages.get("generating-treasure"));

        h.start();
        return true;
    }

}
