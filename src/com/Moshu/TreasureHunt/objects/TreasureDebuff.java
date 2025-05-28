package com.Moshu.TreasureHunt.objects;

import com.Moshu.Misc.Messages;
import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Treasure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public class TreasureDebuff {
    
    private boolean enabled;
    private int clicksToDebuff;
    private boolean shockwave;
    private boolean respawnMobs;
    private List<PotionEffect> potionEffects;
    private TreasureData d;

    private static final Particle EXPLOSION = Settings.getCompatParticle("treasure-spawn-particle");
    private static final Particle EXPLOSION_EMITTER = Settings.getCompatParticle("treasure-remove-particle");
    private static final Particle CAMPFIRE_SIGNAL_SMOKE = Settings.getCompatParticle("treasure-fall-particle");

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public TreasureDebuff(TreasureData d)
    {
        this.d = d;
    }

    public TreasureData getTreasureData()
    {
        return d;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getClicksToDebuff() {
        return clicksToDebuff;
    }

    public void setClicksToDebuff(int clicksToDebuff) {
        this.clicksToDebuff = clicksToDebuff;
    }

    public boolean isShockwave() {
        return shockwave;
    }

    public void setShockwave(boolean shockwave) {
        this.shockwave = shockwave;
    }

    public boolean isRespawnMobs() {
        return respawnMobs;
    }

    public void setRespawnMobs(boolean respawnMobs) {
        this.respawnMobs = respawnMobs;
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public void debuff(int clicks, Treasure t)
    {

        if(!isEnabled()) return;
        if(clicksToDebuff >= getTreasureData().getClicksToOpen())
        {
            plugin.getLogger().severe("Clicks to debuff is greater than or equal to clicks to open! Change this in order to use it.");
            return;
        }

        if(clicks == getClicksToDebuff())
        {

            Location treasureLoc = t.getLocation();
            Location playerLoc;

            for(Player k : Utils.getNearbyPlayers(treasureLoc, Settings.getProtectionRadius()))
            {

                playerLoc = k.getLocation();

                if(isShockwave())
                {
                    playerLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, playerLoc, 1);
                    k.setVelocity(playerLoc.getDirection().setY(0).multiply(-2).setY(0.5));
                }

                k.addPotionEffects(getPotionEffects());

                for (String s : Messages.getAndFormatList("messages.debuff-reached")) {
                    k.sendMessage(s);
                }

            }

            if(isRespawnMobs())
            {
                t.spawnTreasureKeepers();
            }

            treasureLoc.getWorld().strikeLightningEffect(treasureLoc);
            treasureLoc.getWorld().spawnParticle(EXPLOSION_EMITTER, treasureLoc, 3);

        }

    }

}
