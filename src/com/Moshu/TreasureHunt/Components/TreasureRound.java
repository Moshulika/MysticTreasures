package com.Moshu.TreasureHunt.Components;

import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeper;
import com.Moshu.TreasureHunt.Core.Treasure;

import java.util.ArrayList;

public class TreasureRound {

    private final int roundNumber;
    private final ArrayList<TreasureKeeper> roundTreasureKeepers;
    private TreasureData data;

    public int getRoundNumber() {
        return roundNumber;
    }

    public ArrayList<TreasureKeeper> getRoundTreasureKeepers() {
        return roundTreasureKeepers;
    }

    public TreasureData getData() {
        return data;
    }

    public TreasureRound(int roundNumber, ArrayList<TreasureKeeper> roundTreasureKeepers)
    {

        this.roundNumber = roundNumber;
        this.roundTreasureKeepers = roundTreasureKeepers;

    }

    public void start(Treasure t)
    {



    }

}
