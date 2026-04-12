/*
 * Copyright 2026 Moshu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This software is also subject to the Commons Clause License Condition v1.0.
 * You may obtain a copy of the Commons Clause at:
 * https://commonsclause.com/
 */

package com.Moshu.TreasureHunt.Components.Keepers;

import com.Moshu.Misc.Utils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreasureKeeperEquipment {

    private final Map<String, EquipmentData> equipmentMap = new HashMap<>();
    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    /**
     * TODO: Handle null equipmentSection well
     *
     * @param equipmentSection
     */
    public TreasureKeeperEquipment(ConfigurationSection equipmentSection) {

        if (equipmentSection != null) {

            for (String key : equipmentSection.getKeys(false)) {

                if (equipmentSection.isConfigurationSection(key)) {
                    ConfigurationSection equipConfig = equipmentSection.getConfigurationSection(key);

                    if (equipConfig != null) {

                        String itemStr = equipConfig.getString("item", "STONE");
                        if (itemStr == null || itemStr.equals("none")) continue;

                        Material item = Material.matchMaterial(itemStr);

                        if (item == null) {
                            if (plugin != null) plugin.getLogger().warning("Material '" + itemStr + "' does not exist!");
                            item = Material.STONE;
                        }

                        String slot = equipConfig.getString("slot", "HAND");
                        if (slot == null) slot = "HAND";

                        if (!validSlot(slot)) {
                            if (plugin != null) plugin.getLogger().warning("Equipment slot '" + slot + "' does not exist!");
                            continue;
                        }

                        EquipmentSlot eSlot = matchEquipmentSlot(slot);

                        if (!isValidEquipment(item, eSlot)) {
                            if (plugin != null)
                                plugin.getLogger().warning("Invalid item for slot: " + item.name() + " in " + eSlot.name());
                            item = getDefaultForSlot(slot);
                        }

                        List<String> enchantments = equipConfig.getStringList("enchantments");

                        if (item != null) {
                            EquipmentData data = new EquipmentData(item, eSlot, enchantments);
                            equipmentMap.put(key, data);
                        }
                    }
                } else if (equipmentSection.isString(key)) {
                    String itemStr = equipmentSection.getString(key);
                    if (itemStr == null || itemStr.equalsIgnoreCase("none")) continue;

                    Material item = Material.matchMaterial(itemStr);
                    if (item == null) {
                        if (plugin != null) plugin.getLogger().warning("Material '" + itemStr + "' does not exist for slot '" + key + "'!");
                        continue;
                    }

                    EquipmentSlot eSlot = matchEquipmentSlot(key);
                    if (eSlot == null) {
                         if (plugin != null) plugin.getLogger().warning("Unknown equipment slot: " + key);
                         continue;
                    }

                    EquipmentData data = new EquipmentData(item, eSlot, null);
                    equipmentMap.put(key, data);
                }
            }

        } else {
            if (plugin != null) plugin.getLogger().warning("Configuration section 'equipment' does not exist!");
        }
    }

    private boolean validSlot(String str) {

        return str.equalsIgnoreCase("HEAD") || str.equalsIgnoreCase("CHEST")
                || str.equalsIgnoreCase("LEGS") || str.equalsIgnoreCase("FEET")
                || str.equalsIgnoreCase("HAND") || str.equalsIgnoreCase("OFF_HAND")
                || str.equalsIgnoreCase("HELMET") || str.equalsIgnoreCase("CHESTPLATE")
                || str.equalsIgnoreCase("LEGGINGS") || str.equalsIgnoreCase("BOOTS")
                || str.equalsIgnoreCase("MAIN-HAND") || str.equalsIgnoreCase("OFF-HAND");

    }

    private Material getDefaultForSlot(String slot) {

        if (slot.equalsIgnoreCase("HEAD") || slot.equalsIgnoreCase("HELMET")) {
            return Material.IRON_HELMET;
        } else if (slot.equalsIgnoreCase("CHEST") || slot.equalsIgnoreCase("CHESTPLATE")) {
            return Material.IRON_CHESTPLATE;
        } else if (slot.equalsIgnoreCase("LEGS") || slot.equalsIgnoreCase("LEGGINGS")) {
            return Material.IRON_LEGGINGS;
        } else if (slot.equalsIgnoreCase("FEET") || slot.equalsIgnoreCase("BOOTS")) {
            return Material.IRON_BOOTS;
        } else if (slot.equalsIgnoreCase("HAND") || slot.equalsIgnoreCase("MAIN-HAND")) {
            return Material.IRON_SWORD;
        } else if (slot.equalsIgnoreCase("OFF_HAND") || slot.equalsIgnoreCase("OFF-HAND")) {
            return Material.IRON_SWORD;
        } else return Material.STONE;

    }

    private EquipmentSlot matchEquipmentSlot(String slot) {

        if (slot.equalsIgnoreCase("HELMET")) slot = "HEAD";
        if (slot.equalsIgnoreCase("CHESTPLATE")) slot = "CHEST";
        if (slot.equalsIgnoreCase("LEGGINGS")) slot = "LEGS";
        if (slot.equalsIgnoreCase("BOOTS")) slot = "FEET";
        if (slot.equalsIgnoreCase("MAIN-HAND")) slot = "HAND";
        if (slot.equalsIgnoreCase("OFF-HAND")) slot = "OFF_HAND";

        try {
            return EquipmentSlot.valueOf(slot.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Check for additional mappings if needed
            if (slot.equalsIgnoreCase("HEAD")) return EquipmentSlot.HEAD;
            if (slot.equalsIgnoreCase("CHEST")) return EquipmentSlot.CHEST;
            if (slot.equalsIgnoreCase("LEGS")) return EquipmentSlot.LEGS;
            if (slot.equalsIgnoreCase("FEET")) return EquipmentSlot.FEET;
            if (slot.equalsIgnoreCase("HAND")) return EquipmentSlot.HAND;
            if (slot.equalsIgnoreCase("OFF_HAND")) return EquipmentSlot.OFF_HAND;
            
            if (plugin != null) plugin.getLogger().warning("Equipment slot " + slot + " does not exist!");
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

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Map<String, EquipmentData> getAllEquipment() {
        return equipmentMap;
    }

    public static class EquipmentData {
        private final Material item;
        private final EquipmentSlot slot;
        private final List<String> enchantments;

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

        @SuppressFBWarnings("EI_EXPOSE_REP")
        public List<String> getEnchantments() {
            return enchantments;
        }

        public ItemStack getItemStack() {

            ItemStack itemStack = new ItemStack(item);
            return Utils.addEnchants(itemStack, enchantments);

        }

    }

}

