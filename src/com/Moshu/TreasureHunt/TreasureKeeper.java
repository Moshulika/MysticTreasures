package com.Moshu.TreasureHunt;

import org.bukkit.entity.EntityType;

//Modifiers, armor, etc
public class TreasureKeeper
{

    private EntityType type;
    private int amount;

    public TreasureKeeper(EntityType type, int amount)
    {
        this.type = type;
        this.amount = amount;
    }

    public EntityType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }
}
