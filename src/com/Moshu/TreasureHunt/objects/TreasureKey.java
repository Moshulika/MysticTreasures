package com.Moshu.TreasureHunt.objects;

import com.Moshu.Misc.Utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
//TODO: Add Oraxen, ItemsAdder support
public class TreasureKey {

    private boolean enabled;
    private Material item;
    private String name;
    private List<String> lore;

    public TreasureKey(boolean enabled, Material item, String name, List<String> lore) {
        this.enabled = enabled;
        this.item = item;
        this.name = name;
        this.lore = lore;
    }

    // Getters and setters
    public boolean requiresKey() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public ItemStack getItemStack(int amount)
    {

        ItemStack is = new ItemStack(getItem(), amount);
        ItemMeta im = is.getItemMeta();

        ArrayList<String> coloredLore = new ArrayList<String>();

        for(String s : lore)
        {
            coloredLore.add(Utils.format(s));
        }

        im.setLore(coloredLore);
        im.setDisplayName(Utils.format(name));

        is.setItemMeta(im);
        return is;
    }

    public boolean isTreasureKey(ItemStack apparentKey)
    {

        ItemStack realKey = getItemStack(1);

        if(realKey.getType() == apparentKey.getType())
        {

            if(apparentKey.getItemMeta() == null) return false;
            if(apparentKey.getItemMeta() == null && realKey.getItemMeta() == null) return true;

            if(realKey.getItemMeta().getDisplayName().equals(apparentKey.getItemMeta().getDisplayName()))
            {

                List<String> lore_real = realKey.getItemMeta().getLore();
                List<String> lore_apparent =  apparentKey.getItemMeta().getLore();

                if(lore_apparent.containsAll(lore_real)) return true;

            }

        }

        return false;


    }

}
