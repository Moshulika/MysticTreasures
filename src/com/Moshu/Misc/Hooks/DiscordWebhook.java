package com.Moshu.Misc.Hooks;

import com.Moshu.Misc.Storage.Messages;
import com.Moshu.Misc.Storage.Settings;
import com.Moshu.Misc.Utils;
import com.Moshu.TreasureHunt.Core.Treasure;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class DiscordWebhook {

    public enum DiscordTreasureEventType {
        SPAWN,
        CLAIM
    }

    private String url = "";
    private static DiscordWebhook instance;
    private JsonObject TREASURE_MESSAGES;
    private String TREASURE_SPAWN_PAYLOAD = "";
    private String TREASURE_CLAIM_PAYLOAD = "";

    private DiscordWebhook() {
    }

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("MysticTreasures");

    public static DiscordWebhook getInstance() {
        synchronized (DiscordWebhook.class) {
            if (instance == null) {
                instance = new DiscordWebhook();
            }
        }

        return instance;

    }

    public void init() {

        setURL(Settings.getString("discord-webhook-url"));
        loadPayload();

    }

    public void setURL(String url) {
        this.url = url;
    }

    /**
     * Loads the Discord Webhook Payload from the config file
     */
    public void loadPayload() {

        if (getURL().isEmpty()) return;

        plugin.getLogger().info("Loading Discord Webhook payload for URL: " + getURL() + "..");

        File jsonFile = new File(plugin.getDataFolder(), "discord-webhook.json");

        try {

            try (Reader reader = new InputStreamReader(Files.newInputStream(jsonFile.toPath()), StandardCharsets.UTF_8)) {
                TREASURE_MESSAGES = JsonParser.parseReader(reader).getAsJsonObject();
            }

            TREASURE_CLAIM_PAYLOAD = TREASURE_MESSAGES.get("treasure-claim").toString();
            TREASURE_SPAWN_PAYLOAD = TREASURE_MESSAGES.get("treasure-spawn").toString();

        } catch (IOException e) {

            plugin.getLogger().warning("Could not load Discord Webhook Payload");
            TREASURE_CLAIM_PAYLOAD = "Messages couldn't be loaded.";
            TREASURE_SPAWN_PAYLOAD = "Messages couldn't be loaded.";

        }

        plugin.getLogger().info("Loaded Discord Webhook payload: " + TREASURE_SPAWN_PAYLOAD.substring(0, 40) + "..");

    }

    public String getURL() {
        return this.url;
    }

    public String getPayload(DiscordTreasureEventType t) {
        String payload = t == DiscordTreasureEventType.SPAWN ? TREASURE_SPAWN_PAYLOAD : TREASURE_CLAIM_PAYLOAD;
        return payload != null ? payload : "";
    }

    /**
     * Applies placeholders to the payload
     *
     * @param t    the treasure object
     * @param type the type of treasure event
     * @return the payload with applied placeholders
     */
    private String getPayloadWithAppliedPlaceholders(Treasure t, DiscordTreasureEventType type) {

        String itemRewards = sanitizeForJson(ChatColor.stripColor(t.getTreasureData().getSanitizedRewards(5)));
        String commandRewards = sanitizeForJson(ChatColor.stripColor(t.getTreasureData().getSanitizedCommandRewards(5)));
        String treasureKeepers = sanitizeForJson(ChatColor.stripColor(t.getTreasureData().getSanitizedTreasureKeepers(5)));

        Location loc = t.getLocation();
        String treasureName = sanitizeForJson(ChatColor.stripColor(t.getTreasureData().getTreasureName()));
        int duration = t.getTreasureData().getDuration();
        boolean requiresKey = t.getTreasureData().getTreasureKey().requiresKey();

        String participants = sanitizeForJson(ChatColor.stripColor(String.join(", ", t.getParticipantsNames())));

        List<UUID> sorted = t.getSortedPlayersByDamage();
        StringBuilder top3Builder = new StringBuilder();
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(sorted.get(i));
            String name = op.getName() != null ? op.getName() : "Unknown";
            top3Builder.append(name);
            if (i < Math.min(3, sorted.size()) - 1) {
                top3Builder.append(", ");
            }
        }
        String top3 = sanitizeForJson(top3Builder.toString());

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        int offset = t.getTreasureData().getCoordsNearTreasure();
        int x_offset = x + Utils.randInt(-offset, offset);
        int z_offset = z + Utils.randInt(-offset, offset);

        String payload = getPayload(type);
        payload = payload.replace("{item-rewards}", itemRewards)
                .replace("{now}", Instant.now().toString())
                .replace("{treasure-winner}", participants.isEmpty() ? "None" : participants)
                .replace("{treasure-top-3}", top3.isEmpty() ? "None" : top3)
                .replace("{command-rewards}", commandRewards)
                .replace("{treasure-keepers}", treasureKeepers)
                .replace("{treasure-name}", treasureName)
                .replace("{treasure-duration}", duration + "")
                .replace("{treasure-world}", loc.getWorld().getName())
                .replace("{treasure-requires-key}", requiresKey ? ChatColor.stripColor(Messages.get("menu-yes")) : ChatColor.stripColor(Messages.get("menu-no")))
                .replace("{treasure-coords}", x + ", " + y + ", " + z)
                .replace("{x}", x + "")
                .replace("{y}", y + "")
                .replace("{z}", z + "")
                .replace("{x-offset}", x_offset + "")
                .replace("{z-offset}", z_offset + "");

        return payload;
    }

    private String sanitizeForJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Sends a Discord Webhook Message
     *
     * @param type the type of treasure event
     * @param t    the treasure object
     */
    public void sendWebhookMessage(DiscordTreasureEventType type, Treasure t) {

        if (getURL().isEmpty() || getURL().contains("Error")) return;

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
            if (responseCode >= 400) {
                plugin.getLogger().warning("Discord Webhook returned error code: " + responseCode);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Could not send Discord Webhook Message - invalid URL or the connection has failed");
            e.printStackTrace();
        }

    }

}
