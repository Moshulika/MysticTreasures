package com.Moshu.TreasureHunt.Core.Interaction;

import com.Moshu.Main;
import com.Moshu.TreasureHunt.Core.Treasure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TreasureEventsTest {
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
    public void testClickNonTreasureBlock() {
        PlayerMock player = server.addPlayer();
        Block block = player.getLocation().getBlock();
        block.setType(Material.STONE);

        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.getInventory().getItemInMainHand(), block, null);
        server.getPluginManager().callEvent(event);

        // Should not be cancelled if it's just a normal block
        assertFalse(event.isCancelled());
    }

    @Test
    public void testHandleInteractionNonTreasure() {
        PlayerMock player = server.addPlayer();
        Location loc = player.getLocation();
        
        // Should return false for non-treasure location
        assertFalse(TreasureEvents.getInstance().handleInteraction(player, loc));
    }
}
