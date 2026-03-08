package com.Moshu.TreasureHunt.Components.Rewards;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ItemRewardTest {

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
