package com.Moshu.Misc;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class LocationsTest {

    @BeforeEach
    public void setup() {
        MockBukkit.mock();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testDistanceSquaredTo() {
        World world = Mockito.mock(World.class);
        when(world.getName()).thenReturn("world");

        Location loc1 = new Location(world, 0, 0, 0);
        Location loc2 = new Location(world, 3, 4, 0); // 3^2 + 4^2 = 25

        assertEquals(25.0, Locations.distanceSquaredTo(loc1, loc2), 0.001);
    }

    @Test
    public void testDistanceSquaredToDifferentWorlds() {
        World world1 = Mockito.mock(World.class);
        when(world1.getName()).thenReturn("world1");
        
        World world2 = Mockito.mock(World.class);
        when(world2.getName()).thenReturn("world2");

        Location loc1 = new Location(world1, 0, 0, 0);
        Location loc2 = new Location(world2, 0, 0, 0);

        assertEquals(Double.MAX_VALUE, Locations.distanceSquaredTo(loc1, loc2));
    }

    @Test
    public void testIsInBorderVanilla() {
        World world = Mockito.mock(World.class);
        when(world.getName()).thenReturn("world");
        
        WorldBorder border = Mockito.mock(WorldBorder.class);
        when(border.getSize()).thenReturn(200.0); // Size is diameter, so radius is 100
        when(world.getWorldBorder()).thenReturn(border);

        // Max distance in config is large
        int maxTreasureDistance = 5000;

        // Inside border (radius 100 - 10 safety = 90)
        assertTrue(Locations.isInBorder(new Location(world, 50, 64, 50), maxTreasureDistance));
        
        // Outside border
        assertFalse(Locations.isInBorder(new Location(world, 150, 64, 150), maxTreasureDistance));
    }
}
