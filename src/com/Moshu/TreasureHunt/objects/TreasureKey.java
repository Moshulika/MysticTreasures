package com.Moshu.TreasureHunt.objects;

import com.Moshu.Misc.Utils;
import com.nexomc.nexo.api.NexoItems;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
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
import java.util.HashSet;
import java.util.List;
//TODO: Add Oraxen, ItemsAdder support
public class TreasureKey {

    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private boolean enabled;
    private Material item;
    private String name;
    private List<String> lore;
    String itemStr;

    public TreasureKey(boolean enabled, String itemStr, String name, List<String> lore) {

        this.enabled = enabled;
        this.itemStr = itemStr;
        this.name = name;
        this.lore = lore;

        if(!isNexo() && !isItemsAdder() && !isOraxen())
        {

            item = Material.matchMaterial(itemStr);

            if (item == null) {
                plugin.getLogger().warning("Material '" + itemStr + "' does not exist!");
                item = Material.STONE;
            }

        }

    }

    // Getters and setters
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
    public List<String> getLore() {
        return lore;
    }
    public void setLore(List<String> lore) {
        this.lore = lore;
    }
    public String getItemId() {
        return itemStr;
    }

    public void setItemString(String item) {
        this.itemStr = item;
    }

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

    private boolean isOraxen()
    {
        if(Utils.isEnabled("Oraxen"))
        {
            return OraxenItems.exists(getItemId());
        }

        return false;
    }

    private boolean isOraxen(ItemStack is)
    {
        if(Utils.isEnabled("Oraxen")) {
            return OraxenItems.exists(is);
        }

        return false;
    }

    private boolean isNexo()
    {

        if(Utils.isEnabled("Nexo"))
        {
            return NexoItems.exists(getItemId());
        }

        return false;

    }

    private boolean isNexo(ItemStack is)
    {
        if(Utils.isEnabled("Nexo"))
        {
            return NexoItems.exists(is);
        }

        return false;
    }

    private boolean isItemsAdder()
    {

        if(Utils.isEnabled("ItemsAdder"))
        {
            return CustomStack.isInRegistry(getItemId());
        }

        return false;

    }

    private boolean isItemsAdder(ItemStack is)
    {

        if(Utils.isEnabled("ItemsAdder"))
        {
            return CustomStack.byItemStack(is) != null;
        }

        return false;

    }

    public ItemStack getItemStack(int amount)
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
                stack.setAmount(amount);
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
                stack.setAmount(amount);
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

        if(isOraxen(apparentKey))
        {

            String apparentKeyId = OraxenItems.getIdByItem(apparentKey);
            if(apparentKeyId == null) return false;
            return apparentKeyId.equals(getItemId());

        }

        if(isItemsAdder(apparentKey))
        {

            CustomStack stack = CustomStack.getInstance(getItemId());
            if(stack == null) return false;
            return stack.getNamespacedID().equals(getItemId());

        }

        if(isNexo(apparentKey))
        {
            if(NexoItems.idFromItem(apparentKey) == null) return false;
            return NexoItems.idFromItem(apparentKey).equals(getItemId());
        }

        ItemStack realKey = getItemStack(1);

        if(realKey.getType() == apparentKey.getType())
        {

            if(apparentKey.getItemMeta() == null) return false;
            if(apparentKey.getItemMeta() == null && realKey.getItemMeta() == null) return true;

            if(realKey.getItemMeta().getDisplayName().equals(apparentKey.getItemMeta().getDisplayName()))
            {

                List<String> lore_real = realKey.getItemMeta().getLore();
                List<String> lore_apparent =  apparentKey.getItemMeta().getLore();

                if(new HashSet<>(lore_apparent).containsAll(lore_real)) return true;

            }

        }

        return false;


    }

    public static boolean isKey(ItemStack apparentKey)
    {

        for(TreasureData d : TreasureData.getTreasureData())
        {
            if(d.getTreasureKey().isTreasureKey(apparentKey)) return true;
        }

        return false;
    }

}
