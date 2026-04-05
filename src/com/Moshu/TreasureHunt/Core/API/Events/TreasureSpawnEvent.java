package com.Moshu.TreasureHunt.Core.API.Events;

import com.Moshu.TreasureHunt.Core.Treasure;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TreasureSpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Treasure treasure;

    public TreasureSpawnEvent(Treasure treasure) {
        this.treasure = treasure;
    }

    public Treasure getTreasure() {
        return treasure;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
