package com.Moshu.TreasureHunt.Components;

import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeper;

import java.util.ArrayList;

public class TreasureRoundRegistry {

    private int roundsNumber;
    private final ArrayList<TreasureRound> rounds = new ArrayList<>();
    private final TreasureData data;

    private boolean simpleRoundMode = false;

    public int getRoundsNumber() {
        return roundsNumber;
    }

    public void setRounds(int rounds) {
        this.roundsNumber = rounds;
    }

    public TreasureRoundRegistry(TreasureData data)
    {
        this.data = data;
    }

    public void load() {

        int roundsNumber = getRoundsNumber();

        for(int i = 0; i < roundsNumber; i++)
        {
            TreasureRound round = new TreasureRound(i, getTreasureKeepersInRound(i));
            rounds.add(round);
        }

    }

    private ArrayList<TreasureKeeper> getTreasureKeepersInRound(int round)
    {

        this.simpleRoundMode = getRoundsNumber() <= 1;

        ArrayList<TreasureKeeper> keepers = new ArrayList<>();

        if(simpleRoundMode)
        {
            keepers.addAll(data.getTreasureKeepers());
            return keepers;
        }

        for(TreasureKeeper keeper : data.getTreasureKeepers())
        {
            if(isMobInRound(keeper, round)) keepers.add(keeper);
        }

        return keepers;
    }

    private boolean isMobInRound(TreasureKeeper keeper, int round)
    {
        // Internal round indices are 0-based, but config uses 1-based rounds.
        return keeper.getRounds().contains(round + 1);
    }

    public ArrayList<TreasureRound> getRounds() {
        return rounds;
    }

    public TreasureRound getRound(int roundNumber) {
        return rounds.get(roundNumber);
    }

    public TreasureData getData() {
        return data;
    }


}
