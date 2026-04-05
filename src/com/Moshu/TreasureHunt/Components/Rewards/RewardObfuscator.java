package com.Moshu.TreasureHunt.Components.Rewards;

import com.Moshu.Misc.Hooks.PacketEventsUtils;
import com.Moshu.Misc.Hooks.ProtocolLibUtils;
import com.Moshu.Misc.Storage.Messages;
import com.Moshu.Misc.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages the obfuscation of treasure rewards to hide their true identity from players.
 * This class provides functionality to replace actual reward items with placeholder items
 * until players interact with them, creating a surprise element in treasure hunts.
 * <p>
 * The obfuscation system supports both ProtocolLib and PacketEvents for packet manipulation
 * and can be configured through the plugin configuration.
 *
 * @author Moshu
 * @version 1.0
 */
public class RewardObfuscator implements Listener {

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private static boolean OBFUSCATE = false;
    private static String obfItem = "DIRT";
    private static String obfName = "???";
    private static List<String> obfLore = new ArrayList<>();

    private static ObfuscatedReward obfReward = null;
    private static String protocolPlugin = "none";

    /**
     * Loads the reward obfuscation configuration and initializes the system.
     * Reads settings from the plugin configuration and sets up the appropriate
     * protocol library for packet manipulation.
     */
    public static void load() {

        FileConfiguration config = plugin.getConfig();
        OBFUSCATE = config.getBoolean("settings.obfuscate-rewards", false);

        obfItem = config.getString("settings.obfuscated-reward-item.item", "DIRT");
        obfName = config.getString("settings.obfuscated-reward-item.name", "???");
        obfLore = config.getStringList("settings.obfuscated-reward-item.lore");

        OBFUSCATE = OBFUSCATE && (Utils.isEnabled("ProtocolLib") || Utils.isEnabled("packetevents"));
        protocolPlugin = Utils.isEnabled("ProtocolLib") ? "ProtocolLib" :
                Utils.isEnabled("PacketEvents") ? "packetevents" : "none";

        plugin.getLogger().log(Level.INFO, "Obfuscated rewards: " + OBFUSCATE + ", protocol plugin: " + protocolPlugin);

        obfReward = new ObfuscatedReward(obfItem, obfName, obfLore);
        registerOpenWindowListener();

    }

    /**
     * Checks if reward obfuscation is enabled.
     *
     * @return True if obfuscation is enabled, false otherwise
     */
    public static boolean isEnabled() {
        return OBFUSCATE;
    }

    /**
     * Gets the obfuscated reward configuration.
     *
     * @return The ObfuscatedReward object containing obfuscation settings
     */
    public static ObfuscatedReward getReward() {
        return obfReward;
    }

    /**
     * Checks if the system is using ProtocolLib for packet manipulation.
     *
     * @return True if using ProtocolLib, false otherwise
     */
    public static boolean isUsingProtocolLib() {
        return protocolPlugin.equals("ProtocolLib");
    }

    /**
     * Checks if the system is using PacketEvents for packet manipulation.
     *
     * @return True if using PacketEvents, false otherwise
     */
    public static boolean isUsingPacketEvents() {
        return protocolPlugin.equals("packetevents");
    }

    private static final Map<UUID, Integer> windowIds = new HashMap<>();

    /**
     * Stores a player's window ID for packet manipulation.
     *
     * @param uuid     The player's UUID
     * @param windowId The window ID to store
     */
    public static void putWindowId(UUID uuid, int windowId) {
        windowIds.put(uuid, windowId);
    }

    /**
     * Registers the appropriate window listener based on the available protocol library.
     * Sets up packet interception for inventory interactions.
     */
    public static void registerOpenWindowListener() {

        if (!isEnabled()) return;

        if (isUsingProtocolLib()) {

            ProtocolLibUtils.initProtocolLib(windowIds);
        }

    }

    /**
     * Deobfuscates a specific slot in a player's inventory, revealing the real item.
     *
     * @param p        The player whose inventory to deobfuscate
     * @param slot     The slot to deobfuscate
     * @param realItem The real item to show
     */
    public static void deobfuscate(Player p, int slot, ItemStack realItem) {
        if (!isEnabled()) return;
        sendRealSlot(p, realItem);
    }

    /**
     * Obfuscates an entire inventory, replacing real items with placeholder items.
     *
     * @param player    The player whose inventory to obfuscate
     * @param inventory The inventory to obfuscate
     */
    public static void obfuscateInventory(Player player, Inventory inventory) {

        if (!isEnabled()) return;

        Integer windowId = windowIds.get(player.getUniqueId());

        if (windowId == null) {
            plugin.getLogger().warning("No window ID found for player " + player.getName());
            return;
        }

        List<ItemStack> fakeItems = new ArrayList<>();
        ItemStack fakeItem = getReward().get();

        for (int i = 0; i < inventory.getSize(); i++) {

            ItemStack original = inventory.getItem(i);

            if (original != null && original.getType() != Material.AIR) {
                fakeItems.add(fakeItem.clone());
            } else {
                fakeItems.add(new ItemStack(Material.AIR));
            }
        }

        if (isUsingProtocolLib()) {
            ProtocolLibUtils.sendWindowItemsPacket(windowId, fakeItems, player);
        }

        if (isUsingPacketEvents()) {
            PacketEventsUtils.sendWindowItemsPacket(windowId, fakeItems, player);
        }

    }

    private static void sendRealSlot(Player player, ItemStack realItem) {

        if (!isEnabled()) return;

        if (isUsingProtocolLib()) {
            ProtocolLibUtils.sendSetSlotPacket(realItem, player);
        }

        if (isUsingPacketEvents()) {
            PacketEventsUtils.sendSetSlotPacket(realItem, player);
        }

    }


    @EventHandler
    public void onOpen(InventoryOpenEvent e) {

        if (!isEnabled()) return;

        try {

            Object view = InventoryOpenEvent.class.getMethod("getView").invoke(e);

            Method getTitle = view.getClass().getMethod("getTitle");
            getTitle.setAccessible(true);
            String title = (String) getTitle.invoke(view);

            if (title.equals(Messages.get("treasure-reward-menu-title"))) {

                Player p = (Player) e.getPlayer();

                Method getTopInventoryMethod = view.getClass().getMethod("getTopInventory");
                getTopInventoryMethod.setAccessible(true);
                Object topInventory = getTopInventoryMethod.invoke(view);

                Bukkit.getScheduler().runTaskLater(plugin, () ->
                {
                    obfuscateInventory(p, (Inventory) topInventory);
                }, 0L);

            }

        } catch (Exception ex) {
            plugin.getLogger().severe("Could not use reflection for InventoryOpenEvent on version " + Bukkit.getBukkitVersion() + ", " + Bukkit.getMinecraftVersion());
            ex.printStackTrace();
        }

    }

}
