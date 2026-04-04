package com.Moshu.TreasureHunt.Components;

import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeper;
import com.Moshu.TreasureHunt.Core.Treasure;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class TreasureRound {

    private final int roundNumber;
    private final List<TreasureKeeper> roundTreasureKeepers;

    public int getRoundNumber() {
        return roundNumber;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<TreasureKeeper> getRoundTreasureKeepers() {
        return roundTreasureKeepers;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public TreasureRound(int roundNumber, List<TreasureKeeper> roundTreasureKeepers)
    {

        this.roundNumber = roundNumber;
        this.roundTreasureKeepers = roundTreasureKeepers;

    }

    public void start(Treasure t)
    {

        for(TreasureKeeper k : roundTreasureKeepers)
        {
            k.spawn(t.getSpawnedTreasureKeepers(), t);
        }

    }

}
