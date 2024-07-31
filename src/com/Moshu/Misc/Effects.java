package com.Moshu.Misc;

import com.Moshu.TreasureHunt.Hunt;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class Effects {

    private static Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private static Location getGroundParticleLocation(Location loc)
    {
        return loc.add(ThreadLocalRandom.current().nextDouble(-1.2, 1.2), 0, ThreadLocalRandom.current().nextDouble(-1.2, 1.2));
    }

    public static void runCircle(Location base_loc)
    {

        BukkitRunnable run = new BukkitRunnable() {

            Location loc;
            int radius = 1;

            double i = 0;
            boolean rev;
            int step = 0;
            double x, y, z;

            Hunt h;

            @Override
            public void run() {

                if(!Hunt.isActive(base_loc.getWorld()))
                {
                    Bukkit.getConsoleSender().sendMessage("Hunt not active");
                    this.cancel();
                    return;
                }
                else
                {
                    h = Hunt.getHunt(base_loc.getWorld());

                    if(h.getTreasure() != null && h.getTreasure().isActive() && h.getTreasure().mobsCleared())
                    {
                        Bukkit.getConsoleSender().sendMessage("Treasure not active");
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

                    loc.getWorld().spawnParticle(Particle.CRIT, loc.getX() + x + 0.5, y , loc.getZ() + 0.5 + z, 0, 0,0,0, 0.0001);

                }

                loc.getWorld().spawnParticle(Particle.WAX_OFF, getGroundParticleLocation(loc), 1);
            }

        };

        run.runTaskTimerAsynchronously(plugin, 0, 1);



    }

    public static void runOrbs(Location base_loc)
    {

        BukkitRunnable run = new BukkitRunnable() {

            Location loc;
            int radius = 1;
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromBGR(255, 255, 0), 1);

            double c = 0;
            double i = 0;
            boolean rev;

            double x, x2, z, z2;

            @Override
            public void run() {

                if(!Hunt.isActive(base_loc.getWorld()))
                {
                    Bukkit.getConsoleSender().sendMessage("Hunt not active");
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

                loc.getWorld().spawnParticle(Particle.DUST, loc.getX() + x, loc.getY() + i + 0.7, loc.getZ() + z, 0, dust);
                loc.getWorld().spawnParticle(Particle.DUST, loc.getX() + x2, loc.getY() + i + 0.7, loc.getZ() + z2, 0, dust);

                c += 0.18;

                loc.getWorld().spawnParticle(org.bukkit.Particle.SOUL, getGroundParticleLocation(loc), 0, 0,0,0, 0.0001);

                loc.getWorld().spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, getGroundParticleLocation(loc), 0, 0,0,0, 0.0001);
                loc.getWorld().spawnParticle(Particle.WARPED_SPORE, getGroundParticleLocation(loc), 0, 0,0,0, 0.0001);

            }
        };

        run.runTaskTimerAsynchronously(plugin, 0, 1);

    }

    public static void createDoubleSpiral(Location base_loc) {


        BukkitRunnable run = new BukkitRunnable() {

            Location loc;
            int radius = 1;
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromBGR(0, 128, 255), 1);

            int step = 0;
            boolean rev;
            double c = 0;

            double x, x2, z, z2, y;

            @Override
            public void run() {

                if(!Hunt.isActive(base_loc.getWorld()))
                {
                    Bukkit.getConsoleSender().sendMessage("Hunt not active");
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
                    loc.getWorld().spawnParticle(org.bukkit.Particle.LAVA, loc, 3);
                }
                else if(step <= 0)
                {
                    rev = false;
                    loc.getWorld().spawnParticle(org.bukkit.Particle.LAVA, loc, 3);
                }

                y = loc.getY() + (step / 10D) - 0.5;

                loc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, loc.getX() + x, y , loc.getZ() + z, 0, 0,0,0, 0.0001);
                loc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, loc.getX() + x2, y , loc.getZ() + z2, 0, 0,0,0, 0.0001);

                c += 0.2;

                loc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, getGroundParticleLocation(loc), 0, 0,0,0, 0.0001);


                //i += 0.03;

            }

        };

        run.runTaskTimerAsynchronously(plugin, 0, 1);

    }


}
