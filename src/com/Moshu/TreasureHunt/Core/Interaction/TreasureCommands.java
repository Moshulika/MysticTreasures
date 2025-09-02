package com.Moshu.TreasureHunt.Core.Interaction;

import com.Moshu.Main;
import com.Moshu.Misc.Cooldown;
import com.Moshu.Misc.Storage.Messages;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Components.TreasureData;
import com.Moshu.TreasureHunt.Core.Hunt;
import com.Moshu.TreasureHunt.Core.Treasure;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class TreasureCommands implements CommandExecutor {

    private static Main plugin;

    public TreasureCommands(Main plugin) {
        TreasureCommands.plugin = plugin;
    }

    private static final ArrayList<Player> debugging = new ArrayList<Player>();

    public static boolean isDebugging(Player p) {
        return debugging.contains(p);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("hunt")) {

            if (args.length == 0) {

                if (sender instanceof Player) {

                    Player p = (Player) sender;

                    if (p.hasPermission("mystictreasures.hunt")) {

                        if (Hunt.getActiveTreasures().isEmpty()) {
                            p.sendMessage(Messages.get("no-hunt-in-this-world"));
                        } else {
                            Hunt.activeHuntsMenu(p);
                        }

                        return true;
                    } else {
                        p.sendMessage(Messages.get("no-permission"));
                    }

                } else {

                    Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fHunts are active in the following worlds:"));
                    Bukkit.getConsoleSender().sendMessage(Utils.format("&5&lMystic&d&lTreasures: &fUse /hunt start/stop (Identifier) to control the hunts"));

                    int x = 1;
                    Treasure t;

                    for (Hunt h : Hunt.getActiveTreasures()) {

                        t = h.getTreasure();

                        Bukkit.getConsoleSender().sendMessage(Utils.format("&5&l" + x + ". &f" + t.getTreasureData().getIdentifier() + " @ " +
                                t.getLocation().getWorld().getName() + ", X:" + t.getLocation().getBlockX() + ", Z:" + t.getLocation().getBlockZ() +
                                ", remaining time: " + Utils.formatRemainingTime(h.getRemainingTime())));
                        x++;
                    }

                }

            } else if (args.length == 1) {

                if (args[0].equalsIgnoreCase("start")) {

                    sender.sendMessage(Messages.get("wrong-command"));

                } else if (args[0].equalsIgnoreCase("stop")) {

                    sender.sendMessage(Messages.get("wrong-command"));


                }
                else if(args[0].equalsIgnoreCase("showcase"))
                {

                    if (sender instanceof Player) {

                        Player p = (Player) sender;
                        TreasureMenu.showcase(p);
                    }
                    else
                    {
                        Utils.sendNotPlayer();
                    }

                }
                else if (args[0].equalsIgnoreCase("debug")) {

                    if (sender instanceof Player) {

                        Player p = (Player) sender;

                        if(!p.hasPermission("mystictreasures.admin"))
                        {
                            p.sendMessage(Messages.get("no-permission"));
                            return true;
                        }

                        if (debugging.contains(p)) {
                            p.sendMessage(Utils.format("&6&lTreasure&e&lHunt &fYou've stopped debugging"));
                            debugging.remove(p);
                        } else {
                            p.sendMessage(Utils.format("&6&lTreasure&e&lHunt &fYou started debugging. Right click an ArmorStand (the falling treasure) to gain more information and remove it"));
                            debugging.add(p);
                        }

                    } else {
                        Utils.sendNotPlayer();
                    }

                } else if (args[0].equalsIgnoreCase("reload")) {

                    if(sender instanceof Player) {

                        if(!sender.hasPermission("mystictreasures.admin"))
                        {
                            sender.sendMessage(Messages.get("no-permission"));
                            return true;
                        }

                    }

                    if(!Hunt.getHunts().isEmpty())
                    {
                        sender.sendMessage(Messages.get("reload-with-hunt-active"));
                        return true;
                    }

                    sender.sendMessage(Messages.get("config-reload"));
                    plugin.reloadFiles();
                    TreasureData.reload();

                } else if (args[0].equalsIgnoreCase("help")) {

                    sender.sendMessage(Utils.format("&6&lTreasure&e&lHunt &fHelp page"));
                    sender.sendMessage(" ");
                    sender.sendMessage(Utils.format("  &6/hunt start (Identifier) &8(&fStarts a hunt at a random location&8)"));
                    sender.sendMessage(Utils.format("  &6/hunt stop (Identifier) &8(&fStops the hunt&8)"));
                    sender.sendMessage(Utils.format("  &6/hunt start &ehere (Identifier) &8(&fStarts a hunt at the player's location&8)"));
                    sender.sendMessage(Utils.format("  &6/hunt reload &8(&fReloads the config & messages - not all config values can be reloaded&8)"));
                    sender.sendMessage(Utils.format("  &6/hunt clear (Player) &8(&fClears a player's winner cooldown&8)"));
                    sender.sendMessage(Utils.format("  &6/hunt key (Player) (Identifier) [Amount] &8(&fGives a player a key for that treasure&8)"));
                    sender.sendMessage(Utils.format("  &6/hunt showcase &8(&fOpen the treasure showcase menu&8)"));
                    sender.sendMessage(Utils.format("  &6/hunt debug &8(&fEnter debug mode&8)"));
                    sender.sendMessage(" ");

                } else if (args[0].equalsIgnoreCase("key")) {
                    sender.sendMessage(Messages.get("wrong-command"));
                } else {
                    sender.sendMessage(Messages.get("wrong-command"));
                }

                return true;
            } else if (args.length == 2) {

                if (sender instanceof Player) {

                    Player p = (Player) sender;

                    if (!p.hasPermission("mystictreasures.admin")) {
                        p.sendMessage(Messages.get("no-permission"));
                        return true;
                    }

                }

                if (args[0].equalsIgnoreCase("start")) {

                    if (TreasureData.getTreasureIdentifiers().contains(args[1])) {

                        String id = args[1];

                        if (Hunt.isHuntActive(id)) {
                            sender.sendMessage(Messages.get("hunt-already-active"));
                            return true;
                        }

                        Hunt h = new Hunt(id, TreasureData.getByIdentifier(id).getDuration());
                        sender.sendMessage(Messages.get("generating-treasure"));

                        CompletableFuture<Hunt> started = new CompletableFuture<>();

                        h.getLocationReadyFuture()
                                .thenRun(() -> {

                                    // Ensure start() runs on the main server thread
                                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                                        try {

                                            sender.sendMessage(Messages.get("treasure-generated"));

                                            h.start();
                                            started.complete(h);
                                        } catch (Throwable t) {
                                            started.completeExceptionally(t);
                                        }
                                    });
                                })
                                .exceptionally(ex -> {
                                    plugin.getLogger().severe("Failed to start treasure hunt with id '" + id + "': " + ex.getMessage());
                                    started.completeExceptionally(ex);
                                    return null;
                                });

                    } else {
                        sender.sendMessage(Messages.get("inexistent-treasure"));
                    }

                } else if (args[0].equalsIgnoreCase("stop")) {

                    if (TreasureData.getTreasureIdentifiers().contains(args[1])) {

                        String id = args[1];

                        if (!Hunt.isHuntActive(id)) {
                            sender.sendMessage(Messages.get("hunt-not-active"));
                            return true;
                        }

                        //Treasure is not yet spawned, so block stopping the treasure because it will cause bugs.
                        if(!Hunt.getHuntByIdentifier(id).getTreasure().isActive())
                        {
                            sender.sendMessage(Messages.get("hunt-not-active"));
                            return true;
                        }

                        Hunt.getHuntByIdentifier(id).stop();
                        sender.sendMessage(Messages.get("hunt-stopped"));

                    } else {
                        sender.sendMessage(Messages.get("inexistent-treasure"));
                    }

                } else if (args[0].equalsIgnoreCase("clear")) {

                    if (Bukkit.getPlayer(args[1]) == null) {
                        sender.sendMessage(Messages.get("player-not-found"));
                        return true;
                    }

                    Player t = Bukkit.getPlayer(args[1]);
                    Cooldown.setCooldowns(t.getUniqueId(), "treasure-winner", 0);
                    sender.sendMessage(Messages.get("cooldown-reset"));

                } else if (args[0].equalsIgnoreCase("key")) {
                    sender.sendMessage(Messages.get("wrong-command"));
                } else {
                    sender.sendMessage(Messages.get("wrong-command"));
                }

            } else if (args.length == 3) {

                if (args[0].equalsIgnoreCase("start")) {

                    if (args[1].equalsIgnoreCase("here")) {

                        if (TreasureData.getTreasureIdentifiers().contains(args[2])) {

                            if (!(sender instanceof Player)) {
                                Utils.sendNotPlayer();
                                return true;
                            }

                            Player p = (Player) sender;

                            if (!p.hasPermission("mystictreasures.admin")) {
                                p.sendMessage(Messages.get("no-permission"));
                                return true;
                            }

                            String id = args[2];
                            if (Hunt.isHuntActive(id)) {
                                sender.sendMessage(Messages.get("hunt-already-active"));
                                return true;
                            }

                            Hunt h = new Hunt(p.getLocation(), id, TreasureData.getByIdentifier(id).getDuration());
                            sender.sendMessage(Messages.get("generating-treasure"));

                            h.start();
                            sender.sendMessage(Messages.get("treasure-generated"));

                            return true;
                        }

                    } else {
                        sender.sendMessage(Messages.get("inexistent-treasure"));
                    }

                } else {
                    sender.sendMessage(Messages.get("wrong-command"));
                }

            }
            else if (args.length == 4) {

                if (args[0].equalsIgnoreCase("key")) {

                    if(sender instanceof Player)
                    {
                        if(!sender.hasPermission("mystictreasures.admin"))
                        {
                            sender.sendMessage(Messages.get("no-permission"));
                            return true;
                        }
                    }

                    if (Bukkit.getPlayer(args[1]) == null) {
                        sender.sendMessage(Messages.get("player-not-found"));
                        return true;
                    }

                    Player t = Bukkit.getPlayer(args[1]);

                    if (!TreasureData.getTreasureIdentifiers().contains(args[2])) {
                        sender.sendMessage(Messages.get("inexistent-treasure"));
                        return true;
                    }

                    String id = args[2];

                    if (!Utils.isInt(args[3])) {
                        sender.sendMessage(Messages.get("not-number"));
                        return true;
                    }

                    int amount = Integer.parseInt(args[3]);
                    Utils.addToInventory(t, TreasureData.getByIdentifier(id).getTreasureKey().getItemStack(amount));
                    sender.sendMessage(Messages.get("received-key"));

                } else {
                    sender.sendMessage(Messages.get("wrong-command"));
                }
            } else {
                sender.sendMessage(Messages.get("wrong-command"));
            }
        }

        return true;
    }

}
