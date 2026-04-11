/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.Moshu.TreasureHunt.Components.Rewards;

import com.Moshu.Misc.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Represents a command reward that can be executed for players during treasure hunts.
 * This class manages the execution of console commands as rewards, with support
 * for chance-based execution and top-X player targeting.
 *
 * @author Moshu
 * @version 1.0
 */
public class CommandReward {


    //TODO: Add custom item support to showcase items of command rewards

    /**
     * Creates a new command reward with default values.
     */
    public CommandReward() {
        this.identifier = null;
        this.command = null;
        this.chance = 0;
        this.rewardToTopX = 0;
    }

    private String identifier; // e.g., "money"
    private String command;
    private int chance;
    private String menuItem;
    private int rewardToTopX;

    // Getters and setters
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setRewardToTopX(int rewardToTopX) {
        this.rewardToTopX = rewardToTopX;
    }

    public int getRewardToTopX() {
        return this.rewardToTopX;
    }

    public boolean shouldRewardToTopX() {
        return this.rewardToTopX > 0;
    }

    public boolean isTopX(int currentTop) {
        return currentTop == rewardToTopX;
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

    public String getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(String menuItem) {
        this.menuItem = menuItem;
    }

    /**
     * Executes the command reward for the specified player.
     * The command is executed with chance-based probability and player name replacement.
     *
     * @param p The player to execute the command for
     */
    public void run(Player p) {

        if (Utils.chance() < getChance()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", p.getName()));
        }

    }

}

