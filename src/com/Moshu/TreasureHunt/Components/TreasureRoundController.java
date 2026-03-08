package com.Moshu.TreasureHunt.Components;

import com.Moshu.TreasureHunt.Core.Treasure;

public class TreasureRoundController {

    private final Treasure t;
    private final TreasureRoundRegistry r;

    private int roundNumber = 0;

    public TreasureRoundController(Treasure t, TreasureRoundRegistry r) {

        this.t = t;
        this.r = r;
    }

    public TreasureRoundRegistry getRoundRegistry() {
        return r;
    }

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
        if(!hasMoreRounds()) {
            return false;
        }

        // If the current round is NOT cleared yet, don't start the next one
        // We check remainingMobs() from the Treasure object
        if (t.haveTheMobsSpawned() && t.remainingMobs() > 0) {
            return false;
        }

        // Apply debuff starting from the second round (index 1)
        if(roundNumber >= 1)
        {
            t.getTreasureData().getDebuff().debuff(t);
        }

        r.getRound(roundNumber).start(t);
        roundNumber++;
        return true;

    }





}
