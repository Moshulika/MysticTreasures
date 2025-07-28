package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Messages;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.objects.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class TreasureMenu implements Listener {

    private static HashMap<Player, TreasureData> treasureData = new HashMap<>();

    private static ArrayList<String> applyPlaceholders(ArrayList<String> lore, TreasureData d)
    {
        ArrayList<String> newList = new ArrayList<>();

        int treasureKeepers = d.getTreasureKeepers().size();
        int rewards = d.getCommandRewards().size() + d.getItemRewards().size();
        int duration = d.getDuration();
        String yes = Messages.get("menu-yes");
        String no = Messages.get("menu-no");
        String requiresMobsDead = d.requireAllMobsDead() ? yes : no;
        String debuff = d.getDebuff().isEnabled() ? yes : no;
        String world = d.getWorldName();
        String requiresKey = d.getTreasureKey().requiresKey() ? yes : no;

        for(String s : lore)
        {

            newList.add(s.replace("{keepers}", treasureKeepers + "")
                    .replace("{rewards}", rewards + "")
                    .replace("{duration}", duration + "")
                    .replace("{requires_mobs_dead}", requiresMobsDead)
                    .replace("{world}", world)
                    .replace("{debuff}", debuff)
                    .replace("{requires_key}", requiresKey));

        }

        return newList;
    }

    private static ArrayList<String> applyPlaceholdersToItemRewards(ArrayList<String> lore, ItemReward i)
    {
        ArrayList<String> newList = new ArrayList<>();

        for(String s : lore)
        {

            newList.add(s.replace("{item}", Utils.setCapitals(Utils.format(i.getName())))
                    .replace("{amount}", i.getRange())
                    .replace("{chance}", i.getChance() + "%"));

        }

        return newList;
    }

    private static ArrayList<String> applyPlaceholdersToCommandRewards(ArrayList<String> lore, CommandReward i)
    {
        ArrayList<String> newList = new ArrayList<>();

        for(String s : lore)
        {

            newList.add(s.replace("{command}", i.getCommand())
                    .replace("{chance}", i.getChance() + "%"));

        }

        return newList;
    }

    private static ArrayList<String> applyPlaceholdersToTreasureKeepers(ArrayList<String> lore, TreasureKeeper i)
    {
        ArrayList<String> newList = new ArrayList<>();

        String yes = Messages.get("menu-yes");
        String no = Messages.get("menu-no");

        String entity = i.isMythicMob() ? i.getMobId() : i.getEntityType().name();
        String mythic = i.isMythicMob() ? yes : no;

        for(String s : lore)
        {

            newList.add(s.replace("{entity}", Utils.setCapitals(entity.toLowerCase()
                            .replace("_", " ")))
                            .replace("{mythic}", mythic)
                            .replace("{buffed}", i.getPotionEffects().isEmpty() ? no : yes)
                            .replace("{range}", i.getRange())
                            .replace("{health}", i.getMaxHealth() + "")
                    );


        }

        return newList;
    }

    public static void showcase(Player p)
    {

        Inventory inv = Bukkit.createInventory(null, 27, Messages.get("showcase-hunts-menu.title"));
        ItemMeta im;

        String name = Utils.format(Messages.get("showcase-hunts-menu.name"));

        for(TreasureData d : TreasureData.getTreasureData())
        {
            ItemStack is = Utils.checkMaterial(d.getMenuItem());
            im = is.getItemMeta();

            if(im == null) continue;

            im.setDisplayName(Utils.format(name.replace("{treasure_name}", d.getTreasureName())));

            ArrayList<String> lore = Messages.getAndFormatList("messages.showcase-hunts-menu.lore");
            im.setLore(applyPlaceholders(lore, d));

            is.setItemMeta(im);
            inv.addItem(is);

        }

        Utils.fillWithGlass(inv);
        p.openInventory(inv);

    }

    public static void keyMenu(Player p, TreasureData d)
    {

        if(!d.getTreasureKey().requiresKey()) return;

        Inventory inv = Bukkit.createInventory(null, 54, Messages.get("showcase-key-menu-title"));
        ItemStack is = d.getTreasureKey().getItemStack(1);
        inv.setItem(13, is);

        ItemStack rewards = new ItemStack(Utils.checkMaterial(Messages.get("showcase-hunts-menu.other-buttons.rewards.item")));
        ItemMeta rewardsMeta = rewards.getItemMeta();
        rewardsMeta.setDisplayName(Utils.format(Messages.get("showcase-hunts-menu.other-buttons.rewards.name")));
        rewards.setItemMeta(rewardsMeta);
        inv.setItem(48, rewards);

        ItemStack home = new ItemStack(Utils.checkMaterial(Messages.get("showcase-hunts-menu.other-buttons.home.item")));
        ItemMeta homeMeta = home.getItemMeta();
        homeMeta.setDisplayName(Utils.format(Messages.get("showcase-hunts-menu.other-buttons.home.name")));
        home.setItemMeta(homeMeta);
        inv.setItem(49, home);

        ItemStack keepers = new ItemStack(Utils.checkMaterial(Messages.get("showcase-hunts-menu.other-buttons.keepers.item")));
        ItemMeta keepersMeta = keepers.getItemMeta();
        keepersMeta.setDisplayName(Utils.format(Messages.get("showcase-hunts-menu.other-buttons.keepers.name")));
        keepers.setItemMeta(keepersMeta);
        inv.setItem(50, keepers);

        Utils.fillWithGlass(inv);
        p.openInventory(inv);

    }

    private static void rewardMenu(TreasureData data, Player p) {

        Inventory inv = Bukkit.createInventory(null, 54, Messages.get("showcase-rewards-menu.title"));
        ItemMeta im;

        String name = Utils.format(Messages.get("showcase-rewards-menu.reward-item-name"));

        //if(data.getItemRewards().isEmpty()) return;

        for (ItemReward i : data.getItemRewards().subList(0, Math.min(data.getItemRewards().size(), 46))) {

            if(i.isOraxen() || i.isNexo() || i.isItemsAdder() || i.isMMOItem())
            {
                inv.addItem(i.getItemStack());
                continue;
            }

            ItemStack is = Utils.checkMaterial(i.getMenuItem());
            im = is.getItemMeta();

            if (im == null) continue;

            im.setDisplayName(Utils.format(name.replace("{reward_name}", Utils.setCapitals(i.getIdentifier()))));

            ArrayList<String> lore = Messages.getAndFormatList("messages.showcase-rewards-menu.reward-item-lore");
            im.setLore(applyPlaceholdersToItemRewards(lore, i));

            is.setItemMeta(im);
            inv.addItem(is);

        }

        name = Utils.format(Messages.get("showcase-rewards-menu.command-item-name"));

        for (CommandReward i : data.getCommandRewards().subList(0, Math.min(data.getCommandRewards().size(), 46))) {

            ItemStack is = Utils.checkMaterial(i.getMenuItem());
            im = is.getItemMeta();

            if (im == null) continue;

            im.setDisplayName(Utils.format(name.replace("{reward_name}", Utils.setCapitals(i.getIdentifier()))));

            ArrayList<String> lore = Messages.getAndFormatList("messages.showcase-rewards-menu.command-item-lore");
            im.setLore(applyPlaceholdersToCommandRewards(lore, i));

            is.setItemMeta(im);
            inv.addItem(is);
        }

        ItemStack home = new ItemStack(Utils.checkMaterial(Messages.get("showcase-hunts-menu.other-buttons.home.item")));
        ItemMeta homeMeta = home.getItemMeta();
        homeMeta.setDisplayName(Utils.format(Messages.get("showcase-hunts-menu.other-buttons.home.name")));
        home.setItemMeta(homeMeta);
        inv.setItem(49, home);

        ItemStack keepers = new ItemStack(Utils.checkMaterial(Messages.get("showcase-hunts-menu.other-buttons.keepers.item")));
        ItemMeta keepersMeta = keepers.getItemMeta();
        keepersMeta.setDisplayName(Utils.format(Messages.get("showcase-hunts-menu.other-buttons.keepers.name")));
        keepers.setItemMeta(keepersMeta);
        inv.setItem(50, keepers);

        if(data.getTreasureKey().requiresKey())
        {
            ItemStack key = new ItemStack(Utils.checkMaterial(Messages.get("showcase-hunts-menu.other-buttons.key.item")));
            ItemMeta keyMeta = key.getItemMeta();
            keyMeta.setDisplayName(Utils.format(Messages.get("showcase-hunts-menu.other-buttons.key.name")));
            key.setItemMeta(keyMeta);
            inv.setItem(48, key);
        }
        
        Utils.fillWithGlass(inv);
        p.openInventory(inv);


    }

    private static void keepersMenu(TreasureData data, Player p) {


        Inventory inv = Bukkit.createInventory(null, 54, Messages.get("showcase-keepers-menu.title"));

        ItemStack is;
        ItemMeta im;

        String name = Utils.format(Messages.get("showcase-keepers-menu.name"));
        //if(data.getTreasureKeepers().isEmpty()) return;

        for (TreasureKeeper k : data.getTreasureKeepers().subList(0, Math.min(data.getTreasureKeepers().size(), 46))) {

            is = Utils.checkMaterial(k.getMenuItem());
            im = is.getItemMeta();

            if (im == null) continue;
            im.setDisplayName(Utils.format(name.replace("{keeper_name}", k.getCustomName())));

            ArrayList<String> lore = Messages.getAndFormatList("messages.showcase-keepers-menu.lore");
            im.setLore(applyPlaceholdersToTreasureKeepers(lore, k));

            is.setItemMeta(im);
            inv.addItem(is);

        }

        ItemStack home = new ItemStack(Utils.checkMaterial(Messages.get("showcase-hunts-menu.other-buttons.home.item")));
        ItemMeta homeMeta = home.getItemMeta();
        homeMeta.setDisplayName(Utils.format(Messages.get("showcase-hunts-menu.other-buttons.home.name")));
        home.setItemMeta(homeMeta);
        inv.setItem(49, home);

        ItemStack rewards = new ItemStack(Utils.checkMaterial(Messages.get("showcase-hunts-menu.other-buttons.rewards.item")));
        ItemMeta rewardsMeta = rewards.getItemMeta();
        rewardsMeta.setDisplayName(Utils.format(Messages.get("showcase-hunts-menu.other-buttons.rewards.name")));
        rewards.setItemMeta(rewardsMeta);
        inv.setItem(48, rewards);

        if(data.getTreasureKey().requiresKey())
        {
            ItemStack key = new ItemStack(Utils.checkMaterial(Messages.get("showcase-hunts-menu.other-buttons.key.item")));
            ItemMeta keyMeta = key.getItemMeta();
            keyMeta.setDisplayName(Utils.format(Messages.get("showcase-hunts-menu.other-buttons.key.name")));
            key.setItemMeta(keyMeta);
            inv.setItem(50, key);
        }

        Utils.fillWithGlass(inv);
        p.openInventory(inv);

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if (e.getClickedInventory() == null) return;

        try {

            Object view = InventoryClickEvent.class.getMethod("getView").invoke(e);
            Method getTitle = view.getClass().getMethod("getTitle");
            getTitle.setAccessible(true);
            String title = (String) getTitle.invoke(view);

            Player p = (Player) e.getWhoClicked();

            if (title.equals(Messages.get("showcase-hunts-menu.title"))) {

                e.setCancelled(true);

                ArrayList<TreasureData> data = TreasureData.getTreasureData();
                if(e.getSlot() >= data.size()) return;

                if(e.isLeftClick())
                {
                    treasureData.put(p, data.get(e.getSlot()));
                    rewardMenu(data.get(e.getSlot()), p);
                }
                else if(e.isRightClick())
                {
                    treasureData.put(p, data.get(e.getSlot()));
                    keepersMenu(data.get(e.getSlot()), p);
                }

            }
            else if(title.equals(Messages.get("showcase-rewards-menu.title")))
            {
                e.setCancelled(true);

                if(!treasureData.containsKey(p) || treasureData.get(p) == null)
                {
                    p.closeInventory();
                    return;
                }

                if(e.getSlot() == 48) keyMenu(p, treasureData.get(p));
                if(e.getSlot() == 49) showcase(p);
                if(e.getSlot() == 50) keepersMenu(treasureData.get(p), p);

            }
            else if(title.equals(Messages.get("showcase-keepers-menu.title")))
            {
                e.setCancelled(true);

                if(!treasureData.containsKey(p) || treasureData.get(p) == null)
                {
                    p.closeInventory();
                    return;
                }

                if(e.getSlot() == 49) showcase(p);
                if(e.getSlot() == 48) rewardMenu(treasureData.get(p), p);
                if(e.getSlot() == 50) keyMenu(p, treasureData.get(p));
            }
            else if(title.equals(Messages.get("showcase-key-menu-title")))
            {
                e.setCancelled(true);

                if(!treasureData.containsKey(p) || treasureData.get(p) == null)
                {
                    p.closeInventory();
                    return;
                }

                if(e.getSlot() == 49) showcase(p);
                if(e.getSlot() == 48) rewardMenu(treasureData.get(p), p);
                if(e.getSlot() == 50) keepersMenu(treasureData.get(p), p);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
