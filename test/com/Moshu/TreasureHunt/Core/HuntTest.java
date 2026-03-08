package com.Moshu.TreasureHunt.Core;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class HuntTest {

    @Test
    public void testIsHuntActiveCollision() {
        World world = Mockito.mock(World.class);
        when(world.getName()).thenReturn("world");

        // Mock a hunt at 10, 64, 10
        Hunt mockHunt = Mockito.mock(Hunt.class);
        Location huntLoc = new Location(world, 10, 64, 10);
        when(mockHunt.getLocation()).thenReturn(huntLoc);
        
        // Add to active cache
        mockHunt.setTreasureActive();

        // Check for collision at same spot
        Location collisionLoc = new Location(world, 10, 64, 10);
        assertTrue(Hunt.isHuntActive(collisionLoc));

        // Check for no collision at different spot
        Location safeLoc = new Location(world, 20, 64, 20);
        assertFalse(Hunt.isHuntActive(safeLoc));
        
        // Cleanup for other tests
        mockHunt.setInactive();
    }

    @Test
    public void testIsHuntActiveDifferentWorld() {
        World world1 = Mockito.mock(World.class);
        when(world1.getName()).thenReturn("world1");
        
        World world2 = Mockito.mock(World.class);
        when(world2.getName()).thenReturn("world2");

        Hunt mockHunt = Mockito.mock(Hunt.class);
        when(mockHunt.getLocation()).thenReturn(new Location(world1, 10, 64, 10));
        mockHunt.setTreasureActive();

        // Same coords, different world
        assertFalse(Hunt.isHuntActive(new Location(world2, 10, 64, 10)));
        
        mockHunt.setInactive();
    }
}
