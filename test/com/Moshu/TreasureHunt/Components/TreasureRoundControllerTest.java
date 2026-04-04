package com.Moshu.TreasureHunt.Components;

import com.Moshu.TreasureHunt.Core.Treasure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TreasureRoundControllerTest {

    private Treasure treasure;
    private TreasureRoundRegistry registry;
    private TreasureRoundController controller;
    private ArrayList<TreasureRound> rounds;

    @BeforeEach
    public void setup() {
        MockBukkit.mock();
        treasure = Mockito.mock(Treasure.class);
        registry = Mockito.mock(TreasureRoundRegistry.class);
        rounds = new ArrayList<>();
        
        // Add 2 mock rounds
        rounds.add(Mockito.mock(TreasureRound.class));
        rounds.add(Mockito.mock(TreasureRound.class));
        
        when(registry.getRounds()).thenReturn(rounds);
        when(registry.getRound(0)).thenReturn(rounds.get(0));
        when(registry.getRound(1)).thenReturn(rounds.get(1));
        
        TreasureData mockData = Mockito.mock(TreasureData.class);
        when(treasure.getTreasureData()).thenReturn(mockData);

        controller = new TreasureRoundController(treasure, registry);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testStartRoundSequence() {
        // Assume no mobs spawned yet for first round
        when(treasure.haveTheMobsSpawned()).thenReturn(false);
        
        assertTrue(controller.startRound()); // Starts Round 1 (index 0)
        assertEquals(1, controller.getRoundNumber());
        
        // After first round started, mobs are considered spawned. 
        // Need to mock mobs cleared to start next round.
        when(treasure.haveTheMobsSpawned()).thenReturn(true);
        when(treasure.remainingMobs()).thenReturn(0);
        
        assertTrue(controller.startRound()); // Starts Round 2 (index 1)
        assertEquals(2, controller.getRoundNumber());
        
        assertFalse(controller.startRound()); // No more rounds
    }

    @Test
    public void testStartRoundBlockedByMobs() {
        when(treasure.haveTheMobsSpawned()).thenReturn(true);
        when(treasure.remainingMobs()).thenReturn(5); // Mobs still alive
        
        assertFalse(controller.startRound());
        assertEquals(0, controller.getRoundNumber());
    }

    @Test
    public void testHasMoreRounds() {
        assertTrue(controller.hasMoreRounds());
        
        when(treasure.haveTheMobsSpawned()).thenReturn(false);
        controller.startRound(); // Round 1
        
        assertTrue(controller.hasMoreRounds());
        
        when(treasure.haveTheMobsSpawned()).thenReturn(true);
        when(treasure.remainingMobs()).thenReturn(0);
        controller.startRound(); // Round 2
        
        assertFalse(controller.hasMoreRounds());
    }
}
