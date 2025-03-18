package com.Moshu.TreasureHunt;

import com.Moshu.Misc.Messages;
import com.Moshu.Misc.Utils;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class HologramHandler {

    private static HologramHandler handler = null;
    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public static HologramHandler getInstance()
    {

        if(handler == null)
        {
            handler = new HologramHandler();
        }

        return handler;

    }

    public void cleanup()
    {

        boolean decent = Utils.isEnabled("DecentHolograms");
        boolean fancy = Utils.isEnabled("FancyHolograms");

        for(World world : Bukkit.getWorlds())
        {

            if(decent)
            {
                Hologram h = DHAPI.getHologram("treasurehunt_" + world.getName());
                if(h != null) h.delete();
            }

            if(fancy)
            {

                HologramManager hologramManager = FancyHologramsPlugin.get().getHologramManager();

                if (hologramManager.getHologram("treasurehunt_" + world.getName()).isPresent()) {
                    de.oliver.fancyholograms.api.hologram.Hologram h = hologramManager.getHologram("treasurehunt_" + world.getName()).get();
                    hologramManager.removeHologram(h);
                    FancyHologramsPlugin.get().getHologramStorage().delete(h);
                }

            }

        }

    }


    public void createFancyHologram(Location loc)
    {

        if(Utils.isEnabled("FancyHolograms"))
        {
            HologramManager hologramManager = FancyHologramsPlugin.get().getHologramManager();

            TextHologramData data = new TextHologramData("treasurehunt_" + loc.getWorld().getName(), loc.clone().add(0.5,1.5,0.5));
            data.setBackground(Color.fromARGB(0, 0,0,0));
            data.setVisibilityDistance(50);
            de.oliver.fancyholograms.api.hologram.Hologram h = hologramManager.create(data);
            hologramManager.addHologram(h);
        }

    }

    public void update(World w, ArrayList<String> lines) {

        if (Utils.isEnabled("FancyHolograms")) {

            HologramManager hologramManager = FancyHologramsPlugin.get().getHologramManager();

            if (hologramManager.getHologram("treasurehunt_" + w.getName()).isPresent()) {

                de.oliver.fancyholograms.api.hologram.Hologram h = hologramManager.getHologram("treasurehunt_" + w.getName()).get();

                Bukkit.getScheduler().runTask(plugin, () ->
                {

                    TextHologramData data = (TextHologramData) h.getData();
                    data.setText(lines);
                    h.queueUpdate();

                });

            }

        }
    }

    public void delete(Location loc) {
        if (Utils.isEnabled("FancyHolograms")) {

            HologramManager hologramManager = FancyHologramsPlugin.get().getHologramManager();

            if (hologramManager.getHologram("treasurehunt_" + loc.getWorld().getName()).isPresent()) {

                de.oliver.fancyholograms.api.hologram.Hologram h = hologramManager.getHologram("treasurehunt_" + loc.getWorld().getName()).get();
                hologramManager.removeHologram(h);
                FancyHologramsPlugin.get().getHologramStorage().delete(h);

            }

        }
    }

}
