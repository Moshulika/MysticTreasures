package com.Moshu.Misc.Storage;

import com.Moshu.TreasureHunt.Components.TreasureData;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
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

    public static synchronized FileHandler getInstance() {

        if (fileHandler == null) fileHandler = new FileHandler();
        return fileHandler;

    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
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

        File[] files = targetFolder.listFiles();
        if (files != null && files.length != 0) {
            return;
        }

        // Copy the resource file from the JAR
        try (InputStream inputStream = plugin.getResource(resourceName);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

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

    /**
     * Merges a local YAML file with its default version from the JAR.
     * Missing keys are added to the local file while keeping existing values intact.
     *
     * @param localFile    The file on the disk
     * @param resourcePath The path to the resource inside the JAR
     */
    public void mergeWithDefault(File localFile, String resourcePath) {
        mergeWithDefault(localFile, resourcePath, Collections.emptyList());
    }

    /**
     * Merges a local YAML file with its default version from the JAR, respecting protected sections.
     *
     * @param localFile         The file on the disk
     * @param resourcePath      The path to the resource inside the JAR
     * @param protectedSections List of sections that should not be merged if they already exist locally
     */
    public void mergeWithDefault(File localFile, String resourcePath, java.util.List<String> protectedSections) {
        if (!localFile.exists()) {
            saveResourceToFolder(resourcePath, localFile.getParentFile().getName());
            return;
        }

        YamlConfiguration localConfig = YamlConfiguration.loadConfiguration(localFile);

        try (InputStream is = plugin.getResource(resourcePath)) {
            if (is == null) return;

            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));

            if (mergeConfigs(localConfig, defaultConfig, protectedSections)) {
                localConfig.save(localFile);
                plugin.getLogger().info("Updated " + localFile.getName() + " with missing default values.");
            }

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not merge config file: " + localFile.getName(), e);
        }
    }

    /**
     * Merges a configuration section with another, respecting protected sections.
     *
     * @param local             The local section to merge into
     * @param defaultConfig     The default section to merge from
     * @param protectedSections A list of keys that, if present in the local config, should skip merging their children.
     * @return true if the local config was modified
     */
    private boolean mergeConfigs(ConfigurationSection local, ConfigurationSection defaultConfig, java.util.List<String> protectedSections) {
        boolean modified = false;
        for (String key : defaultConfig.getKeys(true)) {
            // Check if this key or any of its parents are in the protectedSections list
            boolean isProtected = false;
            for (String ps : protectedSections) {
                if ((key.equals(ps) || key.startsWith(ps + ".")) && local.contains(ps)) {
                    isProtected = true;
                    break;
                }
            }

            if (isProtected) continue;

            if (!local.contains(key)) {
                local.set(key, defaultConfig.get(key));
                modified = true;
            }
        }
        return modified;
    }

    @SuppressFBWarnings("MS_EXPOSE_REP")
    public ArrayList<TreasureData> setup() {
        createFolder();
        saveResourceToFolder("treasure.yml", "treasures");
        saveResourceToFolder("rounds.yml", "rounds");
        com.Moshu.TreasureHunt.Components.RoundManager.load();
        return loadTreasures();
    }

    @SuppressFBWarnings("MS_EXPOSE_REP")
    public ArrayList<TreasureData> reload() {
        com.Moshu.TreasureHunt.Components.RoundManager.load();
        return loadTreasures();
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    private void createFolder() {
        folder = new File(plugin.getDataFolder(), "treasures");

        if (!folder.exists()) folder.mkdir();
    }

    /**
     * Gets a configuration file for the treasure
     * MUST HAVE .YML IN NAME
     *
     * @param name the treasure's name
     * @return the configuration file for the treasure
     */
    public YamlConfiguration getTreasureFile(String name) {

        File f = new File(plugin.getDataFolder() + "/treasures/" + name);

        YamlConfiguration shop = new YamlConfiguration();

        try {
            shop.load(f);
        } catch (IOException | InvalidConfigurationException e) {

        }


        return shop;

    }

    /**
     * Get a list of all menu names available
     *
     * @return a list of all the menus available
     */
    @SuppressFBWarnings("MS_EXPOSE_REP")
    public ArrayList<String> getTreasureNames() {

        ArrayList<String> menus = new ArrayList<>();
        File directory = new File(plugin.getDataFolder() + "/treasures/");

        String[] filesList = directory.list();
        if (filesList == null) return menus;

        Collections.addAll(menus, filesList);

        return menus;

    }

    /**
     *
     * Get a list of all menus available
     *
     * @return a list of all the menus available
     */
    @SuppressFBWarnings("MS_EXPOSE_REP")
    private ArrayList<TreasureData> loadTreasures() {

        ArrayList<TreasureData> treasures = new ArrayList<>();
        File directory = new File(plugin.getDataFolder() + "/treasures/");

        String[] filesList = directory.list();
        if (filesList == null) {
            plugin.getLogger().info("No treasures found inside the treasures folder");
            return treasures;
        }

        // Load default treasure config to use as schema for merging
        YamlConfiguration defaultTreasureConfig = null;
        try (InputStream is = plugin.getResource("treasure.yml")) {
            if (is != null) {
                defaultTreasureConfig = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not load default treasure.yml for merging.");
        }

        java.util.List<String> protectedSections = java.util.Arrays.asList("rounds", "scheduler");


        for (String s : filesList) {
            File f = new File(directory, s);
            if (!f.getName().endsWith(".yml")) continue;

            YamlConfiguration treasureFile = YamlConfiguration.loadConfiguration(f);
            ConfigurationSection treasureRootSection = treasureFile.getConfigurationSection("treasure");

            if (treasureRootSection == null || treasureRootSection.getKeys(false).isEmpty()) {
                continue;
            }

            String treasureId = treasureRootSection.getKeys(false).iterator().next();

            // Auto-merge missing fields from default schema if available
            if (defaultTreasureConfig != null) {
                ConfigurationSection defaultRoot = defaultTreasureConfig.getConfigurationSection("treasure");
                if (defaultRoot != null && !defaultRoot.getKeys(false).isEmpty()) {
                    String defaultId = defaultRoot.getKeys(false).iterator().next();
                    ConfigurationSection defaultSection = defaultRoot.getConfigurationSection(defaultId);
                    ConfigurationSection localSection = treasureRootSection.getConfigurationSection(treasureId);

                    if (defaultSection != null && localSection != null) {
                        if (mergeConfigs(localSection, defaultSection, protectedSections)) {
                            try {
                                treasureFile.save(f);
                                plugin.getLogger().info("Merged missing fields into treasure file: " + s);
                            } catch (IOException e) {
                                plugin.getLogger().warning("Could not save merged treasure file: " + s);
                            }
                        }
                    }
                }
            }

            ConfigurationSection treasureSection = treasureFile.getConfigurationSection("treasure." + treasureId);
            if (treasureSection == null) continue;

            TreasureData m = new TreasureData(treasureSection);
            m.setIdentifier(treasureId);

            boolean repeated = false;
            for (TreasureData x : treasures) {
                if (x.getTreasureName().equals(m.getTreasureName())) {
                    repeated = true;
                    Bukkit.getLogger().log(Level.SEVERE, "Treasure " + s + " has a name that's already taken");
                }
                if (x.getIdentifier().equals(m.getIdentifier())) {
                    repeated = true;
                    Bukkit.getLogger().log(Level.SEVERE, "Treasure " + s + " has an ID that's already taken");
                }
            }

            if (!repeated) {
                treasures.add(m);
            }
        }

        plugin.getLogger().info("Loaded " + treasures.size() + " treasure(s)");
        return treasures;
    }

}
