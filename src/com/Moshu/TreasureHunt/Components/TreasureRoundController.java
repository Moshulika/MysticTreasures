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

import com.Moshu.TreasureHunt.Core.Treasure;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TreasureRoundController {

    private final Treasure t;
    private final TreasureRoundRegistry r;

    private int roundNumber = 0;

    public TreasureRoundController(Treasure t, TreasureRoundRegistry r) {

        this.t = t;
        this.r = r;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TreasureRoundRegistry getRoundRegistry() {
        return r;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Treasure getTreasure() {
        return t;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public boolean hasMoreRounds() {
        return roundNumber < r.getRounds().size();
    }

    public boolean isLastRound() {
        return roundNumber >= r.getRounds().size();
    }

    public boolean startRound() {

        // If all rounds are finished
        if (!hasMoreRounds()) {
            return false;
        }

        // If the current round is NOT cleared yet, don't start the next one
        if (t.haveTheMobsSpawned() && t.remainingMobs() > 0) {
            return false;
        }

        // Give rewards for the previous round if it's not the first time
        if (roundNumber > 0) {
            TreasureRound prevRound = r.getRound(roundNumber - 1);
            if (prevRound != null && prevRound.getRoundData() != null) {
                t.giveRewards(prevRound.getRoundData());
            }
        }

        TreasureRound round = r.getRound(roundNumber);

        // Apply debuff starting from the second round (index 1), but only if a debuff is configured
        if (roundNumber >= 1 && round != null && round.getRoundData() != null && round.getRoundData().getDebuff() != null) {
            round.getRoundData().getDebuff().debuff(t);
        }

        if (round != null) {
            round.start(t);
            t.markMobsSpawned();
            if (round.getRoundData() != null && round.getRoundData().getAwardMethod() == Treasure.AwardMethod.CHEST) {
                t.setupInventory();
            }
        }
        
        roundNumber++;
        return true;

    }


}
