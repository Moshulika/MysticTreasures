package com.Moshu.Misc.Hooks;

import com.Moshu.TreasureHunt.Components.Rewards.RewardObfuscator;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;

public class HuntPacketListener implements com.github.retrooper.packetevents.event.PacketListener {

    @Override
    public void onPacketSend(PacketSendEvent event) {

        User user = event.getUser();
        if (event.getPacketType() != PacketType.Play.Server.OPEN_WINDOW) return;

        WrapperPlayServerOpenWindow openWindow = new WrapperPlayServerOpenWindow(event);
        RewardObfuscator.putWindowId(user.getUUID(), openWindow.getContainerId());

    }

}
