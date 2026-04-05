package com.Moshu.TreasureHunt.Core.API.Events;

import com.Moshu.TreasureHunt.Core.Treasure;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TreasureInteractEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Treasure treasure;
    private final Player player;
    private boolean cancelled;

    public TreasureInteractEvent(Treasure treasure, Player player) {
        this.treasure = treasure;
        this.player = player;
    }

    public Treasure getTreasure() {
        return treasure;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
