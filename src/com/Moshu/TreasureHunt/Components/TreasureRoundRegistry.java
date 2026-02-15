package com.Moshu.TreasureHunt.Components;

import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeper;

import java.util.ArrayList;

public class TreasureRoundRegistry {

    private int roundsNumber;
    private final ArrayList<TreasureRound> rounds = new ArrayList<>();
    private final TreasureData data;

    public int getRoundsNumber() {
        return roundsNumber;
    }

    public void setRounds(int rounds) {
        this.roundsNumber = rounds;
    }

    public TreasureRoundRegistry(TreasureData data)
    {
        this.data = data;
        load();
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

        ArrayList<TreasureKeeper> keepers = new ArrayList<>();

        for(TreasureKeeper keeper : data.getTreasureKeepers())
        {
            if(isMobInRound(keeper, round)) keepers.add(keeper);
        }

        return keepers;
    }

    private boolean isMobInRound(TreasureKeeper keeper, int round)
    {
        return keeper.getRounds().contains(round);
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
