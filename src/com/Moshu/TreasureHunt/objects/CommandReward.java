package com.Moshu.TreasureHunt.objects;

import com.Moshu.Misc.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandReward {

    public CommandReward()
    {
        this.identifier = null;
        this.command = null;
        this.chance = 0;
    }

    private String identifier; // e.g., "money"
    private String command;
    private int chance;
    private String menuItem;

    // Getters and setters
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getCommand() {
        return command;
    }
    public void setCommand(String command) {
        this.command = command;
    }
    public int getChance() {
        return chance;
    }
    public void setChance(int chance) {
        this.chance = chance;
    }
    public String getMenuItem() { return menuItem; }
    public void setMenuItem(String menuItem) { this.menuItem = menuItem; }

    public void run(Player p)
    {

        if(Utils.chance() < getChance())
        {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", p.getName()));
        }

    }

}
