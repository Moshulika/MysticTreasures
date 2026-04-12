# ⚙️ MysticTreasures Configuration Guide

This guide provides comprehensive information on how to configure and run the **MysticTreasures** plugin.

---

## 📑 Table of Contents
1. [Global Settings (config.yml)](#-global-settings-configyml)
2. [Treasure Definitions (treasure.yml)](#-treasure-definitions-treasureyml)
3. [Rounds & Rewards (rounds.yml)](#-rounds--rewards-roundsyml)
4. [Messages & Menus (messages.yml)](#-messages--menus-messagesyml)
5. [Discord Integration (discord-webhook.json)](#-discord-integration-discord-webhookjson)
6. [Commands & Permissions](#-commands--permissions)
7. [Placeholders](#-placeholders)

---

## 🌍 Global Settings (config.yml)

The `config.yml` file handles global plugin behavior and restrictions.

### 🔊 Sounds
*   `positive-sound`: Played on successful actions (e.g., `ENTITY_PLAYER_LEVELUP`).
*   `negative-sound`: Played on errors or failures (e.g., `BLOCK_ANVIL_BREAK`).

### ⚔️ Player Behavior Near Treasure
*   `allow-pvp-near-treasure`: If `false`, PVP is disabled in the treasure's vicinity.
*   `allow-flight-near-treasure`: Disables flight (Creative/Fly) near active treasures.
*   `allow-god-near-treasure`: Disables God mode near treasures.
*   `allow-elytra-near-treasure`: Disables Elytra usage near treasures.

### 🛡️ Protection & Conditions
*   `protection-radius`: Radius (in blocks) where griefing is prevented.
*   `min-players-online`: Minimum players required for a random treasure to spawn.
*   `protect-mobs-from-sun`: Prevents undead Treasure Keepers from burning.
*   `potion-effect-radius`: Distance at which players receive configured potion effects.
*   `winner-cooldown`: Minutes a player must wait after winning before claiming another.
*   `max-players-looting`: Maximum concurrent players in a loot chest.

### 🎁 Reward Obfuscation
*   `obfuscate-rewards`: If `true`, rewards appear as "???" until claimed.
*   `obfuscated-reward-item`: The placeholder item used for hidden rewards.

### 📢 Actionbar & Effects
*   `actionbar.enabled`: Toggle the real-time status display.
*   `effects-particles`: Customizable particle types for various plugin events.

### 💾 Data Storage
*   `cooldowns.yml`: This file is used by the plugin to store active player cooldowns. It is not intended for manual editing.

---

## 💎 Treasure Definitions (treasure.yml)

Treasures are defined by unique IDs. Each ID can have its own spawning logic, world, and appearance.

### 🟢 General Settings
*   `world-name`: The world where this treasure type spawns.
*   `treasure-block`: The block used for the treasure (e.g., `ENDER_CHEST`).
*   `duration`: Minutes before the treasure despawns if not claimed.
*   `rounds`: A list of rounds from `rounds.yml` associated with this treasure.
*   `award-method`: How rewards are distributed:
    *   `CHEST`: Traditional loot chest.
    *   `ALL_PLAYERS`: Everyone involved gets rewards.
    *   `HIGHEST_DAMAGE`: Only the top damager wins.
    *   `TOP_X`: Top X damagers win (e.g., `TOP_5`).
    *   `DROP_ON_GROUND`: Items drop at the treasure location.

### 📍 Spawning Logic
*   **Random Task:** Configures background chance-based spawning.
    *   `chance-for-treasure`: % chance per interval.
    *   `interval`: How often (in minutes) the plugin tries to spawn one.
*   **Scheduler:** Fixed-time spawning (e.g., Every Monday at 17:00).
*   **Manual Coords:** Set `spawn-to-certain-coords: true` to use `spawn-coords`.

---

## ⚔️ Rounds & Rewards (rounds.yml)

Rounds define the "challenge" phase of a treasure.

### 🚫 Debuffs
*   `shockwave`: Toggles the knockback effect on interaction.
*   `clicks-to-debuff`: Number of clicks required to trigger the debuff.
*   `potion-effects`: List of effects applied to the player (Format: `EFFECT:LEVEL:TICKS`).

### 🧝 Treasure Keepers (Mobs)
*   Supports Vanilla or MythicMobs.
*   `range`: Number of mobs to spawn (e.g., `3-5`).
*   `equipment`: Full armor and weapon configuration.
*   `drops`: Custom items dropped when the keeper is killed.

### 🎁 Rewards
*   `item-rewards`: Physical items with custom names, lore, and drop chances.
*   `command-rewards`: Console commands executed for the winner (supports `%player%`).

---

## 💬 Messages & Menus (messages.yml)

All user-facing text is located here. It supports:
*   Standard color codes (`&6`, `&l`).
*   Multi-line broadcast templates for spawns and wins.
*   GUI titles and lore for the `/hunt` menus.

### 🌐 Translations
The plugin supports multiple languages. Additional translation files (e.g., `messages_cn.yml`) can be found in the `translations/` folder. To use a different language, replace the contents of `messages.yml` with the desired translation.

---

## 🔗 Discord Integration (discord-webhook.json)

Enable by pasting your Webhook URL in `config.yml`. The JSON file defines the structure of the embeds.

*   **treasure-spawn**: Sent when a treasure appears.
*   **treasure-claim**: Sent when a treasure is successfully looted.
*   **Supported Placeholders:** `{treasure-name}`, `{treasure-world}`, `{treasure-coords}`, `{treasure-winner}`, `{item-rewards}`.

---

## 🛠️ Commands & Permissions

### Commands
*   `/hunt`: Base command.
*   `/hunt help`: Displays all available sub-commands.
*   `/hunt reload`: Reloads all configuration files (Admin).

### Permissions
*   `mystictreasures.hunt`: Access to basic hunt commands (Default: true).
*   `mystictreasures.admin`: Access to management commands (Default: OP).
*   `mystictreasures.bypass`: Bypass cooldowns and restrictions (Default: OP).

---

## ✨ Aesthetics & Visuals (treasure.yml)

Customize how the treasure appears and interacts with the world.

### 🎥 Animations & Effects
*   `treasure-animation`: Visual effect for the chest (Options: `none`, `spiral`, `orb`, `protection`).
*   `treasure-particles`: The particle effect surrounding the treasure (e.g., `COMPOSTER`).
*   `fall-from-the-sky`: If `true`, the treasure falls from the sky with a smoke trail.
*   `fireworks`: Toggles fireworks when the treasure is opened.

### 🗺️ Navigation
*   `waypoint`: (Paper 1.21.6+) Shows a client-side waypoint to players within a certain range.
*   `flare`: Options for `none`, `few`, or `many` particles to help players locate the chest from a distance.

---

## 🛠️ Requirements & Integrations

MysticTreasures integrates with several popular plugins to enhance functionality.

### 📦 Supported Plugins
*   **Holograms:** `DecentHolograms`, `FancyHolograms`.
*   **Custom Items:** `ItemsAdder`, `Oraxen`, `Nexo`, `MMOItems`.
*   **Protection:** `WorldGuard`, `Lands`.
*   **Combat:** `MythicMobs`, `ProtocolLib`, `packetevents`.
*   **Economy:** `Vault`.

---

## 🧩 Placeholders

Use these in `messages.yml` or Holograms:
*   `{x}`, `{y}`, `{z}`: Coordinates of the treasure.
*   `{world}`: World name.
*   `{time}`: Time remaining until despawn.
*   `{remaining_mobs}`: Count of Treasure Keepers still alive.
*   `{player}`: Name of the winner/participant.
*   `{treasure}`: The internal ID of the treasure.
*   `{alias}`: The display name of the treasure.
