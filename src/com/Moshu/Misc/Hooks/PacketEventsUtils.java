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

    public static void initPacketEvents() {
        PacketEvents.getAPI().init();
    }

    public static boolean isReady() {
        return PacketEvents.getAPI().isInitialized() && PacketEvents.getAPI().isLoaded();
    }

    public static boolean initReady() {
        return PacketEvents.getAPI().isInitialized();
    }

    public static void loadPacketEvents() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(Bukkit.getPluginManager().getPlugin("MysticTreasures")));
        PacketEvents.getAPI().load();

        PacketEvents.getAPI().getEventManager().registerListener(
                new HuntPacketListener(), PacketListenerPriority.HIGH);
    }

    public static void disablePacketEvents() {
        PacketEvents.getAPI().terminate();
    }

    public static void sendWindowItemsPacket(int windowId, List<org.bukkit.inventory.ItemStack> fakeItems, Player player) {

        List<com.github.retrooper.packetevents.protocol.item.ItemStack> convertedFakeItems = new ArrayList<>();

        for (org.bukkit.inventory.ItemStack is : fakeItems) {
            convertedFakeItems.add(SpigotConversionUtil.fromBukkitItemStack(is.clone()));
        }

        WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(windowId, 0, convertedFakeItems, null);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, windowItems);
    }

    public static void sendSetSlotPacket(org.bukkit.inventory.ItemStack realItem, Player player) {
        WrapperPlayServerSetSlot slot = new WrapperPlayServerSetSlot(-1, 0, -1, SpigotConversionUtil.fromBukkitItemStack(realItem));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, slot);
    }

}

