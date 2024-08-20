package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Cooldown;
import com.Moshu.Misc.Messages;
import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
import dev.lone.itemsadder.api.CustomEntity;
import dev.lone.itemsadder.api.CustomFurniture;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TreasureEvents implements Listener {


    @EventHandler
    public void onBreak(BlockBreakEvent e) {

        if (Hunt.isActive(e.getBlock().getWorld())) {

            if (Treasure.isTreasure(e.getBlock().getLocation())) {

                e.setCancelled(true);

                String worldName = e.getBlock().getWorld().getName();
                Location o = e.getBlock().getLocation();
                Location l = new Location(o.getWorld(), o.getBlockX(), o.getBlockY(), o.getBlockZ());
                Player p = e.getPlayer();

                if(p.getGameMode() != GameMode.SURVIVAL) {
                    p.sendMessage(Messages.get("no-creative"));
                    p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
                    Utils.sendBreakSound(e.getPlayer());
                    return;
                }

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

                        if(Settings.getWorldBooleanUnknown(worldName, "reward-all-players-who-participated"))
                        {
                            t.awardPrizes();
                        }
                        else
                        {
                            t.awardPrize(e.getPlayer());
                        }

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

                    if(Settings.getWorldBooleanUnknown(worldName, "reward-all-players-who-participated"))
                    {
                        t.awardPrizes();
                    }
                    else
                    {
                        t.awardPrize(e.getPlayer());
                    }

                    t.remove();
                    Utils.sendLevelupSound(p);
                }

            }
            else
            {

                if (Treasure.isNearTreasure(e.getPlayer())) {

                    if(e.getPlayer().hasPermission("mystictreasures.bypass")) return;

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

                if(e.getPlayer().hasPermission("mystictreasures.bypass")) return;

                e.setCancelled(true);
                e.getPlayer().sendMessage(Messages.get("cannot-place-near-treasure"));
            }

        }


    }

    @EventHandler
    public void onDamage(PlayerBucketEmptyEvent e) {

        if (Hunt.isActive(e.getPlayer().getWorld())) {


            if (Treasure.isNearTreasure(e.getPlayer())) {

                if(e.getPlayer().hasPermission("mystictreasures.bypass")) return;

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

            if (e.getDamager() instanceof Player attacker) {

                boolean itemsAdder = Utils.isEnabled("ItemsAdder");

                if(itemsAdder) {

                    if(Treasure.isTreasure(e.getEntity().getLocation())) {

                        if (CustomEntity.isCustomEntity(e.getEntity())) {
                            e.setCancelled(true);
                        } else if (CustomFurniture.byAlreadySpawned(e.getEntity()) != null) {
                            e.setCancelled(true);
                        }

                    }

                }

                if (e.getEntity() instanceof Player p) {

                    if (Treasure.isNearTreasure(p)) {

                        if(attacker.hasPermission("mystictreasures.bypass")) return;

                        if(!Settings.getWorldBooleanUnknown(p.getWorld().getName(), "allow-pvp-near-treasure")) {

                            e.setCancelled(true);
                            e.setDamage(0);

                            e.getDamager().sendMessage(Messages.get("cannot-attack-near-treasure"));

                        }

                    }


                }
                else if(e.getEntity() instanceof LivingEntity victim)
                {

                    if(e.getFinalDamage() >= victim.getHealth())
                    {

                        Hunt h = Hunt.getHunt(victim.getWorld());
                        Treasure t = h.getTreasure();

                        if (t.isTreasureKeeper(victim)) {

                            if(!t.getParticipants().contains(attacker)) {
                                t.addParticipant(attacker);
                                attacker.sendMessage(Messages.get("participating"));
                            }

                            if(t.remainingMobs() - 1 <= 0)
                            {

                                for(Player p : t.getParticipants()) {
                                    p.sendMessage(Messages.get("participating-cleared-mobs"));
                                }

                            }

                        }
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
    public void onCombust(EntityCombustEvent e) {

        if(Hunt.isActive(e.getEntity().getWorld())) {

            if(e.getEntity() instanceof LivingEntity en)
            {

                try {

                    if (en.getLastDamageCause() != null) {

                        if (en.getLastDamageCause().getDamageSource().getCausingEntity() != null) {

                            if (en.getLastDamageCause().getDamageSource().getCausingEntity() instanceof Player p) {

                                if (p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.FIRE_ASPECT))
                                    return;

                            }

                        }

                    }

                }
                catch (NoSuchMethodError err)
                {}


                Hunt h = Hunt.getHunt(en.getWorld());

                if(h.getTreasure().isTreasureKeeper(en)) {

                    boolean combust = Settings.getWorldBooleanUnknown(en.getWorld().getName(), "protect-mobs-from-sun");

                    if(combust) {

                        e.setCancelled(true);
                        en.setFireTicks(0);
                        en.setVisualFire(false);

                    }
                }

            }

        }

    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        if (Hunt.isActive(e.getPlayer().getWorld())) {

            if(e.getHand() == EquipmentSlot.OFF_HAND) return;

            if (e.getPlayer().getGameMode() != GameMode.SPECTATOR) {

                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                    String worldName = e.getClickedBlock().getWorld().getName();

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


                                    if(Settings.getWorldBooleanUnknown(worldName, "reward-all-players-who-participated"))
                                    {
                                        t.awardPrizes();
                                    }
                                    else
                                    {
                                        t.awardPrize(e.getPlayer());
                                    }

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

                                if(Settings.getWorldBooleanUnknown(worldName, "reward-all-players-who-participated"))
                                {
                                    t.awardPrizes();
                                }
                                else
                                {
                                    t.awardPrize(e.getPlayer());
                                }

                                t.remove();
                                Utils.sendLevelupSound(p);
                            }

                        }


                }
            }
        }

    }

}
