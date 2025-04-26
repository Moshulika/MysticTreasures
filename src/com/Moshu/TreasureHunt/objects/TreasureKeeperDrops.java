package com.Moshu.TreasureHunt.objects;

import com.Moshu.Misc.Utils;
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
                    Material item = Material.matchMaterial(itemStr);

                    if(item == null)
                    {
                        plugin.getLogger().warning("Material '" + itemStr + "' does not exist!");
                        item = Material.STONE;
                    }

                    String range = dropConfig.getString("amount", "1-5");
                    int chance = dropConfig.getInt("chance", 100);

                    String name = dropConfig.getString("name", Utils.setCapitals(item.name().toLowerCase()).replace("_", " "));
                    List<String> lore = dropConfig.getStringList("lore");

                    List<String> enchantments = dropConfig.getStringList("enchantments");

                    DropData data = new DropData(item, getAmountFromRange(range), chance, name, lore, enchantments);
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

        private String name;
        private List<String> lore;
        private List<String> enchantments;

        public DropData(Material item, int amount, int chance, String name, List<String> lore, List<String> enchantments) {
            this.item = item;
            this.amount = amount;
            this.chance = chance;
            this.name = name;
            this.lore = lore;
            this.enchantments = enchantments;
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

        public ItemStack getItemStack()
        {

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
