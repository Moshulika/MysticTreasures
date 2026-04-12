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

package com.Moshu.TreasureHunt.Components;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class TreasureRoundRegistry {

    private final List<String> roundIds = new ArrayList<>();
    private final List<TreasureRound> rounds = new ArrayList<>();
    private final TreasureData data;
    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public void setRoundIds(List<String> rounds) {
        this.roundIds.clear();
        this.roundIds.addAll(rounds);
    }

    public int getRoundsNumber() {
        return roundIds.size();
    }

    public TreasureRoundRegistry(TreasureData data) {
        this.data = data;
    }

    public void load() {
        rounds.clear();

        if (data == null) {
             if (plugin != null) plugin.getLogger().severe("TreasureRoundRegistry has NULL TreasureData!");
             return;
        }

        if (roundIds.isEmpty()) {
            if (plugin != null) {
                plugin.getLogger().severe("Treasure '" + data.getIdentifier() + "' has NO rounds configured! This will cause errors.");
            }
            return;
        }

        int index = 0;
        for (String id : roundIds) {
            RoundData roundData = RoundManager.getRound(id);
            if (roundData == null) {
                if (plugin != null) {
                    plugin.getLogger().severe("Round '" + id + "' was NOT found for treasure '" + data.getIdentifier() + "'! Check your /rounds/ folder.");
                }
            } else {
                if (plugin != null) {
                    plugin.getLogger().info("Successfully linked round '" + id + "' to treasure '" + data.getIdentifier() + "' (Round #" + (index + 1) + ")");
                }
            }
            TreasureRound round = new TreasureRound(index, roundData);
            rounds.add(round);
            index++;
        }
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<TreasureRound> getRounds() {
        return rounds;
    }

    public TreasureRound getRound(int roundNumber) {
        if (roundNumber >= 0 && roundNumber < rounds.size()) {
            return rounds.get(roundNumber);
        }
        return null;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TreasureData getData() {
        return data;
    }

}

