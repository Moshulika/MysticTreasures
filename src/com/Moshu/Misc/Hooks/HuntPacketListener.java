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

