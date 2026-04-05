package com.Moshu.TreasureHunt.Components;

import com.Moshu.Misc.Storage.Messages;
import com.Moshu.Misc.Storage.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Core.API.Events.TreasureDebuffEvent;
import com.Moshu.TreasureHunt.Core.Treasure;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import java.util.List;

/**
 * Represents a debuff system that can be applied to players during treasure hunts.
 * This class manages negative effects that can be triggered when players interact
 * with treasures, including potion effects, shockwaves, and mob respawning.
 *
 * @author Moshu
 * @version 1.0
 */
public class TreasureDebuff {

    private boolean enabled;
    private int clicksToDebuff;
    private boolean shockwave;
    private boolean respawnMobs;
    private List<PotionEffect> potionEffects;
    private final TreasureData d;

    private static final Particle EXPLOSION = Settings.getCompatParticle("treasure-spawn-particle");
    private static final Particle EXPLOSION_EMITTER = Settings.getCompatParticle("treasure-remove-particle");
    private static final Particle CAMPFIRE_SIGNAL_SMOKE = Settings.getCompatParticle("treasure-fall-particle");

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    /**
     * Creates a new treasure debuff with the specified treasure data.
     *
     * @param d The treasure data configuration
     */
    public TreasureDebuff(TreasureData d) {
        this.d = d;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public TreasureData getTreasureData() {
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

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setPotionEffects(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    /**
     * Applies debuff effects to players near the treasure when triggered.
     *
     * @param t The treasure instance to apply debuffs for
     */
    public void debuff(Treasure t) {

        if (!isEnabled()) return;
        if (t.alreadyDebuffed()) return;

        if (clicksToDebuff >= getTreasureData().getClicksToOpen()) {
            if (plugin != null)
                plugin.getLogger().severe("Clicks to debuff is greater than or equal to clicks to open! Change this in order to use it.");
            return;
        }

        if (t.getCurrentClicks() == getClicksToDebuff()) {

            TreasureDebuffEvent debuffEvent = new TreasureDebuffEvent(t);
            Bukkit.getPluginManager().callEvent(debuffEvent);

            if (debuffEvent.isCancelled()) return;

            Location treasureLoc = t.getLocation();
            Location playerLoc;

            for (Player k : Utils.getNearbyPlayers(treasureLoc, Settings.getProtectionRadius())) {

                playerLoc = k.getLocation();

                if (isShockwave()) {
                    World world = playerLoc.getWorld();
                    if (world != null) world.spawnParticle(Particle.SWEEP_ATTACK, playerLoc, 1);
                    k.setVelocity(playerLoc.getDirection().setY(0).multiply(-2).setY(0.5));
                }

                List<PotionEffect> effects = getPotionEffects();
                if (effects != null) k.addPotionEffects(effects);

                for (String s : Messages.getAndFormatList("messages.debuff-reached")) {
                    k.sendMessage(s);
                }

            }

            if (isRespawnMobs()) {
                // do we still need this?
            }

            World world = treasureLoc.getWorld();
            if (world != null) {
                world.strikeLightningEffect(treasureLoc);
                if (EXPLOSION_EMITTER != null) world.spawnParticle(EXPLOSION_EMITTER, treasureLoc, 3);
            }

        }

    }

}
