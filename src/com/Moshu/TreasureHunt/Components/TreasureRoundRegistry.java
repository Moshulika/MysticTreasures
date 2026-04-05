package com.Moshu.TreasureHunt.Components;

import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;

public class TreasureRoundRegistry {

    private int roundsNumber;
    private final List<TreasureRound> rounds = new ArrayList<>();
    private final TreasureData data;

    private boolean simpleRoundMode = false;

    public int getRoundsNumber() {
        return roundsNumber;
    }

    public void setRounds(int rounds) {
        this.roundsNumber = rounds;
    }

    public TreasureRoundRegistry(TreasureData data) {
        this.data = data;
    }

    public void load() {

        int roundsNumber = getRoundsNumber();

        for (int i = 0; i < roundsNumber; i++) {
            TreasureRound round = new TreasureRound(i, getTreasureKeepersInRound(i));
            rounds.add(round);
        }

    }

    private List<TreasureKeeper> getTreasureKeepersInRound(int round) {

        this.simpleRoundMode = getRoundsNumber() <= 1;

        List<TreasureKeeper> keepers = new ArrayList<>();

        if (simpleRoundMode) {
            keepers.addAll(data.getTreasureKeepers());
            return keepers;
        }

        for (TreasureKeeper keeper : data.getTreasureKeepers()) {
            if (isMobInRound(keeper, round)) keepers.add(keeper);
        }

        return keepers;
    }

    private boolean isMobInRound(TreasureKeeper keeper, int round) {
        // Internal round indices are 0-based, but config uses 1-based rounds.
        return keeper.getRounds().contains(round + 1);
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<TreasureRound> getRounds() {
        return rounds;
    }

    public TreasureRound getRound(int roundNumber) {
        return rounds.get(roundNumber);
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TreasureData getData() {
        return data;
    }


}
