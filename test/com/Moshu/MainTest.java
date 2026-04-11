package com.Moshu;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest {
    private ServerMock server;
    private Main plugin;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        // We might need to mock some dependencies if Main.onEnable() fails without them
        // But let's try a simple load first.
        plugin = MockBukkit.load(Main.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testPluginEnabled() {
        assertTrue(plugin.isEnabled());
    }

    @Test
    public void testConfigLoaded() {
        assertNotNull(plugin.getConfig());
    }
}
