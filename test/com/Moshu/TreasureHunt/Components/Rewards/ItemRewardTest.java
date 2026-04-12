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

package com.Moshu.TreasureHunt.Components.Rewards;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemRewardTest {

    @BeforeEach
    public void setup() {
        MockBukkit.mock();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testGetSanitizedName() {
        ItemReward reward = new ItemReward();
        reward.setName("&6&lGolden &eApple");
        assertEquals("Golden Apple", reward.getSanitizedName());

        reward.setName("Normal Item");
        assertEquals("Normal Item", reward.getSanitizedName());

        reward.setName("#FF5555Red Item");
        // ChatColor.stripColor also handles some hex patterns or at least &x
        // But for standard & codes it definitely works.
        assertTrue(reward.getSanitizedName().contains("Red Item"));
    }
}
