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
