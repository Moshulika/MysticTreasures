package com.Moshu.TreasureHunt.Components.Rewards;

import com.Moshu.Misc.Utils;
import com.nexomc.nexo.api.NexoItems;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.manager.TypeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an item reward that can be given to players during treasure hunts.
 * This class manages the configuration and distribution of item rewards,
 * supporting various item types including vanilla items, custom items from
 * ItemsAdder, Oraxen, Nexo, and MMOItems.
 * 
 * @author Moshu
 * @version 1.0
 */
public class ItemReward {

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private String identifier; // e.g., "diamond" or "emerald"
    private Material item;
    String itemStr;
    private String name;
    private List<String> lore;
    private int amount; // stored as a String (e.g., "1-10") unless parsed further
    private int chance;
    private List<String> enchants;
    private String range;
    private String menuItem;
    private int rewardToTopX;

    // Getters and setters
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getItemId() {
        return itemStr;
    }

    public void setItemString(String item) {
        this.itemStr = item;
    }

    public int getRewardToTopX()
    {
        return rewardToTopX;
    }

    public void setRewardToTopX(int rewardToTopX)
    {
        this.rewardToTopX = rewardToTopX;
    }

    public boolean isTopX(int currentTop)
    {
        return currentTop == rewardToTopX;
    }

    public boolean shouldGiveOnlyToTopX()
    {
        return rewardToTopX > 0;
    }

    public String getName() {
        return name;
    }

    public String getSanitizedName() {
        return org.bukkit.ChatColor.stripColor(name);
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
    public void setEnchants(List<String> enchants) { this.enchants = enchants; }
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
    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
    public String getMenuItem() { return menuItem; }
    public void setMenuItem(String menuItem) { this.menuItem = menuItem; }

    public boolean isMMOItem()
    {

        if(Utils.isEnabled("MMOItems"))
        {

            //type:id
            String type = getItemId().split(":")[0];
            TypeManager types = MMOItems.plugin.getTypes();
            return types.has(type);

        }

        return false;

    }

    public boolean isOraxen()
    {
        if(Utils.isEnabled("Oraxen"))
        {
            return OraxenItems.exists(getItemId());
        }

        return false;
    }

    public boolean isNexo()
    {

        if(Utils.isEnabled("Nexo"))
        {
            return NexoItems.exists(getItemId());
        }

        return false;

    }

    public boolean isItemsAdder()
    {

        if(Utils.isEnabled("ItemsAdder"))
        {
            return CustomStack.isInRegistry(getItemId());
        }

        return false;

    }

    public ItemReward build()
    {

        if(isItemsAdder() || isOraxen() || isNexo()) return this;

        item = Material.matchMaterial(itemStr);

        if(item == null)
        {
            plugin.getLogger().warning("Material '" + itemStr + "' does not exist!");
            item = Material.STONE;
        }

        return this;
    }

    /**
     *
     * Fetches the ItemReward object from the config
     * and integrates with custom item plugins to
     * retrieve the final custom ItemStack.
     *
     * @return the Bukkit ItemStack for this ItemReward
     */
    public ItemStack getItemStack()
    {

        if(isItemsAdder())
        {
            CustomStack stack = CustomStack.getInstance(getItemId());

            if(stack != null)
            {
                ItemStack is = stack.getItemStack();
                is.setAmount(amount);
                return is;
            }
            else
            {
                plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                return new ItemStack(Material.STONE);
            }

        }

        if(isOraxen())
        {
            ItemStack stack = OraxenItems.getItemById(getItemId()).build();

            if(stack != null)
            {
                stack.setAmount(getAmount());
                return stack;
            }
            else
            {
                plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                return new ItemStack(Material.STONE);
            }
        }

        if(isNexo())
        {
            ItemStack stack = NexoItems.itemFromId(getItemId()).build();

            if(stack != null)
            {
                stack.setAmount(getAmount());
                return stack;
            }
            else
            {
                plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                return new ItemStack(Material.STONE);
            }
        }

        if(isMMOItem())
        {

            if(getItemId().split(":").length != 2)
            {
                plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                return new ItemStack(Material.STONE);
            }

            String type = getItemId().split(":")[0];
            String id = getItemId().split(":")[1];

            MMOItem mmoitem = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(type), id);

            if(mmoitem == null)
            {
                plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                return new ItemStack(Material.STONE);
            }

            return mmoitem.newBuilder().build();

        }

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
        return Utils.addUnsafeEnchants(itemStack, enchants);

    }

    public ItemReward()
    {}

}
