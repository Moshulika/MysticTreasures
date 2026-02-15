package com.Moshu.TreasureHunt.Handlers;

import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Components.TreasureData;
import com.Moshu.TreasureHunt.Core.Treasure;
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
    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

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

            for(String id : TreasureData.getTreasureIdentifiers()) {

                String hologramName = Treasure.getHologramName(world, id);

                if (decent) {
                    Hologram h = DHAPI.getHologram(hologramName);
                    if (h != null) h.delete();
                }

                if (fancy) {

                    HologramManager hologramManager = FancyHologramsPlugin.get().getHologramManager();

                    if (hologramManager.getHologram(hologramName).isPresent()) {
                        de.oliver.fancyholograms.api.hologram.Hologram h = hologramManager.getHologram(hologramName).get();
                        hologramManager.removeHologram(h);
                        FancyHologramsPlugin.get().getHologramStorage().delete(h);
                    }

                }

            }

        }

    }


    public void createFancyHologram(Location loc, String hologramName)
    {

        if(Utils.isEnabled("FancyHolograms"))
        {
            HologramManager hologramManager = FancyHologramsPlugin.get().getHologramManager();

            TextHologramData data = new TextHologramData(hologramName, loc.clone().add(0.5,1.5,0.5));
            data.setBackground(Color.fromARGB(0, 0,0,0));
            data.setVisibilityDistance(50);
            de.oliver.fancyholograms.api.hologram.Hologram h = hologramManager.create(data);
            hologramManager.addHologram(h);
        }

    }

    public void update(String hologramName, ArrayList<String> lines) {

        if (Utils.isEnabled("FancyHolograms")) {

            HologramManager hologramManager = FancyHologramsPlugin.get().getHologramManager();

            if (hologramManager.getHologram(hologramName).isPresent()) {

                de.oliver.fancyholograms.api.hologram.Hologram h = hologramManager.getHologram(hologramName).get();

                Bukkit.getScheduler().runTask(plugin, () ->
                {

                    TextHologramData data = (TextHologramData) h.getData();
                    data.setText(lines);
                    h.queueUpdate();

                });

            }

        }
    }

    public void delete(String hologramName) {
        if (Utils.isEnabled("FancyHolograms")) {

            HologramManager hologramManager = FancyHologramsPlugin.get().getHologramManager();

            if (hologramManager.getHologram(hologramName).isPresent()) {

                de.oliver.fancyholograms.api.hologram.Hologram h = hologramManager.getHologram(hologramName).get();
                hologramManager.removeHologram(h);
                FancyHologramsPlugin.get().getHologramStorage().delete(h);

            }

        }
    }

}
