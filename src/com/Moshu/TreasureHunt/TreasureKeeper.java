package com.Moshu.TreasureHunt;

import io.lumine.mythic.api.mobs.MythicMob;
import org.bukkit.entity.EntityType;

//Modifiers, armor, etc
public class TreasureKeeper
{

    private MythicMob mythicMob;
    private EntityType type;
    private int amount;

    public TreasureKeeper(EntityType type, int amount)
    {
        this.type = type;
        this.amount = amount;
        this.mythicMob = null;
    }

    public TreasureKeeper(MythicMob type, int amount)
    {
        this.mythicMob = type;
        this.type = null;
        this.amount = amount;
    }

    public MythicMob getMythicMob()
    {
        return mythicMob;
    }

    public boolean isMythicMob()
    {
        return this.type == null;
    }

    public EntityType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }
}
