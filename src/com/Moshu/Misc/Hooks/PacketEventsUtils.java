package com.Moshu.Misc.Hooks;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PacketEventsUtils {

    public static void initPacketEvents()
    {
        PacketEvents.getAPI().init();
    }

    public static boolean isReady()
    {
        return PacketEvents.getAPI().isInitialized() && PacketEvents.getAPI().isLoaded();
    }

    public static boolean initReady()
    {
        return PacketEvents.getAPI().isInitialized();
    }

    public static void loadPacketEvents()
    {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(Bukkit.getPluginManager().getPlugin("MysticTreasures")));
        PacketEvents.getAPI().load();

        PacketEvents.getAPI().getEventManager().registerListener(
                new PacketListener(), PacketListenerPriority.HIGH);
    }

    public static void disablePacketEvents()
    {
        PacketEvents.getAPI().terminate();
    }

    public static void sendWindowItemsPacket(int windowId, List<org.bukkit.inventory.ItemStack> fakeItems, Player player)
    {

        List<com.github.retrooper.packetevents.protocol.item.ItemStack> convertedFakeItems = new ArrayList<>();

        for(org.bukkit.inventory.ItemStack is : fakeItems)
        {
            convertedFakeItems.add(SpigotConversionUtil.fromBukkitItemStack(is.clone()));
        }

        WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(windowId, 0, convertedFakeItems, null);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, windowItems);
    }

    public static void sendSetSlotPacket(org.bukkit.inventory.ItemStack realItem, Player player)
    {
        WrapperPlayServerSetSlot slot = new WrapperPlayServerSetSlot(-1, 0, -1, SpigotConversionUtil.fromBukkitItemStack(realItem));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, slot);
    }

}
