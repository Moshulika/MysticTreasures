package com.Moshu.TreasureHunt.Components.Keepers;

import com.Moshu.Misc.Utils;
import com.nexomc.nexo.api.NexoItems;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.manager.TypeManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreasureKeeperDrops {

    private final Map<String, DropData> dropsMap = new HashMap<>();
    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public TreasureKeeperDrops(ConfigurationSection dropsSection) {

        if (dropsSection != null) {

            for (String key : dropsSection.getKeys(false)) {

                ConfigurationSection dropConfig = dropsSection.getConfigurationSection(key);

                if (dropConfig != null) {

                    String itemStr = dropConfig.getString("item", "DIAMOND");

                    String range = dropConfig.getString("amount", "1-5");
                    int chance = dropConfig.getInt("chance", 100);

                    String name = dropConfig.getString("name", Utils.setCapitals(itemStr.toLowerCase()).replace("_", " ")
                            .replace(":", " "));
                    List<String> lore = dropConfig.getStringList("lore");

                    List<String> enchantments = dropConfig.getStringList("enchantments");

                    DropData data = new DropData(itemStr, getAmountFromRange(range), chance, name, lore, enchantments);
                    dropsMap.put(key, data);

                }
                else
                {
                    if (plugin != null) plugin.getLogger().warning("Configuration section '" + key + "' does not exist!");
                }
            }
        }
        else
        {
            if (plugin != null) plugin.getLogger().warning("Configuration section 'drops' does not exist!");
        }
    }

    private int getAmountFromRange(String s)
    {
        if (s == null) return 1;

        String[] arr = s.split("-");

        for(String x : arr)
        {
            if(!Utils.isInt(x))
            {
                if (plugin != null) plugin.getLogger().warning("Invalid amount of item-reward: " + x);
            }
        }

        try {
            if(arr.length == 1) return Integer.parseInt(arr[0]);
            else return Utils.randInt(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return 1;
        }

    }

    public DropData getDrop(String key) {
        return dropsMap.get(key);
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Map<String, DropData> getAllDrops() {
        return dropsMap;
    }

    public static class DropData {
        private Material item;
        private final int amount;
        private final int chance;
        private String itemStr;

        private final String name;
        private final List<String> lore;
        private final List<String> enchantments;

        public DropData(String itemStr, int amount, int chance, String name, List<String> lore, List<String> enchantments) {

            this.itemStr = itemStr;
            this.amount = amount;
            this.chance = chance;
            this.name = name;
            this.lore = lore;
            this.enchantments = enchantments;

            if(!isNexo() && !isItemsAdder() && !isOraxen())
            {
                item = Material.matchMaterial(itemStr);

                if(item == null)
                {
                    if (plugin != null) plugin.getLogger().warning("Material '" + itemStr + "' does not exist!");
                    item = Material.STONE;
                }
            }

        }

        public String getName()
        {
            return name;
        }

        @SuppressFBWarnings("EI_EXPOSE_REP")
        public List<String> getEnchantments()
        {
            return enchantments;
        }

        @SuppressFBWarnings("EI_EXPOSE_REP")
        public List<String> getLore()
        {
            return lore;
        }

        public Material getItem() {
            return item;
        }

        public int getAmount() {
            return amount;
        }

        public int getChance() {
            return chance;
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
                String[] split = getItemId().split(":");
                if (split.length < 1) return false;
                String type = split[0];
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

        private boolean isNexo()
        {

            if(Utils.isEnabled("Nexo"))
            {
                return NexoItems.exists(getItemId());
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

        @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
        public ItemStack getItemStack()
        {

            if(isItemsAdder())
            {
                CustomStack stack = CustomStack.getInstance(getItemId());

                if(stack != null)
                {
                    return stack.getItemStack().asQuantity(getAmount());
                }
                else
                {
                    if (plugin != null) plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                    return new ItemStack(Material.STONE);
                }

            }

            if(isOraxen())
            {
                ItemBuilder builder = OraxenItems.getItemById(getItemId());
                ItemStack stack = builder != null ? builder.build() : null;

                if(stack != null)
                {
                    stack.setAmount(getAmount());
                    return stack;
                }
                else
                {
                    if (plugin != null) plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                    return new ItemStack(Material.STONE);
                }
            }

            if(isNexo())
            {
                com.nexomc.nexo.items.ItemBuilder builder = NexoItems.itemFromId(getItemId());
                ItemStack stack = builder != null ? builder.build() : null;

                if(stack != null)
                {
                    stack.setAmount(getAmount());
                    return stack;
                }
                else
                {
                    if (plugin != null) plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                    return new ItemStack(Material.STONE);
                }
            }

            if(isMMOItem())
            {

                String[] split = getItemId().split(":");
                if(split.length != 2)
                {
                    if (plugin != null) plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                    return new ItemStack(Material.STONE);
                }

                String type = split[0];
                String id = split[1];

                MMOItem mmoitem = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(type), id);

                if(mmoitem == null)
                {
                    if (plugin != null) plugin.getLogger().severe("Could not get ItemStack from this id: " + getItemId());
                    return new ItemStack(Material.STONE);
                }

                return mmoitem.newBuilder().build();

            }

            ItemStack itemStack = new ItemStack(item != null ? item : Material.STONE, amount);
            ItemMeta itemMeta = itemStack.getItemMeta();

            if(itemMeta != null && name != null && !name.equals("none"))
            {
                itemMeta.setDisplayName(Utils.format(name));
                itemMeta.setLore(Utils.formatList(lore));
                itemStack.setItemMeta(itemMeta);
            }

            return Utils.addUnsafeEnchants(itemStack, enchantments);
        }

    }

}
