package com.Moshu.TreasureHunt.Components.Rewards;

import com.Moshu.Misc.Utils;
import com.nexomc.nexo.api.NexoItems;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an obfuscated reward item used to hide the true identity of treasure rewards.
 * This class manages placeholder items that are shown to players instead of the actual
 * rewards until they interact with them.
 * 
 * Supports various item types including vanilla items, ItemsAdder, Oraxen, and Nexo items.
 * 
 * @author Moshu
 * @version 1.0
 */
public class ObfuscatedReward {

        private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

        private final String itemStr;
        private final String name;
        private final List<String> lore;

        /**
         * Creates a new obfuscated reward with the specified configuration.
         * 
         * @param itemStr The item identifier string
         * @param name The display name of the obfuscated item
         * @param lore The lore text for the obfuscated item
         */
        public ObfuscatedReward(String itemStr, String name, List<String> lore)
        {
            this.itemStr = itemStr;
            this.name = name;
            this.lore = lore;
        }

        public String getItemId()
        {
            return itemStr;
        }

        public String getName()
        {
            return name;
        }

        public List<String> getLore()
        {
            return lore;
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

        public ItemStack get()
        {

            if(isItemsAdder())
            {
                CustomStack stack = CustomStack.getInstance(getItemId());

                if(stack != null)
                {
                    return stack.getItemStack();
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
                    return stack;
                }
                else
                {
                    plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                    return new ItemStack(Material.STONE);
                }
            }

            Material item = Material.matchMaterial(itemStr);

            if(item == null)
            {
                plugin.getLogger().warning("Material '" + itemStr + "' does not exist!");
                item = Material.STONE;
            }

            ItemStack itemStack = new ItemStack(item);
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



}
