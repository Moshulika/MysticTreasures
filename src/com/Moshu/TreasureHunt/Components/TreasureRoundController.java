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
        return roundNumber == r.getRounds().size() - 1;
    }

    public boolean startRound() {

        // If all rounds are finished
        if (!hasMoreRounds()) {
            return false;
        }

        // If the current round is NOT cleared yet, don't start the next one
        // We check remainingMobs() from the Treasure object
        if (t.haveTheMobsSpawned() && t.remainingMobs() > 0) {
            return false;
        }

        // Apply debuff starting from the second round (index 1), but only if a debuff is configured and enabled
        if (roundNumber >= 1 && t.getTreasureData() != null && t.getTreasureData().getDebuff() != null) {
            if (t.getTreasureData().getDebuff().isEnabled()) {
                t.getTreasureData().getDebuff().debuff(t);
            }
        }

        r.getRound(roundNumber).start(t);
        roundNumber++;
        return true;

    }


}
