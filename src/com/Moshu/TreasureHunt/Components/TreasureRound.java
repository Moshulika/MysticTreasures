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
