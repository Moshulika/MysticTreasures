package com.Moshu.TreasureHunt.objects;

import com.Moshu.Misc.Utils;
import com.nexomc.nexo.api.NexoItems;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.manager.TypeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreasureKeeperDrops {

    private Map<String, DropData> dropsMap = new HashMap<>();
    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

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
                    plugin.getLogger().warning("Configuration section '" + key + "' does not exist!");
                }
            }
        }
        else
        {
            plugin.getLogger().warning("Configuration section 'drops' does not exist!");
        }
    }

    private int getAmountFromRange(String s)
    {

        String[] arr = s.split("-");

        for(String x : arr)
        {
            if(!Utils.isInt(x))
            {
                plugin.getLogger().warning("Invalid amount of item-reward: " + x);
            }
        }

        if(arr.length == 1) return Integer.parseInt(arr[0]);
        else return Utils.randInt(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));

    }

    public DropData getDrop(String key) {
        return dropsMap.get(key);
    }

    public Map<String, DropData> getAllDrops() {
        return dropsMap;
    }

    public static class DropData {
        private Material item;
        private int amount;
        private int chance;
        private String itemStr;

        private String name;
        private List<String> lore;
        private List<String> enchantments;

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
                    plugin.getLogger().warning("Material '" + itemStr + "' does not exist!");
                    item = Material.STONE;
                }
            }

        }

        public String getName()
        {
            return name;
        }

        public List<String> getEnchantments()
        {
            return enchantments;
        }

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

            if(!name.equals("none"))
            {
                itemMeta.setDisplayName(Utils.format(name));
                itemMeta.setLore(Utils.formatList(lore));
                itemStack.setItemMeta(itemMeta);
            }

            return Utils.addUnsafeEnchants(itemStack, enchantments);
        }

    }

}
