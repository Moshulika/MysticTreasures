package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Cooldown;
import com.Moshu.Misc.Messages;
import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TreasureEvents implements Listener {

    @EventHandler
    public void onFallingBlockLand(EntityChangeBlockEvent e){

        if(e.getEntity() instanceof FallingBlock f){

            if(f.getMaterial() == Settings.getWorldMaterialUnknown(e.getEntity().getWorld().getName(), "treasure-block"))
            {
                e.setCancelled(true);
                f.remove();

        }
    }
}

    @EventHandler
    public void onBreak(BlockBreakEvent e) {

        if (Hunt.isActive(e.getBlock().getWorld())) {

            if (Treasure.isTreasure(e.getBlock().getLocation())) {

                e.setCancelled(true);

                String worldName = e.getBlock().getWorld().getName();
                Location o = e.getBlock().getLocation();
                Location l = new Location(o.getWorld(), o.getBlockX(), o.getBlockY(), o.getBlockZ());
                Player p = e.getPlayer();

                if(Cooldown.hasCooldown(p.getUniqueId(), "treasure-winner"))
                {
                    p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
                    Utils.sendBreakSound(e.getPlayer());
                    p.sendMessage(Messages.get("winner-cooldown").replace("{time}", Utils.formatRemainingTime(Cooldown.getRemainingTimeMinutes(p.getUniqueId(), "treasure-winner"))));
                    return;
                }

                if(Settings.getWorldBooleanUnknown(worldName, "require-all-mobs-dead"))
                {

                    Treasure t = Treasure.getTreasure(l);

                    if (t.mobsCleared()) {

                        t.awardPrize(e.getPlayer());
                        t.remove();

                        Utils.sendLevelupSound(p);
                    }
                    else
                    {

                        for(String s : Messages.getAndFormatList("messages.need-to-kill-all-mobs"))
                        {
                            p.sendMessage(s);
                        }

                        p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
                        Utils.sendBreakSound(e.getPlayer());
                    }

                }
                else
                {
                    Treasure t = Treasure.getTreasure(l);
                    t.awardPrize(e.getPlayer());
                    t.remove();

                    Utils.sendLevelupSound(p);
                }

            }
            else
            {

                if (Treasure.isNearTreasure(e.getPlayer())) {

                    e.setCancelled(true);
                    e.getPlayer().sendMessage(Messages.get("cannot-break-near-treasure"));

                }

            }

        }
    }

    @EventHandler
    public void onDamage(BlockPlaceEvent e) {

        if (Hunt.isActive(e.getBlock().getWorld())) {


            if (Treasure.isNearTreasure(e.getPlayer())) {

                e.setCancelled(true);
                e.getPlayer().sendMessage(Messages.get("cannot-place-near-treasure"));
            }

        }


    }

    @EventHandler
    public void onDamage(PlayerBucketEmptyEvent e) {

        if (Hunt.isActive(e.getPlayer().getWorld())) {


            if (Treasure.isNearTreasure(e.getPlayer())) {

                e.setCancelled(true);
                e.getPlayer().sendMessage(Messages.get("cannot-place-near-treasure"));

            }
        }


    }

    @EventHandler
    public void onExplode(BlockExplodeEvent e)
    {

        if(Hunt.isActive(e.getBlock().getWorld())) {

            for (Block b : e.blockList()) {

                if (Treasure.isTreasure(b.getLocation())) {
                    e.setCancelled(true);
                }

            }
        }

    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e)
    {

        if(Hunt.isActive(e.getEntity().getWorld())) {


            for (Block b : e.blockList()) {

                if (Treasure.isTreasure(b.getLocation())) {
                    e.setCancelled(true);
                }

            }

        }

    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {

        if (Hunt.isActive(e.getEntity().getWorld())) {

            if (e.getDamager() instanceof Player) {

                if (e.getEntity() instanceof Player p) {

                    if (Treasure.isNearTreasure(p)) {

                        e.setCancelled(true);
                        e.setDamage(0);

                        e.getDamager().sendMessage(Messages.get("cannot-attack-near-treasure"));

                    }


                }
            }
        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        if (Hunt.isActive(e.getPlayer().getWorld()))
        {

            if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()) {

                if (Treasure.isNearTreasure(e.getPlayer())) {

                    if (e.getPlayer().isFlying() && !e.getPlayer().hasPermission("mystictreasures.bypass")) {

                        if(!Settings.getWorldBooleanUnknown(e.getPlayer().getWorld().getName(), "allow-flight-near-treasure")) {

                            e.getPlayer().setFlying(false);
                            e.getPlayer().setAllowFlight(false);
                            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 1200, 1));

                        }

                    }

                    if (e.getPlayer().isInvulnerable() && !e.getPlayer().hasPermission("mystictreasures.bypass")) {

                        if(!Settings.getWorldBooleanUnknown(e.getPlayer().getWorld().getName(), "allow-god-near-treasure")) {

                            e.getPlayer().setInvulnerable(false);

                        }
                    }

                    if(e.getPlayer().isGliding() && !e.getPlayer().hasPermission("mystictreasures.bypass"))
                    {

                        if(!Settings.getWorldBooleanUnknown(e.getPlayer().getWorld().getName(), "allow-elytra-near-treasure")) {

                            e.getPlayer().setGliding(false);
                            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 1200, 1));

                        }

                    }

                }


            }

        }

    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        if (Hunt.isActive(e.getPlayer().getWorld())) {

            if (e.getPlayer().getGameMode() != GameMode.SPECTATOR) {

                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                    String worldName = e.getClickedBlock().getWorld().getName();

                    if (e.getClickedBlock().getType() == Settings.getWorldMaterialUnknown(worldName, "treasure-block")) {

                        Location o = e.getClickedBlock().getLocation();
                        Location l = new Location(o.getWorld(), o.getBlockX(), o.getBlockY(), o.getBlockZ());
                        Player p = e.getPlayer();

                        if (Treasure.isTreasure(l)) {

                            e.setCancelled(true);

                            if (Cooldown.hasCooldown(p.getUniqueId(), "treasure-winner")) {
                                p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
                                Utils.sendBreakSound(e.getPlayer());
                                p.sendMessage(Messages.get("winner-cooldown").replace("{time}", Utils.formatRemainingTime(Cooldown.getRemainingTimeMinutes(p.getUniqueId(), "treasure-winner"))));
                                return;
                            }

                            if (Settings.getWorldBooleanUnknown(worldName, "require-all-mobs-dead")) {


                                Treasure t = Treasure.getTreasure(l);

                                if (t.mobsCleared()) {

                                    t.awardPrize(e.getPlayer());
                                    t.remove();

                                    Utils.sendLevelupSound(p);

                                } else {

                                    for (String s : Messages.getAndFormatList("messages.need-to-kill-all-mobs")) {
                                        p.sendMessage(s);
                                    }

                                    p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
                                    Utils.sendBreakSound(e.getPlayer());
                                }

                            } else {
                                Treasure t = Treasure.getTreasure(l);
                                t.awardPrize(e.getPlayer());
                                t.remove();

                                Utils.sendLevelupSound(p);
                            }

                        }

                    }
                }
            }
        }

    }

}
