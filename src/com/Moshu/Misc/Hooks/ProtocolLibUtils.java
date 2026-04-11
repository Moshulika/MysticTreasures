/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.Moshu.Misc.Hooks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProtocolLibUtils {

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public static void initProtocolLib(Map<UUID, Integer> windowIds) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.OPEN_WINDOW) {

            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();
                int windowId = event.getPacket().getIntegers().read(0);
                windowIds.put(player.getUniqueId(), windowId);

            }
        });
    }

    public static void sendWindowItemsPacket(int windowId, List<ItemStack> fakeItems, Player player) {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.WINDOW_ITEMS);
        packet.getIntegers().write(0, windowId);
        packet.getItemListModifier().write(0, fakeItems);

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);

    }

    public static void sendSetSlotPacket(org.bukkit.inventory.ItemStack realItem, Player player) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SET_SLOT);
        packet.getIntegers().write(0, -1);
        packet.getIntegers().write(1, -1);
        packet.getItemModifier().write(0, realItem);

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    }


}

