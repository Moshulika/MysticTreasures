package com.Moshu.TreasureHunt.objects;

import com.Moshu.Misc.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.mozilla.javascript.ast.TryStatement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreasureKeeperEquipment {

    private Map<String, EquipmentData> equipmentMap = new HashMap<>();
    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    /**
     * TODO: Handle null equipmentSection well
     * @param equipmentSection
     */
    public TreasureKeeperEquipment(ConfigurationSection equipmentSection) {

        if (equipmentSection != null) {

            for (String key : equipmentSection.getKeys(false)) {

                ConfigurationSection equipConfig = equipmentSection.getConfigurationSection(key);

                if (equipConfig != null) {

                    String itemStr = equipConfig.getString("item", "STONE");

                    if(itemStr.equals("none")) continue;

                    Material item =  Material.matchMaterial(itemStr);

                    if(item == null)
                    {
                        plugin.getLogger().warning("Material '" + itemStr + "' does not exist!");
                        item = Material.STONE;
                    }

                    String slot = equipConfig.getString("slot", "HAND");

                    if(!validSlot(slot))
                    {
                        slot = "HAND";
                        plugin.getLogger().warning("Equipment slot '" + slot + "' does not exist!");
                    }

                    EquipmentSlot eSlot = matchEquipmentSlot(slot);

                    if(!isValidEquipment(item, eSlot))
                    {
                        item = getDefaultForSlot(slot);
                    }

                    List<String> enchantments = equipConfig.getStringList("enchantments");

                    EquipmentData data = new EquipmentData(item, eSlot, enchantments);
                    equipmentMap.put(key, data);

                }
                else
                {
                    plugin.getLogger().warning("Configuration section '" + key + "' does not exist!");
                }
            }

        }
        else
        {
            plugin.getLogger().warning("Configuration section 'equipment' does not exist!");
        }
    }

    private boolean validSlot(String str)
    {

        return str.equalsIgnoreCase("HEAD") || str.equalsIgnoreCase("CHEST")
                || str.equalsIgnoreCase("LEGS") || str.equalsIgnoreCase("FEET");

    }

    private Material getDefaultForSlot(String slot)
    {

        if(slot.equalsIgnoreCase("HEAD"))
        {
            return Material.IRON_HELMET;
        }
        else if(slot.equalsIgnoreCase("BODY"))
        {
            return Material.IRON_CHESTPLATE;
        }
        else if(slot.equalsIgnoreCase("LEGS"))
        {
            return Material.IRON_LEGGINGS;
        }
        else if(slot.equalsIgnoreCase("FEET"))
        {
            return Material.IRON_BOOTS;
        }
        else if(slot.equalsIgnoreCase("HAND"))
        {
            return Material.IRON_SWORD;
        }
        else if(slot.equalsIgnoreCase("OFF_HAND"))
        {
            return Material.IRON_SWORD;
        }
        else return null;

    }

    private EquipmentSlot matchEquipmentSlot(String slot)
    {

        try
        {
            return EquipmentSlot.valueOf(slot);
        }
        catch (IllegalArgumentException e)
        {
            plugin.getLogger().warning("Equipment slot " + slot + " does not exist!");
            return EquipmentSlot.HAND;
        }

    }

    private boolean isValidEquipment(Material material, EquipmentSlot slot) {

        if (material == null || slot == null) {
            return false;
        }

        switch (slot) {
            case HEAD:
                return isHelmet(material);
            case CHEST:
                return isChestplate(material);
            case LEGS:
                return isLeggings(material);
            case FEET:
                return isBoots(material);
            case HAND:
            case OFF_HAND:
                return isWeaponOrTool(material);
            default:
                return false;
        }
    }

    private boolean isHelmet(Material material) {
        return material.name().endsWith("_HELMET") || material == Material.CARVED_PUMPKIN
                || material == Material.TURTLE_HELMET;
    }

    private boolean isChestplate(Material material) {
        return material.name().endsWith("_CHESTPLATE") || material == Material.ELYTRA;
    }

    private boolean isLeggings(Material material) {
        return material.name().endsWith("_LEGGINGS");
    }

    private boolean isBoots(Material material) {
        return material.name().endsWith("_BOOTS");
    }

    private boolean isWeaponOrTool(Material material) {
        return material.name().endsWith("_SWORD") || material.name().endsWith("_AXE")
                || material.name().endsWith("_PICKAXE") || material.name().endsWith("_SHOVEL")
                || material.name().endsWith("_HOE") || material == Material.BOW
                || material == Material.CROSSBOW || material == Material.TRIDENT;
    }

    public EquipmentData getEquipment(String key) {
        return equipmentMap.get(key);
    }

    public Map<String, EquipmentData> getAllEquipment() {
        return equipmentMap;
    }

    public static class EquipmentData {
        private Material item;
        private EquipmentSlot slot;
        private List<String> enchantments;

        public EquipmentData(Material item, EquipmentSlot slot, List<String> enchantments) {
            this.item = item;
            this.slot = slot;
            this.enchantments = enchantments;
        }

        public Material getItem() {
            return item;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }

        public List<String> getEnchantments() {
            return enchantments;
        }

        public ItemStack getItemStack()
        {

            ItemStack itemStack = new ItemStack(item);
            String[] args;
            String enchantment;
            Enchantment enchant;
            int level;

            for(String s : enchantments)
            {

                args = s.split(":");
                enchantment = args[0];

                if(!Utils.isInt(args[1]))
                {
                    plugin.getLogger().warning("Enchantment level needs to be a number! Affected enchantment: " +  s);
                    continue;
                }

                level = Integer.parseInt(args[1]);
                enchant = Enchantment.getByName(enchantment);

                if(enchant == null)
                {
                    plugin.getLogger().warning("Enchantment is invalid! Affected enchantment: " +  s);
                    continue;
                }

                if(!enchant.canEnchantItem(itemStack))
                {
                    plugin.getLogger().warning("Enchantment " + enchantment + " can not be used on " + item.toString());
                    continue;
                }

                if(level > enchant.getMaxLevel())
                {
                    plugin.getLogger().warning("Max level for enchantment " + enchantment + " is " + level);
                    itemStack.addEnchantment(enchant, enchant.getMaxLevel());
                }
                else
                {
                    itemStack.addEnchantment(enchant, level);
                }


            }

            return itemStack;

        }

    }

}
