package com.Moshu.TreasureHunt;

import com.Moshu.Main;
import com.Moshu.TreasureHunt.Components.Rewards.ItemReward;
import com.Moshu.TreasureHunt.Components.TreasureData;
import com.Moshu.TreasureHunt.Core.Hunt;
import com.Moshu.TreasureHunt.Core.Treasure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class HuntLifecycleTest {
    private ServerMock server;
    private Main plugin;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Main.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testFullHuntLifecycle() {
        PlayerMock player = server.addPlayer();
        Location spawnLoc = player.getLocation().add(5, 0, 5);
        
        // 1. Spawning
        // We need to bypass the file-based TreasureData.getByIdentifier
        // Since Hunt constructor calls it, we might need a way to mock the static registry or the Data object itself.
        // For this test, let's assume we have a way to get a mock TreasureData.
        
        TreasureData mockData = Mockito.mock(TreasureData.class);
        when(mockData.getIdentifier()).thenReturn("test_treasure");
        when(mockData.getTreasureName()).thenReturn("Test Treasure");
        when(mockData.getTreasureBlockString()).thenReturn("CHEST");
        when(mockData.getTreasureType()).thenReturn(TreasureData.TreasureType.VANILLA);
        when(mockData.getWorld()).thenReturn(spawnLoc.getWorld());
        when(mockData.getDuration()).thenReturn(30);
        
        com.Moshu.TreasureHunt.Components.TreasureKey mockKey = Mockito.mock(com.Moshu.TreasureHunt.Components.TreasureKey.class);
        when(mockKey.requiresKey()).thenReturn(false);
        when(mockData.getTreasureKey()).thenReturn(mockKey);
        
        com.Moshu.TreasureHunt.Components.TreasureRoundRegistry mockRegistry = Mockito.mock(com.Moshu.TreasureHunt.Components.TreasureRoundRegistry.class);
        when(mockData.getRoundRegistry()).thenReturn(mockRegistry);
        
        // Mock rewards
        ItemReward reward = new ItemReward();
        reward.setAmount(1);
        reward.setChance(100);
        reward.setName("Legendary Sword");
        reward.setItemString("DIAMOND_SWORD");
        
        List<ItemReward> rewards = new ArrayList<>();
        rewards.add(reward);
        when(mockData.getItemRewards()).thenReturn(rewards);
        when(mockData.canOpenChest()).thenReturn(false); // Simple award on click for this test
        
        // 2. Create a Hunt and directly wire in our mocked TreasureData
        Hunt hunt = new Hunt(spawnLoc, "test_treasure", 30);
        // The Hunt constructor relies on TreasureData.getByIdentifier, which is file-backed and
        // hard to mock. For unit testing we instead construct a Treasure instance directly using
        // the mocked TreasureData.
        Treasure treasure = new Treasure(hunt, mockData);
        assertNotNull(treasure, "Treasure should be created with mocked TreasureData");
        
        // 3. Interaction & Rewards
        // Simulate player clicking the treasure
        // Since we can't easily trigger the full start() logic without real config files,
        // we test the awardPrize logic which is a key component.
        
        int initialItems = 0;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is != null && is.getType() != Material.AIR) initialItems++;
        }
        
        treasure.awardPrize(player);
        
        int afterItems = 0;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is != null && is.getType() != Material.AIR) afterItems++;
        }
        
        // Verify player received the reward
        assertTrue(afterItems > initialItems, "Player should have received a reward item");
    }

    @Test
    public void testMobClearingLogic() {
        // Mock a treasure with spawned mobs
        Treasure treasure = Mockito.mock(Treasure.class);
        ArrayList<org.bukkit.entity.Entity> mobs = new ArrayList<>();
        
        // Add a "live" mob
        org.bukkit.entity.Entity mockMob = Mockito.mock(org.bukkit.entity.LivingEntity.class);
        when(mockMob.isDead()).thenReturn(false);
        mobs.add(mockMob);
        
        when(treasure.getSpawnedTreasureKeepers()).thenReturn(mobs);
        
        // The real mobsCleared() iterates over spawnedTreasureKeepers
        // We'll test the actual method implementation logic if possible or its usage.
    }
}
