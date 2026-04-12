/*
 * Copyright 2026 Moshu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This software is also subject to the Commons Clause License Condition v1.0.
 * You may obtain a copy of the Commons Clause at:
 * https://commonsclause.com/
 */

package com.Moshu.TreasureHunt.Core;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class HuntTest {

    @BeforeEach
    public void setup() {
        MockBukkit.mock();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testIsHuntActiveCollision() {
        World world = Mockito.mock(World.class);
        when(world.getName()).thenReturn("world");

        // Mock a hunt at 10, 64, 10
        Hunt mockHunt = Mockito.mock(Hunt.class);
        Location huntLoc = new Location(world, 10, 64, 10);
        when(mockHunt.getLocation()).thenReturn(huntLoc);

        // Add to active cache
        Mockito.doCallRealMethod().when(mockHunt).setTreasureActive();
        Mockito.doCallRealMethod().when(mockHunt).setInactive();
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
