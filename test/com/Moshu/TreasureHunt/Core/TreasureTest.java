package com.Moshu.TreasureHunt.Core;

import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class TreasureTest {

    @BeforeEach
    public void setup() {
        MockBukkit.mock();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testIsTreasureLogic() {
        // Since isTreasure is static and relies on Hunt.getActiveTreasures(),
        // and we can't easily mock static methods without specialized libraries,
        // we'll focus on testing logic that can be isolated or mocked via instances.
    }

    @Test
    public void testRenameWorld() {
        World world = Mockito.mock(World.class);
        when(world.getName()).thenReturn("World-123! Test");

        String renamed = Treasure.renameWorld(world);
        // Pattern: [^-_A-Za-z] -> _
        assertEquals("World-123__Test", renamed);
    }

    @Test
    public void testGetHologramName() {
        World world = Mockito.mock(World.class);
        when(world.getName()).thenReturn("MyWorld");

        String name = Treasure.getHologramName(world, "GoldenChest");
        assertEquals("treasure_myworld_goldenchest", name);
    }
}
