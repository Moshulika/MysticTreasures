package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Storage.Messages;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Components.TreasureData;
import com.Moshu.TreasureHunt.Core.Hunt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;

/**
 * Manages scheduled treasure hunts with configurable timing and location settings.
 * This class handles the scheduling logic for treasure hunts, including day/time validation,
 * location parsing, and automatic treasure spawning based on configured schedules.
 * 
 * @author Moshu
 * @version 1.0
 */
public class TreasureScheduler {

    private String id;
    private String day;
    private String time;
    private String world;
    private String encodedCoords;
    private TreasureData data;
    private boolean enabled;

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    /**
     * Default constructor for TreasureScheduler.
     * Creates a new scheduler instance with default values.
     */
    public TreasureScheduler()
    {}

    /**
     * Sets the unique identifier for this scheduler.
     * 
     * @param id The unique identifier string for the scheduler
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the day of the week when the treasure hunt should spawn.
     * Valid values: "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "daily"
     * 
     * @param day The day of the week for scheduling
     */
    public void setDay(String day) {
         this.day = day;
     }

    /**
     * Sets the time when the treasure hunt should spawn.
     * Expected format: "HH:MM" (24-hour format)
     * 
     * @param time The time in HH:MM format when the treasure should spawn
     */
    public void setTime(String time) {
         this.time = time;
     }

    /**
     * Sets the world where the treasure hunt should spawn.
     * 
     * @param world The name of the world for the treasure hunt
     */
    public void setWorld(String world) {
         this.world = world;
     }

    /**
     * Sets the encoded coordinates for the treasure hunt location.
     * Format: "x:y:z" or "random" for random location generation
     * 
     * @param encodedCoords The encoded coordinates string or "random"
     */
    public void setEncodedCoords(String encodedCoords) {
         this.encodedCoords = encodedCoords;
     }

    /**
     * Sets the treasure data configuration for this scheduler.
     * 
     * @param data The TreasureData object containing hunt configuration
     */
    public void setData(TreasureData data) {
        this.data = data;
     }

    /**
     * Sets whether this scheduler is enabled or disabled.
     * 
     * @param enabled True to enable the scheduler, false to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
     }

    /**
     * Checks if this scheduler is currently enabled.
     * 
     * @return True if the scheduler is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Converts a day string to its corresponding Calendar day number.
     * 
     * @return The Calendar day number (1-7 for days of week, -1 for daily, 0 for invalid)
     */
    private int getDayNumber()
    {
        switch (this.day.toLowerCase())
        {
            case "sunday": return 1;
            case "monday": return 2;
            case "tuesday": return 3;
            case "wednesday": return 4;
            case "thursday": return 5;
            case "friday": return 6;
            case "saturday": return 7;
            case "daily": return -1;

            default: return 0;
        }
    }

    /**
     * Parses the encoded coordinates and world to create a Location object.
     * Validates that the world exists and coordinates are properly formatted.
     * 
     * @return The parsed Location object, or null if validation fails
     */
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

    /**
     * Gets the configured day of the week for this scheduler.
     * 
     * @return The day string (e.g., "monday", "daily")
     */
    public String getDay()
    {
        return day;
    }

    /**
     * Gets the configured time for this scheduler.
     * 
     * @return The time string in HH:MM format
     */
    public String getTime()
    {
        return time;
    }

    /**
     * Gets the configured world name for this scheduler.
     * 
     * @return The world name string
     */
    public String getWorld()
    {
        return world;
    }

    /**
     * Gets the encoded coordinates string for this scheduler.
     * 
     * @return The encoded coordinates string or "random"
     */
    public String getEncodedCoords()
    {
        return encodedCoords;
    }

    /**
     * Gets the treasure data configuration for this scheduler.
     * 
     * @return The TreasureData object
     */
    public TreasureData getData()
    {
        return data;
    }

    /**
     * Gets the unique identifier for this scheduler.
     * 
     * @return The scheduler ID string
     */
    public String getId() {
        return id;
    }

    /**
     * Determines if a treasure hunt should spawn based on current time and scheduler configuration.
     * Compares the current day and time with the configured schedule, accounting for daily spawns.
     * Logs debug information about time comparisons.
     * 
     * @return True if the treasure should spawn now, false otherwise
     */
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

    /**
     * Attempts to spawn a treasure hunt based on the scheduler configuration.
     * Handles both fixed location and random location spawning.
     * For random locations, uses asynchronous task to wait for location generation.
     * Validates that no hunt is already active for the same treasure type.
     * 
     * @return True if the treasure hunt was successfully initiated, false otherwise
     */
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
                h.startOnLocationFound();

                return true;
            }
            else
            {
                plugin.getLogger().warning("Scheduler '" + id + "' isn't configured properly! Location is null and isn't set as random");
            }

            return false;
        }

        Hunt h = new Hunt(getLocation(), data.getIdentifier(), data.getDuration());
        sender.sendMessage(Messages.get("generating-treasure"));

        h.start();
        return true;
    }

}
