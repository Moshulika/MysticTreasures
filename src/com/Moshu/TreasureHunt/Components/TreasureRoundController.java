package com.Moshu.TreasureHunt.Components;

import com.Moshu.TreasureHunt.Core.Treasure;

public class TreasureRoundController {

    private final Treasure t;
    private final TreasureRoundRegistry r;

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

    public void startRound(int roundNumber) {

        if(roundNumber < 0 || roundNumber >= r.getRounds().size()) {
            return;
        }

        r.getRound(roundNumber).start(t);

    }





}
