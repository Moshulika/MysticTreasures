package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Cooldown;
import com.Moshu.Misc.Messages;
import com.Moshu.Misc.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.objects.TreasureKeeper;
import com.Moshu.TreasureHunt.objects.TreasureKeeperDrops;
import dev.lone.itemsadder.api.CustomEntity;
import dev.lone.itemsadder.api.CustomFurniture;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

public class TreasureEvents implements Listener {

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    @EventHandler
    public void onBreak(BlockBreakEvent e) {

        if (Treasure.isTreasure(e.getBlock().getLocation())) {

            e.setCancelled(true);

            String worldName = e.getBlock().getWorld().getName();
            Location o = e.getBlock().getLocation();
            Location l = new Location(o.getWorld(), o.getBlockX(), o.getBlockY(), o.getBlockZ());
            Player p = e.getPlayer();

            Treasure t = Treasure.getTreasure(l);

            if (p.getGameMode() != GameMode.SURVIVAL) {
                p.sendMessage(Messages.get("no-creative"));
                p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
                Utils.sendBreakSound(e.getPlayer());
                return;
            }

            if (Cooldown.hasCooldown(p.getUniqueId(), "treasure-winner")) {
                p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
                Utils.sendBreakSound(e.getPlayer());
                p.sendMessage(Messages.get("winner-cooldown").replace("{time}", Utils.formatRemainingTime(Cooldown.getRemainingTimeMinutes(p.getUniqueId(), "treasure-winner"))));
                return;
            }

            if (t.getTreasureData().getTreasureKey().requiresKey()) {
                p.sendMessage(Messages.get("no-key"));
                p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
                Utils.sendBreakSound(e.getPlayer());
                return;
            }

            if(!t.timePassedBeforePickup())
            {
                p.sendMessage(Messages.get("minutes-before-pickup-not-passed").replace("{time}",
                        Utils.formatRemainingTime(t.getTreasureData().getMilliesBeforePickup() - t.getHunt().getElapsedTime())));
                Utils.sendBreakSound(p);
                p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
                return;
            }

            if (t.getTreasureData().requireAllMobsDead()) {


                if (t.mobsCleared()) {

                    if (t.getTreasureData().rewardAllPlayersWhoParticipated()) {
                        t.awardPrizes();
                    }
                    else if(t.getTreasureData().rewardMostDamageGiven())
                    {

                        if(t.wereTreasureKeepersDamaged())
                        {
                            t.awardPrize(t.getPlayerWithMostDamage());
                        }
                        else
                        {
                            t.awardPrize(e.getPlayer());
                        }

                    } else {
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

                if (t.getTreasureData().rewardAllPlayersWhoParticipated()) {
                    t.awardPrizes();
                }
                else if(t.getTreasureData().rewardMostDamageGiven())
                {

                    if(t.wereTreasureKeepersDamaged())
                    {
                        t.awardPrize(t.getPlayerWithMostDamage());
                    }
                    else
                    {
                        t.awardPrize(e.getPlayer());
                    }

                } else {
                    t.awardPrize(e.getPlayer());
                }

                t.remove();
                Utils.sendLevelupSound(p);
            }

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
                        if(h == null) return;

                        Treasure t = h.getTreasure();

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
            if(projectile.getShooter() == null) return;

            if(projectile.getShooter() instanceof Player)
            {

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

                            if(h == null) return;
                            Treasure t = h.getTreasure();

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

        if (Hunt.huntActiveInWorld(e.getPlayer().getWorld()))
        {

            if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()) {

                if (Treasure.isNearTreasure(e.getPlayer())) {

                    if (e.getPlayer().isFlying() && !e.getPlayer().hasPermission("mystictreasures.bypass")) {

                        if(!Settings.getBoolean("allow-flight-near-treasure")) {

                            e.getPlayer().setFlying(false);
                            e.getPlayer().setAllowFlight(false);
                            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,
                                    Settings.getSlowFallingDuration(), Settings.getSlowFallingLevel()));

                        }

                    }

                    if (e.getPlayer().isInvulnerable() && !e.getPlayer().hasPermission("mystictreasures.bypass")) {

                        if(!Settings.getBoolean("allow-god-near-treasure")) {

                            e.getPlayer().setInvulnerable(false);

                        }
                    }

                    if(e.getPlayer().isGliding() && !e.getPlayer().hasPermission("mystictreasures.bypass"))
                    {

                        if(!Settings.getBoolean("allow-elytra-near-treasure")) {

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
    public void onCommand(PlayerCommandPreprocessEvent e)
    {

        Player p = e.getPlayer();

        if(p.hasPermission("mystictreasures.bypass")) return;

        if(Treasure.isNearTreasure(p)) {

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

                    if(Utils.isPaper())
                    {
                        p.sendMessage(" - Physics: " + as.hasNoPhysics());
                        p.sendMessage(" - Can move: " + as.canMove());
                        p.sendMessage(" - Can tick: " + as.canMove());
                    }

                    p.sendMessage(" ");
                    p.sendMessage(" *The ArmorStand will be removed in 1 minute");

                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->
                    {
                        as.remove();
                    }, 1200);

                }

            }

        }

    }

    @EventHandler
    public void onCombust(EntityCombustEvent e) {

        if(Hunt.huntActiveInWorld(e.getEntity().getWorld())) {

            if(e.getEntity() instanceof LivingEntity)
            {

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

                }
                catch (NoSuchMethodError err)
                {
                }



                if(TreasureKeeper.isTreasureKeeper(en)) {

                    boolean combust = Settings.getBoolean("protect-mobs-from-sun");

                    if(combust) {

                        e.setCancelled(true);
                        en.setFireTicks(0);
                        en.setVisualFire(false);

                    }
                }

            }

        }

    }

    private static final HashMap<Treasure, HashMap<Player, Integer>> clicks = new HashMap<>();

    private HashMap<Player, Integer> getHuntClicks(Treasure t)
    {
        clicks.putIfAbsent(t, new HashMap<>());
        return clicks.get(t);
    }

    private int getClicks(Treasure t, Player p)
    {
        return getHuntClicks(t).get(p) == null ? 1 : getHuntClicks(t).get(p);
    }

    private void addClicks(Treasure t, Player p)
    {
        getHuntClicks(t).put(p, getClicks(t, p) + 1);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e)
    {

        if(TreasureKeeper.isTreasureKeeper(e.getEntity()))
        {

                LivingEntity entity = e.getEntity();
                Hunt h = TreasureKeeper.getHunt(entity);

                if(h == null)
                {
                    plugin.getLogger().warning("Entity drops couldn't be handled - the hunt is null");
                    return;
                }

                TreasureKeeper t = h.getTreasure().getTreasureKeeper(entity);
                if(t == null) return;

                TreasureKeeperDrops d = t.getDrops();
                e.getDrops().clear();

                for(TreasureKeeperDrops.DropData data : d.getAllDrops().values())
                {

                    if(Utils.chance() < data.getChance())
                    {
                        Item i = entity.getWorld().dropItemNaturally(entity.getLocation(), data.getItemStack());

                        if(!data.getName().equalsIgnoreCase("none"))
                        {
                            i.setCustomName(Utils.format(data.getName()));
                            i.setCustomNameVisible(true);
                        }

                    }

                }

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

            if (title.equals(Messages.get("active-hunts-menu.title"))) {
                e.setCancelled(true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        if (Hunt.huntActiveInWorld(e.getPlayer().getWorld())) {

            if(e.getHand() == EquipmentSlot.OFF_HAND) return;

            if (e.getPlayer().getGameMode() != GameMode.SPECTATOR) {

                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

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

                            Treasure t = Treasure.getTreasure(l);
                            int needed_clicks = t.getTreasureData().getClicksToOpen();

                            if(t.getTreasureData().getTreasureKey().requiresKey())
                            {

                                ItemStack itemInHand = p.getInventory().getItemInMainHand();

                                if(!t.getTreasureData().getTreasureKey().isTreasureKey(itemInHand))
                                {
                                    p.sendMessage(Messages.get("no-key"));
                                    Utils.sendBreakSound(e.getPlayer());
                                    p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
                                    return;
                                }

                            }

                            if(getClicks(t, p) < needed_clicks) {
                                p.sendMessage(Messages.get("remaining-clicks")
                                        .replace("{current_clicks}", "" + getClicks(t, p))
                                        .replace("{needed_clicks}", "" + needed_clicks));
                                addClicks(t, p);
                                Utils.sendBreakSound(p);
                                return;
                            }

                            if(!t.timePassedBeforePickup())
                            {
                                p.sendMessage(Messages.get("minutes-before-pickup-not-passed").replace("{time}",
                                        Utils.formatRemainingTime(t.getTreasureData().getMilliesBeforePickup() - t.getHunt().getElapsedTime())));
                                Utils.sendBreakSound(p);
                                p.setVelocity(p.getLocation().getDirection().multiply(-1).setY(1));
                                return;
                            }

                            if (t.getTreasureData().requireAllMobsDead()) {


                                if (t.mobsCleared()) {

                                    if (t.getTreasureData().getTreasureKey().requiresKey()) {
                                        Utils.substractItem(p, t.getTreasureData().getTreasureKey().getTreasureKey(1), 1);
                                    }

                                    if (t.getTreasureData().rewardAllPlayersWhoParticipated()) {
                                        t.awardPrizes();
                                    }
                                    else if(t.getTreasureData().rewardMostDamageGiven())
                                    {

                                        if(t.wereTreasureKeepersDamaged())
                                        {
                                            t.awardPrize(t.getPlayerWithMostDamage());
                                        }
                                        else
                                        {
                                            t.awardPrize(e.getPlayer());
                                        }

                                    } else {
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

                                if (t.getTreasureData().getTreasureKey().requiresKey()) {
                                    Utils.substractItem(p, t.getTreasureData().getTreasureKey().getTreasureKey(1), 1);
                                }

                                if(t.getTreasureData().rewardAllPlayersWhoParticipated())
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
