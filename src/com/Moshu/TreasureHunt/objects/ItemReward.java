package com.Moshu.TreasureHunt.objects;

import com.Moshu.Misc.Utils;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class ItemReward {

    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private String identifier; // e.g., "diamond" or "emerald"
    private Material item;
    private String name;
    private List<String> lore;
    private int amount; // stored as a String (e.g., "1-10") unless parsed further
    private int chance;

    // Getters and setters
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public Material getItem() {
        return item;
    }
    public void setItem(Material item) {
        this.item = item;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<String> getLore() {
        return lore;
    }
    public void setLore(List<String> lore) {
        this.lore = lore;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public int getChance() {
        return chance;
    }
    public void setChance(int chance) {
        this.chance = chance;
    }

    public ItemStack getItemStack()
    {

        ItemStack itemStack = new ItemStack(item, amount);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if(!name.isEmpty())
        {
            itemMeta.setDisplayName(Utils.format(name));
        }
        if(!lore.isEmpty())
        {

            ArrayList<String> coloredLore = new ArrayList<>();

            for(String s: lore)
            {
                coloredLore.add(Utils.format(s));
            }

            itemMeta.setLore(coloredLore);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;

    }

    public ItemReward()
    {}

}
