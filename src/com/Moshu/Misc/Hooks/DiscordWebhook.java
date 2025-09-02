package com.Moshu.Misc.Hooks;

import com.Moshu.Misc.Storage.Messages;
import com.Moshu.Misc.Storage.Settings;
import com.Moshu.TreasureHunt.Core.Treasure;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;

public class DiscordWebhook {

    public enum DiscordTreasureEventType
    {
        SPAWN,
        CLAIM
    }

    private String url = "";
    private static DiscordWebhook instance;
    private JsonObject TREASURE_MESSAGES;
    private String TREASURE_SPAWN_PAYLOAD = "";
    private String TREASURE_CLAIM_PAYLOAD = "";
    private DiscordWebhook() {}

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public static DiscordWebhook getInstance()
    {
        if(instance == null)
        {
            instance = new DiscordWebhook();
        }

        return instance;

    }

    public void init()
    {

        setURL(Settings.getString("discord-webhook-url"));
        loadPayload();

    }

    public void setURL(String url)
    {
        this.url = url;
    }

    /**
     * Loads the Discord Webhook Payload from the config file
     */
    public void loadPayload()
    {

        if(getURL().isEmpty()) return;

        plugin.getLogger().info("Loading Discord Webhook payload for URL: " + getURL() + "..");

        File jsonFile = new File(plugin.getDataFolder(), "discord-webhook.json");

        try {

            try (Reader reader = new InputStreamReader(Files.newInputStream(jsonFile.toPath()), StandardCharsets.UTF_8)) {
                TREASURE_MESSAGES = JsonParser.parseReader(reader).getAsJsonObject();
            }

            TREASURE_CLAIM_PAYLOAD = TREASURE_MESSAGES.get("treasure-claim").toString();
            TREASURE_SPAWN_PAYLOAD = TREASURE_MESSAGES.get("treasure-spawn").toString();

        }
        catch (IOException e) {

            plugin.getLogger().warning("Could not load Discord Webhook Payload");
            TREASURE_CLAIM_PAYLOAD = "Messages couldn't be loaded.";
            TREASURE_SPAWN_PAYLOAD = "Messages couldn't be loaded.";

        }

        plugin.getLogger().info("Loaded Discord Webhook payload: " + TREASURE_SPAWN_PAYLOAD.substring(0, 40) + "..");

    }

    public String getURL()
    {
        return this.url;
    }

    public String getPayload(DiscordTreasureEventType t)
    {
        return t == DiscordTreasureEventType.SPAWN ? TREASURE_SPAWN_PAYLOAD : TREASURE_CLAIM_PAYLOAD;
    }

    /**
     * Applies placeholders to the payload
     * @param t the treasure object
     * @param type the type of treasure event
     * @return the payload with applied placeholders
     */
    private String getPayloadWithAppliedPlaceholders(Treasure t, DiscordTreasureEventType type)
    {

        String itemRewards = t.getTreasureData().getSanitizedRewards(5);
        String commandRewards = t.getTreasureData().getSanitizedCommandRewards(5);
        String treasureKeepers = t.getTreasureData().getSanitizedTreasureKeepers(5);

        Location loc = t.getLocation();
        String treasureName = t.getTreasureData().getTreasureName();
        int duration = t.getTreasureData().getDuration();
        boolean requiresKey = t.getTreasureData().getTreasureKey().requiresKey();

//        int x = h.getLocation().getBlockX();
//        int z = h.getLocation().getBlockZ();
//        int offset = h.getTreasure().getTreasureData().getCoordsNearTreasure();
//        int x_offset = x + Utils.randInt(-offset, offset);
//        int z_offset = z + Utils.randInt(-offset, offset);

        String payload = getPayload(type);
        payload = payload.replace("{item-rewards}", itemRewards)
                .replace("{now}", Instant.now().toString())
                .replace("{treasure-winner}", "winner")
                .replace("{treasure-top-3}", "winner")
                .replace("{command-rewards}", commandRewards)
                .replace("{treasure-keepers}", treasureKeepers)
                .replace("{treasure-name}", treasureName)
                .replace("{treasure-duration}", duration + "")
                .replace("{treasure-world}", loc.getWorld().getName())
                .replace("{treasure-requires-key}", requiresKey ? Messages.get("menu-yes") : Messages.get("menu-no"))
                .replace("{treasure-coords}", loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());

        return payload;
    }

    /**
     * Sends a Discord Webhook Message
     * @param type the type of treasure event
     * @param t the treasure object
     */
    public void sendWebhookMessage(DiscordTreasureEventType type, Treasure t) {

        if(getURL().isEmpty() || getURL().contains("Error")) return;

        try {

            URL url = new URL(getURL());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(getPayloadWithAppliedPlaceholders(t, type).getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Discord response: " + responseCode);

        } catch (Exception e) {
            plugin.getLogger().warning("Could not send Discord Webhook Message - invalid URL or the connection has failed");
            e.printStackTrace();
        }

    }

}
