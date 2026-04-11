/*
 * This software is licensed under the PolyForm Noncommercial License 1.0.0.
 * You may obtain a copy of the License at:
 * https://polyformproject.org/licenses/noncommercial/1.0.0
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.Moshu.TreasureHunt.Core.Interaction;

import com.Moshu.Misc.Cooldown;
import com.Moshu.Misc.Storage.Messages;
import com.Moshu.Misc.Storage.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeper;
import com.Moshu.TreasureHunt.Components.Keepers.TreasureKeeperDrops;
import com.Moshu.TreasureHunt.Components.TreasureData;
import com.Moshu.TreasureHunt.Components.TreasureKey;
import com.Moshu.TreasureHunt.Components.TreasureRound;
import com.Moshu.TreasureHunt.Core.API.Events.TreasureInteractEvent;
import com.Moshu.TreasureHunt.Core.Hunt;
import com.Moshu.TreasureHunt.Core.Treasure;
import dev.lone.itemsadder.api.CustomEntity;
import dev.lone.itemsadder.api.CustomFurniture;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.HashMap;

public class TreasureEvents implements Listener {

    private TreasureEvents() {
    }

    private static TreasureEvents instance;

    public static TreasureEvents getInstance() {
        if (instance == null) {
            instance = new TreasureEvents();
            return instance;
        }

        return instance;
    }

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private static final HashMap<Player, Long> click_cooldowns = new HashMap<>();
    HashMap<Player, Long> inventoryClickCooldown = new HashMap<>();

    private boolean disableBreakToAward(Player p, Treasure t) {
        if (t.getTreasureData().getClicksToOpen() > 1) {
            p.sendMessage(Utils.format(Messages.get("only-right-click")));
            return true;
        }

        return false;
    }

    private boolean isCreative(Player p) {
        if (p.getGameMode() != GameMode.SURVIVAL) {
            p.sendMessage(Messages.get("no-creative"));
            p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
            Utils.sendBreakSound(p);
            return true;
        }

        return false;
    }

    private boolean hasCooldown(Player p) {
        if (Cooldown.hasCooldown(p.getUniqueId(), "treasure-winner")) {
            p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
            Utils.sendBreakSound(p);
            p.sendMessage(Messages.get("winner-cooldown").replace("{time}", Utils.formatRemainingTime(Cooldown.getRemainingTimeMinutes(p.getUniqueId(), "treasure-winner"))));
            return true;
        }

        return false;
    }

    private boolean hasKey(Player p, Treasure t) {
        if (t.isLocked()) {

            ItemStack itemInHand = p.getInventory().getItemInMainHand();

            if (!t.getTreasureData().getTreasureKey().isTreasureKey(itemInHand)) {
                p.sendMessage(Messages.get("no-key"));
                Utils.sendBreakSound(p);
                p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
                return false;
            }

        }

        return true;
    }

    private boolean hasRequiredClicks(Player p, Treasure t) {

        int needed_clicks = t.getTreasureData().getClicksToOpen();

        if (t.getCurrentClicks() < needed_clicks) {

            if (System.currentTimeMillis() - click_cooldowns.getOrDefault(p, 0L) < t.getTreasureData().getCooldownBetweenClicks()) {
                return false;
            }

            if (t.getCurrentClicks() == 0) {
                p.sendMessage(Messages.get("starting-clicks")
                        .replace("{current_clicks}", "" + t.getCurrentClicks())
                        .replace("{needed_clicks}", "" + needed_clicks));
            } else {

                p.sendMessage(Messages.get("remaining-clicks")
                        .replace("{current_clicks}", "" + t.getCurrentClicks())
                        .replace("{needed_clicks}", "" + needed_clicks));
            }

            t.incrementCurrentClicks();
            Utils.sendBreakSound(p);

            click_cooldowns.put(p, System.currentTimeMillis());

            return false;
        }

        return true;
    }

    private boolean canTreasureBeOpened(Player p, Treasure t) {
        if (!t.timePassedBeforePickup()) {
            p.sendMessage(Messages.get("minutes-before-pickup-not-passed").replace("{time}",
                    Utils.formatRemainingTime(t.getTreasureData().getMilliesBeforePickup() - t.getHunt().getElapsedTime())));
            Utils.sendBreakSound(p);
            p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
            return false;
        }

        return true;
    }

    private void sendMobsNotCleared(Player p) {
        for (String s : Messages.getAndFormatList("messages.need-to-kill-all-mobs")) {
            p.sendMessage(s);
        }

        p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
        Utils.sendBreakSound(p);
    }

    private void sendTreasureUnlocked(Player p) {
        for (String s : Messages.getAndFormatList("messages.unlocked-treasure")) {
            p.sendMessage(s);
        }
    }

    private void awardAndRemove(Player p, Treasure t) {
        if (t.getRoundController() != null) {
            int currentRoundIdx = t.getRoundController().getRoundNumber() - 1;
            TreasureRound currentRound = t.getRoundController().getRoundRegistry().getRound(currentRoundIdx);
            if (currentRound != null && currentRound.getRoundData() != null) {
                t.giveRewards(currentRound.getRoundData());
            }

            // Only remove and end if it's the last round
            if (t.getRoundController().isLastRound()) {
                t.remove(true);
                Utils.sendLevelupSound(p);
            }
        } else {
            // Fallback for safety
            t.remove(true);
            Utils.sendLevelupSound(p);
        }
    }

    private void rewardHandler(Player p, Treasure t) {
        TreasureRound currentRound = null;
        if (t.getRoundController() != null) {
            int idx = t.getRoundController().getRoundNumber() - 1;
            currentRound = t.getRoundController().getRoundRegistry().getRound(idx);
        }
        
        boolean isChest = false;
        if (currentRound != null && currentRound.getRoundData() != null) {
            isChest = currentRound.getRoundData().getAwardMethod() == Treasure.AwardMethod.CHEST;
        }

        if (isChest) {
            if (t.getRewardInventory() == null) {
                 awardAndRemove(p, t);
                 return;
            }

            if (t.getRewardInventory().getViewers().size() >= Settings.getMaxPlayersLooting()) {
                p.sendMessage(Messages.get("max-players-looting"));
                return;
            }

            if (Utils.hasFullInventory(p)) {
                p.sendMessage(Messages.get("full-inventory-opening-treasure"));
                return;
            }

            if (Cooldown.hasCooldown(p.getUniqueId(), "treasure-winner")) {
                p.sendMessage(Messages.get("winner-cooldown").replace("{time}", Utils.formatRemainingTime(Cooldown.getRemainingTimeMinutes(p.getUniqueId(), "treasure-winner"))));
                return;
            }

            if (t.isFirstOpen()) {
                t.announceWinner(p);
                t.setFirstOpen(false);
            }

            if (!t.receivedCommandRewards(p)) {
                // We'll use the round-specific command prizes when opening the chest
                if (currentRound != null && currentRound.getRoundData() != null) {
                    t.runCommandPrizes(p, currentRound.getRoundData());
                }
            }

            p.openInventory(t.getRewardInventory());
        } else {
            awardAndRemove(p, t);
        }
    }

    private void openTreasure(Player p, Treasure t) {

        if (t.isLocked()) {
            Utils.substractItem(p, t.getTreasureData().getTreasureKey().getItemStack(1), 1);
            sendTreasureUnlocked(p);
            t.unlock();
        }

        if (t.getTreasureData().requireAllMobsDead()) {

            if (t.mobsCleared()) rewardHandler(p, t);
            else sendMobsNotCleared(p);

        } else {

            rewardHandler(p, t);

        }
    }

    @EventHandler
    public void onDamage(BlockPlaceEvent e) {

        if (!Settings.getBoolean("disable-griefing-protection")) {

            if (Treasure.isNearTreasure(e.getPlayer())) {

                if (e.getPlayer().hasPermission("mystictreasures.bypass")) return;

                e.setCancelled(true);
                e.getPlayer().sendMessage(Messages.get("cannot-place-near-treasure"));
            }
        }

    }

    @EventHandler
    public void onDamage(PlayerBucketEmptyEvent e) {

        if (!Settings.getBoolean("disable-griefing-protection")) {

            if (Treasure.isNearTreasure(e.getPlayer())) {

                if (e.getPlayer().hasPermission("mystictreasures.bypass")) return;

                e.setCancelled(true);
                e.getPlayer().sendMessage(Messages.get("cannot-place-near-treasure"));

            }
        }

    }

    @EventHandler
    public void onExplode(BlockExplodeEvent e) {


        for (Block b : e.blockList()) {

            if (Treasure.isTreasure(b.getLocation())) {
                e.setCancelled(true);
            }

        }


    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {

        for (Block b : e.blockList()) {

            if (Treasure.isTreasure(b.getLocation())) {
                e.setCancelled(true);
            }

        }


    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {

        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();

            if (Treasure.isNearTreasure(p)) {
                p.closeInventory();
            }

        }

        if (e.getDamager() instanceof Player) {

            Player attacker = (Player) e.getDamager();
            boolean itemsAdder = Utils.isEnabled("ItemsAdder");

            if (itemsAdder) {

                if (Treasure.isTreasure(e.getEntity().getLocation())) {

                    if (CustomEntity.isCustomEntity(e.getEntity())) {
                        e.setCancelled(true);
                    } else if (CustomFurniture.byAlreadySpawned(e.getEntity()) != null) {
                        e.setCancelled(true);
                    }

                }

            }

            if (e.getEntity() instanceof Player) {

                Player p = (Player) e.getEntity();

                if (Treasure.isNearTreasure(p)) {

                    if (attacker.hasPermission("mystictreasures.bypass")) return;

                    if (!Settings.getBoolean("allow-pvp-near-treasure")) {

                        e.setCancelled(true);
                        e.setDamage(0);

                        e.getDamager().sendMessage(Messages.get("cannot-attack-near-treasure"));

                    }

                }


            } else if (e.getEntity() instanceof LivingEntity) {

                LivingEntity victim = (LivingEntity) e.getEntity();

                if (e.getFinalDamage() >= victim.getHealth()) {

                    if (TreasureKeeper.isTreasureKeeper(victim)) {

                        Hunt h = TreasureKeeper.getHunt(victim);
                        if (h == null) return;

                        Treasure t = h.getTreasure();
                        if (t == null) return;

                        if (!t.getParticipants().contains(attacker)) {
                            t.addParticipant(attacker);
                            attacker.sendMessage(Messages.get("participating"));
                        }

                        if (t.remainingMobs() - 1 <= 0) {

                            for (Player p : t.getParticipants()) {
                                p.sendMessage(Messages.get("participating-cleared-mobs"));
                            }

                        }

                        t.addDamageGiven(attacker, e.getFinalDamage());

                    }
                }

            }

        }

        if (e.getDamager() instanceof Projectile) {

            Projectile projectile = (Projectile) e.getDamager();
            if (projectile.getShooter() == null) return;

            if (projectile.getShooter() instanceof Player) {

                Player p = (Player) projectile.getShooter();

                if (e.getEntity() instanceof Player) {

                    Player victim = (Player) e.getEntity();

                    if (Treasure.isNearTreasure(victim)) {

                        if (p.hasPermission("mystictreasures.bypass")) return;

                        if (!Settings.getBoolean("allow-pvp-near-treasure")) {

                            e.setCancelled(true);
                            e.setDamage(0);

                            e.getDamager().sendMessage(Messages.get("cannot-attack-near-treasure"));

                        }

                    }


                } else if (e.getEntity() instanceof LivingEntity) {

                    LivingEntity victim = (LivingEntity) e.getEntity();

                    if (e.getFinalDamage() >= victim.getHealth()) {

                        if (TreasureKeeper.isTreasureKeeper(victim)) {

                            Hunt h = TreasureKeeper.getHunt(victim);

                            if (h == null) return;
                            Treasure t = h.getTreasure();
                            if (t == null) return;

                            if (!t.getParticipants().contains(p)) {
                                t.addParticipant(p);
                                p.sendMessage(Messages.get("participating"));
                            }

                            if (t.remainingMobs() - 1 <= 0) {

                                for (Player k : t.getParticipants()) {
                                    k.sendMessage(Messages.get("participating-cleared-mobs"));
                                }

                            }

                            t.addDamageGiven(p, e.getFinalDamage());

                        }
                    }

                }

            }

        }


    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        if (e.getTo() == null) return;

        if (Hunt.huntActiveInWorld(e.getPlayer().getWorld())) {

            if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()) {

                if (Treasure.isNearTreasure(e.getPlayer())) {

                    if (e.getPlayer().isFlying() && !e.getPlayer().hasPermission("mystictreasures.bypass")) {

                        if (!Settings.getBoolean("allow-flight-near-treasure")) {

                            e.getPlayer().setFlying(false);
                            e.getPlayer().setAllowFlight(false);
                            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,
                                    Settings.getSlowFallingDuration(), Settings.getSlowFallingLevel()));

                        }

                    }

                    if (e.getPlayer().isInvulnerable() && !e.getPlayer().hasPermission("mystictreasures.bypass")) {

                        if (!Settings.getBoolean("allow-god-near-treasure")) {

                            e.getPlayer().setInvulnerable(false);

                        }
                    }

                    if (e.getPlayer().isGliding() && !e.getPlayer().hasPermission("mystictreasures.bypass")) {

                        if (!Settings.getBoolean("allow-elytra-near-treasure")) {

                            e.getPlayer().setGliding(false);
                            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,
                                    Settings.getSlowFallingDuration(), Settings.getSlowFallingLevel()));

                        }

                    }

                }


            }

        }

    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {

        Player p = e.getPlayer();

        if (p.hasPermission("mystictreasures.bypass")) return;

        if (Treasure.isNearTreasure(p)) {

            String[] words = e.getMessage().split(" ");
            String command = words[0].trim().toLowerCase().substring(1);

            if (Settings.getBlacklistedCommands().contains(command)) {
                e.setCancelled(true);
                p.sendMessage(Messages.get("blacklisted-command"));
            }
        }

    }

    @EventHandler
    public void onDebug(PlayerInteractEntityEvent e) {

        if (TreasureCommands.isDebugging(e.getPlayer())) {

            if (e.getHand() == EquipmentSlot.HAND) {

                if (e.getRightClicked() instanceof ArmorStand) {

                    ArmorStand as = (ArmorStand) e.getRightClicked();

                    e.setCancelled(true);
                    Player p = e.getPlayer();

                    as.setVisible(true);
                    as.setGlowing(true);

                    p.sendMessage(Utils.format("&6&lTreasure&e&lHunt &fDebug"));
                    p.sendMessage(" ");
                    p.sendMessage(" - Gravity: " + as.hasGravity());
                    p.sendMessage(" - Metadata: " + as.getMetadata("treasure_stand"));
                    p.sendMessage(" - On ground: " + as.isOnGround());
                    p.sendMessage(" - Velocity: " + as.getVelocity());

                    if (Utils.isPaper()) {
                        p.sendMessage(" - Physics: " + as.hasNoPhysics());
                        p.sendMessage(" - Can move: " + as.canMove());
                        p.sendMessage(" - Can tick: " + as.canMove());
                    }

                    p.sendMessage(" ");
                    p.sendMessage(" *The ArmorStand will be removed in 1 minute");

                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                    {
                        as.remove();
                    }, 1200);

                }

            }

        }

    }

    @EventHandler
    public void onCombust(EntityCombustEvent e) {

        if (Hunt.huntActiveInWorld(e.getEntity().getWorld())) {

            if (e.getEntity() instanceof LivingEntity) {

                LivingEntity en = (LivingEntity) e.getEntity();

                try {

                    if (en.getLastDamageCause() != null) {

                        if (en.getLastDamageCause().getDamageSource().getCausingEntity() != null) {

                            if (en.getLastDamageCause().getDamageSource().getCausingEntity() instanceof Player) {

                                Player p = (Player) en.getLastDamageCause().getDamageSource().getCausingEntity();

                                if (p.getInventory().getItemInMainHand().containsEnchantment(Enchantment.FIRE_ASPECT))
                                    return;

                            }

                        }

                    }

                } catch (NoSuchMethodError err) {
                }


                if (TreasureKeeper.isTreasureKeeper(en)) {

                    boolean combust = Settings.getBoolean("protect-mobs-from-sun");

                    if (combust) {

                        e.setCancelled(true);
                        en.setFireTicks(0);
                        en.setVisualFire(false);

                    }
                }

            }

        }

    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {

        if (TreasureKeeper.isTreasureKeeper(e.getEntity())) {

            LivingEntity entity = e.getEntity();
            Hunt h = TreasureKeeper.getHunt(entity);

            if (h == null) {
                plugin.getLogger().warning("Entity drops couldn't be handled - the hunt is null");
                return;
            }

            TreasureKeeper t = h.getTreasure().getTreasureKeeper(entity);
            if (t == null) return;

            TreasureKeeperDrops d = t.getDrops();
            e.getDrops().clear();

            for (TreasureKeeperDrops.DropData data : d.getAllDrops().values()) {

                if (Utils.chance() < data.getChance()) {
                    Item i = entity.getWorld().dropItemNaturally(entity.getLocation(), data.getItemStack());

                    if (!data.getName().equalsIgnoreCase("none")) {
                        i.setCustomName(Utils.format(data.getName()));
                        i.setCustomNameVisible(true);
                    }

                }

            }

        }

    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {

        try {

            Object view = InventoryDragEvent.class.getMethod("getView").invoke(e);

            Method getTitle = view.getClass().getMethod("getTitle");
            getTitle.setAccessible(true);
            String title = (String) getTitle.invoke(view);

            if (title.equals(Messages.get("treasure-reward-menu-title"))) {
                e.setCancelled(true);
            }

        } catch (Exception ex) {
            plugin.getLogger().severe("Could not use reflection for InventoryDragEvent on version " + Bukkit.getBukkitVersion() + ", " + Bukkit.getMinecraftVersion());
            ex.printStackTrace();
        }

    }


    @EventHandler
    public void inventoryClick(InventoryClickEvent e) {

        if (e.getClickedInventory() == null) return;

        try {

            Object view = InventoryClickEvent.class.getMethod("getView").invoke(e);

            Method getTitle = view.getClass().getMethod("getTitle");
            getTitle.setAccessible(true);
            String title = (String) getTitle.invoke(view);

            Player p = (Player) e.getWhoClicked();

            if (title.equals(Messages.get("active-hunts-menu.title"))) {
                e.setCancelled(true);
            }

            if (title.equals(Messages.get("treasure-reward-menu-title"))) {

                if (System.currentTimeMillis() - inventoryClickCooldown.getOrDefault(p, 0L) < Settings.getInventoryClickCooldown()) {
                    p.sendMessage(Messages.get("clicking-too-fast"));
                    e.setCancelled(true);
                    return;
                }

                inventoryClickCooldown.put(p, System.currentTimeMillis());

                Method getTopInventoryMethod = view.getClass().getMethod("getTopInventory");
                getTopInventoryMethod.setAccessible(true);
                Object topInventory = getTopInventoryMethod.invoke(view);

                Method getClickedInventoryMethod = InventoryClickEvent.class.getMethod("getClickedInventory");
                getClickedInventoryMethod.setAccessible(true);
                Object clickedInventory = getClickedInventoryMethod.invoke(e);

                String actionName = e.getAction().name();
                boolean isTaking = actionName.startsWith("PICKUP") || actionName.equals("MOVE_TO_OTHER_INVENTORY");

                if (!isTaking && clickedInventory.equals(topInventory)) {
                    e.setCancelled(true);
                }

                boolean isShiftClickIntoTop = actionName.equals("MOVE_TO_OTHER_INVENTORY") && e.getRawSlot() >= 54;
                if (isShiftClickIntoTop) {
                    e.setCancelled(true);
                }


            }

        } catch (Exception ex) {
            plugin.getLogger().severe("Could not use reflection for InventoryClickEvent on version " + Bukkit.getBukkitVersion() + ", " + Bukkit.getMinecraftVersion());
            ex.printStackTrace();
        }
    }

    /**
     * Handler for interaction with the treasure in oreder to generalize this interaction
     * with external plugins too.
     *
     * @param o the location of the block or entity that the player interacts with
     * @return true or false depending if the interaction was actually with a treasure or not
     */
    public boolean handleInteraction(Player p, Location o) {
        if (o == null || o.getWorld() == null) return false;
        Location l = new Location(o.getWorld(), o.getBlockX(), o.getBlockY(), o.getBlockZ());

        if (Treasure.isTreasure(o)) {

            Treasure t = Treasure.getTreasure(l);
            if (t == null) return false;

            TreasureInteractEvent claimEvent = new TreasureInteractEvent(t, p);
            Bukkit.getPluginManager().callEvent(claimEvent);

            if (claimEvent.isCancelled()) return true;

            if (hasCooldown(p)) return true;
            if (!canTreasureBeOpened(p, t)) return true;
            
            // KEY REQUIREMENT: Must have key to even start the first round
            if (t.isLocked()) {
                if (!hasKey(p, t)) return true;
                
                // Unlock immediately on first interaction if they have the key
                Utils.substractItem(p, t.getTreasureData().getTreasureKey().getItemStack(1), 1);
                sendTreasureUnlocked(p);
                t.unlock();
            }

            // Rounds logic
            if (t.getRoundController() != null && t.getRoundController().hasMoreRounds()) {
                if (t.getRoundController().startRound()) {
                    // New round started successfully
                    return true;
                } else {
                    // Could not start new round, likely because mobs aren't cleared
                    if (t.remainingMobs() > 0) {
                        sendMobsNotCleared(p);
                        return true;
                    }
                }
            }

            if (!hasRequiredClicks(p, t)) return true;

            // If all rounds are finished and all conditions are met, open the treasure
            openTreasure(p, t);
            return true;

        }

        return false;

    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        if (Hunt.huntActiveInWorld(e.getPlayer().getWorld())) {

            Location o = e.getClickedBlock().getLocation();
            boolean isTreasure = handleInteraction(e.getPlayer(), o);

            e.setCancelled(isTreasure);

        }

        if (TreasureKey.isKey(e.getPlayer().getInventory().getItemInMainHand())) {
            e.getPlayer().sendMessage(Messages.get("interact-with-key"));
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {

        if (Treasure.isTreasure(e.getBlock().getLocation())) {

            e.setCancelled(true);

            Location o = e.getBlock().getLocation();
            Location l = new Location(o.getWorld(), o.getBlockX(), o.getBlockY(), o.getBlockZ());
            Player p = e.getPlayer();

            Treasure t = Treasure.getTreasure(l);

            TreasureInteractEvent claimEvent = new TreasureInteractEvent(t, p);
            Bukkit.getPluginManager().callEvent(claimEvent);

            if (claimEvent.isCancelled()) return;

            if (isCreative(p)) return;

            if (disableBreakToAward(p, t)) return;
            if (hasCooldown(p)) return;
            if (!canTreasureBeOpened(p, t)) return;
            
            // KEY REQUIREMENT: Must have key to even interact
            if (t.isLocked()) {
                if (!hasKey(p, t)) return;
                
                Utils.substractItem(p, t.getTreasureData().getTreasureKey().getItemStack(1), 1);
                sendTreasureUnlocked(p);
                t.unlock();
            }
            
            // Respect rounds in break event too
            if (t.getRoundController() != null && t.getRoundController().hasMoreRounds()) {
                if (t.getRoundController().startRound()) {
                    return;
                } else if (t.remainingMobs() > 0) {
                    sendMobsNotCleared(p);
                    return;
                }
            }

            openTreasure(p, t);

        } else {

            if (!Settings.getBoolean("disable-griefing-protection")) {

                if (Treasure.isNearTreasure(e.getPlayer())) {

                    if (e.getPlayer().hasPermission("mystictreasures.bypass")) return;

                    e.setCancelled(true);
                    e.getPlayer().sendMessage(Messages.get("cannot-break-near-treasure"));

                }

            }

        }


    }


    @EventHandler(ignoreCancelled = true)
    public void onSuffocation(EntityDamageEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.SUFFOCATION) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;

        LivingEntity victim = (LivingEntity) e.getEntity();
        if (!TreasureKeeper.isTreasureKeeper(victim)) return;

        Hunt h = TreasureKeeper.getHunt(victim);
        if (h == null || h.getTreasure() == null) return;

        Treasure t = h.getTreasure();
        TreasureData data = t.getTreasureData();

        boolean spawnsInside = data.isSpawnsInside() || data.spawnToCertainCoords();
        Location treasureLoc = t.getLocation();
        
        // Find a new safe location within wandering distance
        int radius = data.getMobWanderingDistance();
        Location newLoc;
        
        if (spawnsInside) {
            newLoc = Utils.getNearLocationInside(treasureLoc, radius);
        } else {
            newLoc = Utils.getNearLocation(treasureLoc, radius);
        }
        
        victim.teleport(newLoc);
        e.setCancelled(true);
    }

}
