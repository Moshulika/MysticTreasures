/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.Moshu.TreasureHunt.Components;

import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeper;
import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeperDrops;
import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeperEquipment;
import com.Moshu.TreasureHunt.Components.Rewards.CommandReward;
import com.Moshu.TreasureHunt.Components.Rewards.ItemReward;
import com.Moshu.TreasureHunt.Core.Treasure;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class RoundData {

    private final String id;
    private final ArrayList<TreasureKeeper> treasureKeepers = new ArrayList<>();
    private final ArrayList<ItemReward> itemRewards = new ArrayList<>();
    private final ArrayList<CommandReward> commandRewards = new ArrayList<>();
    private TreasureDebuff debuff;
    private Treasure.AwardMethod awardMethod;
    private int rewardTopX = 0;

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public RoundData(String id, ConfigurationSection section) {
        this.id = id;
        load(section);
    }

    public String getId() {
        return id;
    }

    public List<TreasureKeeper> getTreasureKeepers() {
        return Collections.unmodifiableList(treasureKeepers);
    }

    public List<ItemReward> getItemRewards() {
        return Collections.unmodifiableList(itemRewards);
    }

    public List<CommandReward> getCommandRewards() {
        return Collections.unmodifiableList(commandRewards);
    }

    public TreasureDebuff getDebuff() {
        return debuff;
    }

    public Treasure.AwardMethod getAwardMethod() {
        return awardMethod;
    }

    public int getRewardTopX() {
        return rewardTopX;
    }

    private void load(ConfigurationSection section) {
        if (section == null) return;

        // Load Award Method
        String awardMethodStr = section.getString("award-method", "CHEST");
        this.awardMethod = Treasure.AwardMethod.fromString(awardMethodStr);
        if (this.awardMethod == Treasure.AwardMethod.TOP_X) {
            String x = awardMethodStr.replace("TOP_", "");
            try {
                this.rewardTopX = Integer.parseInt(x);
            } catch (NumberFormatException e) {
                this.rewardTopX = 3;
            }
        }

        // Load Debuff
        ConfigurationSection debuffSection = section.getConfigurationSection("debuff");
        if (debuffSection != null) {
            debuff = new TreasureDebuff(this);
            debuff.setShockwave(debuffSection.getBoolean("shockwave", true));
            debuff.setClicksToDebuff(debuffSection.getInt("clicks-to-debuff", 10));
            debuff.setPotionEffects(TreasureData.deserializeEffectsWithDuration(debuffSection.getStringList("potion-effects")));

            if (plugin != null) {
                plugin.getLogger().log(Level.INFO, "Linked debuff for round: " + id + " (Clicks: " + debuff.getClicksToDebuff() + ")");
            }
        }

        // Load Mobs
        ConfigurationSection mobsSection = section.getConfigurationSection("mobs");
        if (mobsSection != null) {
            Set<String> mobKeys = mobsSection.getKeys(false);
            for (String mobId : mobKeys) {
                ConfigurationSection mobSection = mobsSection.getConfigurationSection(mobId);
                if (mobSection != null) {
                    TreasureKeeper mob = new TreasureKeeper(null); 
                    mob.setMobId(mobId);
                    mob.setMythicMob(mobSection.getBoolean("mythic-mobs", false));
                    mob.setKeeperIdentifier(mobSection.getString("entity-type", "ZOMBIE"));
                    mob.setCustomName(mobSection.getString("custom-name", "Treasure Keeper"));
                    mob.setRange(mobSection.getString("range", "5-10"));
                    mob.setAmount(TreasureData.getAmountFromRangeStatic(mobSection.getString("range", "5-10")));
                    mob.setChance(mobSection.getInt("chance", 100));
                    mob.setMenuItem(mobSection.getString("menu-item", "STONE"));
                    mob.setMaxHealth(mobSection.getInt("max-health", 20));
                    mob.setPotionEffects(TreasureData.deserializeEffects(mobSection.getStringList("potion-effects")));
                    mob.setAnimatedSpawn(true); 

                    TreasureKeeperDrops drops = new TreasureKeeperDrops(mobSection.getConfigurationSection("drops"));
                    mob.setDrops(drops);

                    TreasureKeeperEquipment equipment = new TreasureKeeperEquipment(mobSection.getConfigurationSection("equipment"));
                    mob.setEquipment(equipment);

                    treasureKeepers.add(mob);
                }
            }
        }

        // Load Item Rewards
        ConfigurationSection itemRewardsSection = section.getConfigurationSection("item-rewards");
        if (itemRewardsSection != null) {
            Set<String> rewardKeys = itemRewardsSection.getKeys(false);
            for (String rewardId : rewardKeys) {
                ConfigurationSection rewardSection = itemRewardsSection.getConfigurationSection(rewardId);
                if (rewardSection != null) {
                    ItemReward reward = new ItemReward();
                    reward.setIdentifier(rewardId);
                    reward.setItemString(rewardSection.getString("item", "STONE"));
                    reward.setName(rewardSection.getString("name", "&6&l&oREWARD #1"));
                    reward.setLore(rewardSection.getStringList("lore"));
                    reward.setRange(rewardSection.getString("amount", "5-10"));
                    reward.setAmount(TreasureData.getAmountFromRangeStatic(rewardSection.getString("amount", "5-10")));
                    reward.setChance(rewardSection.getInt("chance", 40));
                    reward.setEnchants(rewardSection.getStringList("enchantments"));
                    reward.setMenuItem(rewardSection.getString("menu-item", "STONE"));
                    reward.setRewardToTopX(rewardSection.getInt("award-to-top", 0));
                    reward.build();
                    itemRewards.add(reward);
                }
            }
        }

        // Load Command Rewards
        ConfigurationSection commandRewardsSection = section.getConfigurationSection("command-rewards");
        if (commandRewardsSection != null) {
            Set<String> commandKeys = commandRewardsSection.getKeys(false);
            for (String commandId : commandKeys) {
                ConfigurationSection commandSection = commandRewardsSection.getConfigurationSection(commandId);
                if (commandSection != null) {
                    CommandReward cr = new CommandReward();
                    cr.setIdentifier(commandId);
                    cr.setCommand(commandSection.getString("command"));
                    cr.setChance(commandSection.getInt("chance"));
                    cr.setMenuItem(commandSection.getString("menu-item", "STONE"));
                    commandRewards.add(cr);
                }
            }
        }

        if (treasureKeepers.isEmpty() && itemRewards.isEmpty() && commandRewards.isEmpty() && debuff == null) {
            if (plugin != null) {
                plugin.getLogger().warning("Round '" + id + "' is EMPTY! No mobs, rewards or debuffs configured.");
            }
        }
    }
}

