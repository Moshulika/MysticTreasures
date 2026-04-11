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

import com.Moshu.Misc.Utils;
import com.nexomc.nexo.api.NexoItems;
import dev.lone.itemsadder.api.CustomStack;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.manager.TypeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a treasure key that can be used to unlock treasures.
 * This class manages the configuration and validation of treasure keys,
 * supporting various item types including vanilla items, custom items from
 * ItemsAdder, Oraxen, Nexo, and MMOItems.
 *
 * @author Moshu
 * @version 1.0
 */
public class TreasureKey {

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private boolean enabled;
    private Material item;
    private String name;
    private List<String> lore;
    String itemStr;

    /**
     * Creates a new treasure key with the specified configuration.
     *
     * @param enabled Whether the key requirement is enabled
     * @param itemStr The item identifier string
     * @param name    The display name of the key
     * @param lore    The lore text for the key
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public TreasureKey(boolean enabled, String itemStr, String name, List<String> lore) {

        this.enabled = enabled;
        this.itemStr = itemStr;
        this.name = name;
        this.lore = lore;

        if (!isNexo() && !isItemsAdder() && !isOraxen()) {

            item = Material.matchMaterial(itemStr);

            if (item == null) {
                plugin.getLogger().warning("Material '" + itemStr + "' does not exist!");
                item = Material.STONE;
            }

        }

    }

    // Getters and setters

    /**
     * Checks if a key is required to open the treasure.
     *
     * @return True if a key is required, false otherwise
     */
    public boolean requiresKey() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Material getItem() {
        return item == null ? Material.STONE : item;
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

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<String> getLore() {
        return lore;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public String getItemId() {
        return itemStr;
    }

    public void setItemString(String item) {
        this.itemStr = item;
    }

    /**
     * Checks if this key is an MMOItems item.
     *
     * @return True if it's an MMOItems item, false otherwise
     */
    public boolean isMMOItem() {

        if (Utils.isEnabled("MMOItems")) {

            //type:id
            String type = getItemId().split(":")[0];
            TypeManager types = MMOItems.plugin.getTypes();
            return types.has(type);

        }

        return false;

    }

    private boolean isOraxen() {
        if (Utils.isEnabled("Oraxen")) {
            return OraxenItems.exists(getItemId());
        }

        return false;
    }

    private boolean isOraxen(ItemStack is) {
        if (Utils.isEnabled("Oraxen")) {
            return OraxenItems.exists(is);
        }

        return false;
    }

    private boolean isNexo() {

        if (Utils.isEnabled("Nexo")) {
            return NexoItems.exists(getItemId());
        }

        return false;

    }

    private boolean isNexo(ItemStack is) {
        if (Utils.isEnabled("Nexo")) {
            return NexoItems.exists(is);
        }

        return false;
    }

    private boolean isItemsAdder() {

        if (Utils.isEnabled("ItemsAdder")) {
            return CustomStack.isInRegistry(getItemId());
        }

        return false;

    }

    private boolean isItemsAdder(ItemStack is) {

        if (Utils.isEnabled("ItemsAdder")) {
            return CustomStack.byItemStack(is) != null;
        }

        return false;

    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public ItemStack getItemStack(int amount) {

        if (isItemsAdder()) {
            CustomStack stack = CustomStack.getInstance(getItemId());

            if (stack != null) {
                ItemStack is = stack.getItemStack();
                is.setAmount(amount);
                return is;
            } else {
                plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                return new ItemStack(Material.STONE);
            }

        }

        if (isOraxen()) {
            ItemBuilder builder = OraxenItems.getItemById(getItemId());
            ItemStack stack = builder != null ? builder.build() : null;

            if (stack != null) {
                stack.setAmount(amount);
                return stack;
            } else {
                plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                return new ItemStack(Material.STONE);
            }
        }

        if (isNexo()) {
            com.nexomc.nexo.items.ItemBuilder builder = NexoItems.itemFromId(getItemId());
            ItemStack stack = builder != null ? builder.build() : null;

            if (stack != null) {
                stack.setAmount(amount);
                return stack;
            } else {
                plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                return new ItemStack(Material.STONE);
            }
        }

        if (isMMOItem()) {

            if (getItemId().split(":").length != 2) {
                plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                return new ItemStack(Material.STONE);
            }

            String type = getItemId().split(":")[0];
            String id = getItemId().split(":")[1];

            MMOItem mmoitem = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(type), id);

            if (mmoitem == null) {
                plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                return new ItemStack(Material.STONE);
            }

            return mmoitem.newBuilder().build();

        }

        ItemStack is = new ItemStack(getItem(), amount);
        ItemMeta im = is.getItemMeta();

        ArrayList<String> coloredLore = new ArrayList<String>();

        for (String s : lore) {
            coloredLore.add(Utils.format(s));
        }

        im.setLore(coloredLore);
        im.setDisplayName(Utils.format(name));

        is.setItemMeta(im);
        return is;
    }

    public boolean isTreasureKey(ItemStack apparentKey) {

        if (apparentKey == null || apparentKey.getType() == Material.AIR) return false;

        if (isOraxen(apparentKey)) {

            String apparentKeyId = OraxenItems.getIdByItem(apparentKey);
            if (apparentKeyId == null) return false;
            return apparentKeyId.equals(getItemId());

        }

        if (isItemsAdder(apparentKey)) {

            CustomStack stack = CustomStack.getInstance(getItemId());
            if (stack == null) return false;
            return stack.getNamespacedID().equals(getItemId());

        }

        if (isNexo(apparentKey)) {
            if (NexoItems.idFromItem(apparentKey) == null) return false;
            return NexoItems.idFromItem(apparentKey).equals(getItemId());
        }

        ItemStack realKey = getItemStack(1);

        if (realKey.getType() == apparentKey.getType()) {

            ItemMeta apparentMeta = apparentKey.getItemMeta();
            ItemMeta realMeta = realKey.getItemMeta();

            if (apparentMeta == null || realMeta == null) {
                return apparentMeta == null && realMeta == null;
            }

            String apparentName = apparentMeta.getDisplayName();
            String realName = realMeta.getDisplayName();

            if (realName != null && realName.equals(apparentName)) {

                List<String> lore_real = realMeta.getLore();
                List<String> lore_apparent = apparentMeta.getLore();

                if (lore_real == null || lore_apparent == null) {
                    return lore_real == null && lore_apparent == null;
                }

                return new HashSet<>(lore_apparent).containsAll(lore_real);

            }

        }

        return false;


    }

    public static boolean isKey(ItemStack apparentKey) {
        if (apparentKey == null || apparentKey.getType() == Material.AIR) {
            return false;
        }

        // Guard against uninitialized treasure data in unit tests / early startup
        java.util.List<TreasureData> dataList = TreasureData.getTreasureData();
        if (dataList == null || dataList.isEmpty()) {
            return false;
        }

        for (TreasureData d : dataList) {
            if (d.getTreasureKey() != null && d.getTreasureKey().isTreasureKey(apparentKey)) return true;
        }

        return false;
    }

}

