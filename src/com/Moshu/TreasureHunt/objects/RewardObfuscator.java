package com.Moshu.TreasureHunt.objects;

import com.Moshu.Misc.Messages;
import com.Moshu.Misc.PacketEventsUtils;
import com.Moshu.Misc.ProtocolLibUtils;
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

public class RewardObfuscator implements Listener {

    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private static boolean OBFUSCATE = false;
    private static String obfItem = "DIRT";
    private static String obfName = "???";
    private static List<String> obfLore = new ArrayList<>();

    private static ObfuscatedReward obfReward = null;
    private static String protocolPlugin = "none";

    public static void load()
    {

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

    public static boolean isEnabled()
    {
        return OBFUSCATE;
    }

    public static ObfuscatedReward getReward()
    {
        return obfReward;
    }

    public static boolean isUsingProtocolLib()
    {
        return protocolPlugin.equals("ProtocolLib");
    }

    public static boolean isUsingPacketEvents()
    {
        return protocolPlugin.equals("packetevents");
    }

    private static final Map<UUID, Integer> windowIds = new HashMap<>();

    public static void putWindowId(UUID uuid, int windowId)
    {
        windowIds.put(uuid, windowId);
    }

    public static void registerOpenWindowListener() {

        if(!isEnabled()) return;

        if(isUsingProtocolLib()) {

            ProtocolLibUtils.initProtocolLib(windowIds);
        }

    }

    public static void deobfuscate(Player p, int slot, ItemStack realItem)
    {
        if(!isEnabled()) return;
        sendRealSlot(p, realItem);
    }

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

        if(isUsingProtocolLib())
        {
            ProtocolLibUtils.sendWindowItemsPacket(windowId, fakeItems, player);
        }

        if(isUsingPacketEvents())
        {
            PacketEventsUtils.sendWindowItemsPacket(windowId, fakeItems, player);
        }

    }

    private static void sendRealSlot(Player player, ItemStack realItem) {

        if (!isEnabled()) return;

        if(isUsingProtocolLib()) {
            ProtocolLibUtils.sendSetSlotPacket(realItem, player);
        }

        if(isUsingPacketEvents())
        {
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

            if(title.equals(Messages.get("treasure-reward-menu-title")))
            {

                Player p = (Player) e.getPlayer();

                Method getTopInventoryMethod = view.getClass().getMethod("getTopInventory");
                getTopInventoryMethod.setAccessible(true);
                Object topInventory = getTopInventoryMethod.invoke(view);

                Bukkit.getScheduler().runTaskLater(plugin, ()->
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
