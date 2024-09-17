package com.Moshu.Misc;

import com.Moshu.Main;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FilesUpdater {

    private static Main plugin;

    public FilesUpdater(Main plugin)
    {
        this.plugin = plugin;
    }

    public static void update()
    {

        for(String s : plugin.getConfig().getConfigurationSection("settings.enabled-worlds").getKeys(false))
        {

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s + ".distance-from-player-to-spawn-mobs") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s + ".distance-from-player-to-spawn-mobs", 0);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".reward-all-players-who-participated") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s +".reward-all-players-who-participated", false);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".mob-wandering-distance") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s +".mob-wandering-distance", 20);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".animate-mob-spawning") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s +".animate-mob-spawning", true);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".potion-effect-radius") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s +".potion-effect-radius", 20);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".max-treasure-distance") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s +".max-treasure-distance", 15000);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".protect-mobs-from-sun") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s +".protect-mobs-from-sun", true);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".fall-from-the-sky") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s +".fall-from-the-sky", true);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".clicks-to-open") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s +".clicks-to-open", 1);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".protection-radius") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s +".protection-radius", 50);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".spawn-to-certain-coords") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s +".spawn-to-certain-coords", false);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".disable-griefing-protection") == null)
            {
                plugin.getConfigFile().set("settings.enabled-worlds." + s +".disable-griefing-protection", false);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".potion-effects") == null)
            {

                ArrayList<String> list = new ArrayList<String>();;
                list.add("REGENERATION:1");

                plugin.getConfigFile().set("settings.enabled-worlds." + s +".potion-effects", list);
            }

            if(plugin.getConfigFile().get("settings.enabled-worlds." + s +".spawn-coords") == null)
            {

                ArrayList<String> list = new ArrayList<String>();;
                list.add("0:0:0");

                plugin.getConfigFile().set("settings.enabled-worlds." + s +".spawn-coords", list);
            }

        }

        if(plugin.getConfigFile().get("settings.effects-particles.dust") == null)
        {
            plugin.getConfigFile().set("settings.effects-particles.dust", "DUST");
            plugin.getConfigFile().set("settings.effects-particles.wax_off", "WAX_OFF");
            plugin.getConfigFile().set("settings.effects-particles.soul", "SOUL");
            plugin.getConfigFile().set("settings.effects-particles.soul-fire-flame", "SOUL_FIRE_FLAME");
            plugin.getConfigFile().set("settings.effects-particles.warped-spore", "WARPED_SPORE");
            plugin.getConfigFile().set("settings.effects-particles.crit", "CRIT");
            plugin.getConfigFile().set("settings.effects-particles.lava", "LAVA");
            plugin.getConfigFile().set("settings.effects-particles.flame", "FLAME");
            plugin.getConfigFile().set("settings.effects-particles.treasure-spawn-particle", "EXPLOSION");
            plugin.getConfigFile().set("settings.effects-particles.treasure-fall-particle", "CAMPFIRE_SIGNAL_SMOKE");
            plugin.getConfigFile().set("settings.effects-particles.treasure-remove-particle", "EXPLOSION_EMITTER");
        }

        if(plugin.getConfigFile().get("settings.updater") == null)
        {
            plugin.getConfigFile().set("settings.updater", true);
        }

        if(plugin.getMessages().get("messages.config-reload") == null)
        {
            plugin.getMessages().set("messages.config-reload", "&6&lTreasure&e&lHunt &fConfig reloaded!");
        }

        if(plugin.getMessages().get("messages.remaining-clicks") == null)
        {
            plugin.getMessages().set("messages.remaining-clicks", "&6&lTreasure&e&lHunt &fKeep on clicking! ({current_clicks}/{needed_clicks})");
        }

        if(plugin.getMessages().get("messages.participating") == null)
        {
            plugin.getMessages().set("messages.participating", "&6&lTreasure&e&lHunt &fYou are now participating in this TreasureHunt!");
        }

        if(plugin.getMessages().get("messages.participating-cleared-mobs") == null)
        {
            plugin.getMessages().set("messages.participating-cleared-mobs", "&6&lTreasure&e&lHunt &fYou killed all the Treasure Keepers! The reward is all yours!");
        }

        if(plugin.getMessages().get("messages.no-creative") == null)
        {
            plugin.getMessages().set("messages.no-creative", "&6&lTreasure&e&lHunt &fYou can't break Treasures in Creative mode");
        }

        if(plugin.getMessages().get("messages.cooldown-reset") == null)
        {
            plugin.getMessages().set("messages.cooldown-reset", "&6&lTreasure&e&lHunt &fThis player is not online!");
        }

        if(plugin.getMessages().get("messages.player-not-found") == null)
        {
            plugin.getMessages().set("messages.player-not-found", "&6&lTreasure&e&lHunt &fYou successfully reset the player's cooldown!");
        }

        try
        {

            File configf = new File(plugin.getDataFolder(), "config.yml");
            File messagesf = new File(plugin.getDataFolder(), "messages.yml");
            plugin.getConfigFile().save(configf);
            plugin.getMessages().save(messagesf);

        } catch (IOException e1) {
            e1.printStackTrace();
        }

        plugin.reloadFiles();

    }

}
