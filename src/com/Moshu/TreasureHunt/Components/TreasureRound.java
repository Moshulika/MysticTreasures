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

import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeper;
import com.Moshu.TreasureHunt.Core.Treasure;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;

public class TreasureRound {

    private final int roundNumber;
    private final RoundData roundData;

    public TreasureRound(int roundNumber, RoundData roundData) {
        this.roundNumber = roundNumber;
        this.roundData = roundData;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public RoundData getRoundData() {
        return roundData;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<TreasureKeeper> getRoundTreasureKeepers() {
        return roundData != null ? roundData.getTreasureKeepers() : new ArrayList<>();
    }

    public void start(Treasure t) {
        if (roundData != null) {
            for (TreasureKeeper k : roundData.getTreasureKeepers()) {
                // Ensure the keeper knows the current treasure reference where necessary
                k.spawn(t.getSpawnedTreasureKeepers(), t);
            }
        }
    }

}

