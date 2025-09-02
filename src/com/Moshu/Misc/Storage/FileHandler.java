package com.Moshu.Misc.Storage;

import com.Moshu.TreasureHunt.Components.TreasureData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

public class FileHandler {

    private File folder;
    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    private static FileHandler fileHandler;

    public static FileHandler getInstance() {

        if(fileHandler == null) return new  FileHandler();
        return fileHandler;

    }

    private void saveResourceToFolder(String resourceName, String folderName) {

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File targetFolder = new File(dataFolder, folderName);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        File outputFile = new File(targetFolder, resourceName);
        if (outputFile.exists()) {
            return;
        }

        if(targetFolder.listFiles().length != 0) {
            return;
        }

        // Copy the resource file from the JAR
        try (InputStream inputStream = plugin.getResource(resourceName);
             FileOutputStream outputStream = new FileOutputStream(outputFile))
        {

            if (inputStream == null) {
                plugin.getLogger().warning("Resource " + resourceName + " not found in JAR!");
                return;
            }

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            plugin.getLogger().info("Successfully copied " + resourceName + " to " + folderName + "/");

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to copy resource: " + e.getMessage());
        }
    }

    public ArrayList<TreasureData> setup()
    {
        createFolder();
        saveResourceToFolder("treasure.yml", "treasures");
        return loadTreasures();
    }

    public ArrayList<TreasureData> reload()
    {
        return loadTreasures();
    }

    private void createFolder()
    {
        folder = new File(plugin.getDataFolder(), "treasures");

        if(!folder.exists()) folder.mkdir();
    }

    /**
     * Gets a configuration file for the treasure
     * MUST HAVE .YML IN NAME
     *
     * @param name the treasure's name
     * @return the configuration file for the treasure
     */
    public YamlConfiguration getTreasureFile(String name)
    {

        File f = new File(plugin.getDataFolder() + "/treasures/" + name);

        YamlConfiguration shop = new YamlConfiguration();

        try
        {
            shop.load(f);
        }

        catch (IOException | InvalidConfigurationException e)
        {

        }


        return shop;

    }

    /**
     * Get a list of all menu names available
     * @return a list of all the menus available
     */
    public ArrayList<String> getTreasureNames()
    {

        ArrayList<String> menus = new ArrayList<>();
        File directory = new File(plugin.getDataFolder() + "/treasures/");

        if(directory.list() == null) return menus;

        Collections.addAll(menus, directory.list());

        return menus;

    }

    /**
     *
     * Get a list of all menus available
     * @return a list of all the menus available
     */
    private ArrayList<TreasureData> loadTreasures()
    {

        ArrayList<TreasureData> treasures = new ArrayList<>();
        File directory = new File(plugin.getDataFolder() + "/treasures/");

        if(directory.list() == null)
        {
            plugin.getLogger().info("No treasures found inside the treasures folder");
            return treasures;
        }

        TreasureData m;

        for(String s : directory.list())
        {

            boolean repeated = false;
            FileConfiguration treasureFile = getTreasureFile(s);

            ConfigurationSection treasureRootSection = treasureFile.getConfigurationSection("treasure");

            if (treasureRootSection == null || treasureRootSection.getKeys(false).isEmpty()) {
                throw new IllegalStateException("No treasure section or keys found in the configuration.");
            }

            String treasureId = treasureRootSection.getKeys(false).iterator().next();

            ConfigurationSection treasureSection = treasureFile.getConfigurationSection("treasure." + treasureId);

            if (treasureSection == null) {
                throw new IllegalStateException("Treasure section for id '" + treasureId + "' not found.");
            }

            m = new TreasureData(treasureSection);
            m.setIdentifier(treasureId);

            for(TreasureData x : treasures)
            {

                if(x.getTreasureName().equals(m.getTreasureName()))
                {
                    repeated = true;
                    Bukkit.getLogger().log(Level.SEVERE, "Treasure " + s + " has a name that's already taken");
                }

                if(x.getIdentifier().equals(m.getIdentifier()))
                {
                    repeated = true;
                    Bukkit.getLogger().log(Level.SEVERE, "Treasure " + s + " has an ID that's already taken");
                }

            }

            if(!repeated) {

                treasures.add(m);

            }

        }

        plugin.getLogger().info("Loaded " + treasures.size() + " treasure(s)");
        return treasures;

    }

}
