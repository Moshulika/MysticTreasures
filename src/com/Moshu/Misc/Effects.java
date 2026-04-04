package com.Moshu.Misc;

import com.Moshu.Misc.Storage.Settings;
import com.Moshu.TreasureHunt.Core.Hunt;
import com.Moshu.TreasureHunt.Core.Treasure;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class Effects {

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private static final Particle DUST = Settings.getCompatParticle("dust");
    private static final Particle WAX_OFF = Settings.getCompatParticle("wax_off");
    private static final Particle SOUL = Settings.getCompatParticle("soul");
    private static final Particle SOUL_FIRE_FLAME = Settings.getCompatParticle("soul-fire-flame");
    private static final Particle WARPED_SPORE = Settings.getCompatParticle("warped-spore");
    private static final Particle LAVA = Settings.getCompatParticle("lava");
    private static final Particle FLAME = Settings.getCompatParticle("flame");
    private static final Particle CRIT = Settings.getCompatParticle("crit");

    private static Location getGroundParticleLocation(Location loc)
    {
        return loc.add(ThreadLocalRandom.current().nextDouble(-1.2, 1.2), 0, ThreadLocalRandom.current().nextDouble(-1.2, 1.2));
    }

    public static void runCircle(Treasure t)
    {

        Location base_loc = t.getLocation();

        BukkitRunnable run = new BukkitRunnable() {

            Location loc;

            boolean rev;
            int step = 0;
            double x, y, z;

            Hunt h;

            @Override
            public void run() {

                if(!t.isActive())
                {
                    this.cancel();
                    return;
                }
                else
                {
                    h = t.getHunt();

                    if(h.getTreasure() != null && h.getTreasure().isActive() && h.getTreasure().mobsCleared())
                    {
                        this.cancel();
                        return;
                    }

                }

                loc = base_loc.clone();

                step += rev ? -1 : 1;

                if(step >= 30)
                {
                    rev = true;
                }
                else if(step <= 0)
                {
                    rev = false;
                }

                y = loc.getY() + (step / 10D) - 0.5;

                for(int s = 0; s < 18; s++)
                {

                    x = Math.cos(s);
                    z = Math.sin(s);

                    loc.getWorld().spawnParticle(CRIT, loc.getX() + x + 0.5, y , loc.getZ() + 0.5 + z, 0, 0,0,0, 0.0001);

                }

                loc.getWorld().spawnParticle(WAX_OFF, getGroundParticleLocation(loc), 1);
            }

        };

        run.runTaskTimerAsynchronously(plugin, 0, 2);



    }

    public static void runOrbs(Treasure t)
    {

        Location base_loc = t.getLocation();
        BukkitRunnable run = new BukkitRunnable() {

            Location loc;
            final Particle.DustOptions dust = new Particle.DustOptions(Color.fromBGR(255, 255, 0), 1);

            double c = 0;
            double i = 0;
            boolean rev;

            double x, x2, z, z2;

            @Override
            public void run() {

                if(!t.isActive())
                {
                    this.cancel();
                    return;
                }

                loc = base_loc.clone();

                x = Math.cos(c);
                z = Math.sin(c);

                x2 = -x;
                z2 = -z;

                if(i <= 0)
                {
                    rev = false;
                }

                if(i >= 0.3)
                {
                    rev = true;
                }

                if(rev)
                {
                    i -= 0.01;
                }
                else
                {
                    i += 0.01;
                }

                loc.getWorld().spawnParticle(DUST, loc.getX() + x + 0.5, loc.getY() + i + 0.7, loc.getZ() + z + 0.5, 0, dust);
                loc.getWorld().spawnParticle(DUST, loc.getX() + x2 + 0.5, loc.getY() + i + 0.7, loc.getZ() + z2 + 0.5, 0, dust);

                c += 0.18;

                loc.getWorld().spawnParticle(SOUL, getGroundParticleLocation(loc), 0, 0,0,0, 0.0001);

                loc.getWorld().spawnParticle(SOUL_FIRE_FLAME, getGroundParticleLocation(loc), 0, 0,0,0, 0.0001);
                loc.getWorld().spawnParticle(WARPED_SPORE, getGroundParticleLocation(loc), 0, 0,0,0, 0.0001);

            }
        };

        run.runTaskTimerAsynchronously(plugin, 0, 2);

    }

    public static void createDoubleSpiral(Treasure t) {

        Location base_loc = t.getLocation();

        BukkitRunnable run = new BukkitRunnable() {

            Location loc;
            final Particle.DustOptions dust = new Particle.DustOptions(Color.fromBGR(0, 128, 255), 1);

            int step = 0;
            boolean rev;
            double c = 0;

            double x, x2, z, z2, y;

            @Override
            public void run() {

                if(!t.isActive())
                {
                    this.cancel();
                    return;
                }

                loc = base_loc.clone();

                x = Math.cos(c);
                z = Math.sin(c);

                x2 = -x;
                z2 = -z;

                step += rev ? -1 : 1;

                if(step >= 30)
                {
                    rev = true;
                    loc.getWorld().spawnParticle(LAVA, loc, 3);
                }
                else if(step <= 0)
                {
                    rev = false;
                    loc.getWorld().spawnParticle(LAVA, loc, 3);
                }

                y = loc.getY() + (step / 10D) - 0.5;

                loc.getWorld().spawnParticle(Particle.FLAME, loc.getX() + x + 0.5, y , loc.getZ() + z + 0.5, 0, 0,0,0, 0.0001);
                loc.getWorld().spawnParticle(Particle.FLAME, loc.getX() + x2 + 0.5, y , loc.getZ() + z2 + 0.5, 0, 0,0,0, 0.0001);

                c += 0.2;

                loc.getWorld().spawnParticle(FLAME, getGroundParticleLocation(loc), 0, 0,0,0, 0.0001);


                //i += 0.03;

            }

        };

        run.runTaskTimerAsynchronously(plugin, 0, 2);

    }


}
