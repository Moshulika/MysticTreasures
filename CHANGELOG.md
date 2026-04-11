# Changelog

All notable changes to the MysticTreasures plugin are documented here.

## [3.1.0] - 2026-04-05

### 🌟 For Users (Plugin Configuration & Behavior)

#### 🚀 New Multi-Round System
The treasure hunt system has been completely overhauled to support multiple rounds. Instead of a single wave of mobs, you can now define a sequence of challenges.
*   **Modular Rounds:** All round configurations (mobs, rewards, debuffs) have moved from `treasure.yml` to a new `/rounds/` folder. This allows you to create separate `.yml` files to organize your rounds better.
*   **Per-Round Rewards:** You can now give rewards at the end of *every* round, not just at the very end of the treasure hunt.
*   **Per-Round Mobs:** Each round can have its own `mobs` section, allowing you to customize the mobs spawned during that round.
*   **Per-Round Award Methods:** Each round can have its own `award-method`. For example, Round 1 can drop items on the ground, while the Final Round opens a loot chest.
*   **Flexible Debuffs:** Debuffs are now round-specific. You can configure different potion effects or shockwaves for different stages of the hunt.

#### 🔑 Treasure Key & Locking
*   **Commitment First:** If a treasure requires a key, it must now be used to **start the first round**. This ensures that players "pay the entry fee" before engaging in combat, preventing them from playing early rounds for free.
*   **One-Time Use:** Once unlocked at the start, the treasure remains unlocked for all subsequent rounds and the final looting phase.

#### 🛠️ General Improvements & Fixes
*   **Reward Precision:** Fixed an issue where reward chances were calculated incorrectly.
*   **Hologram Stability:** Improved holograms with unique, world-specific IDs to prevent overlapping. Support added for world names containing special characters.
*   **Mass Command:** Added `/hunt stop all` to immediately terminate all active treasure hunts across all worlds.
*   **Performance Optimization:** Migrated distance calculations to use `distanceSquared`, significantly reducing CPU overhead during active hunts.
*   **Enhanced Animation:**
    *   Added "Touchdown" detection for falling treasures, ensuring they spawn correctly on ground, water, or lava.
    *   Improved animation watchdog to prevent "stuck" armor stands.
    *   Spawning entities are now invulnerable until the animation completes.
*   **Cooldown Sync:** Cooldowns for treasure types are now tracked individually per identifier, preventing different treasure types from blocking each other.
*   **Mob suffocation:** If a mob is stuck inside blocks (suffocation), it will be teleported to the nearest safe location.

#### ⚠️ Configuration Requirements
*   **Treasure Config:** The `rounds` field in `treasure.yml` is now a **list of IDs** (e.g., `rounds: ["round_1", "round_2"]`).
*   **Error Logging:** The plugin will now clearly log errors in the console if a treasure is missing rounds or if a round ID doesn't exist in your `/rounds/` folder.

---

### 🛠️ For Developers (Internal Architecture)

#### 🏗️ Object-Oriented Refactoring
*   **`RoundData` & `RoundManager`:** Encapsulated round-specific logic into new models. `RoundManager` handles recursive file loading from the `/rounds/` directory.
*   **Registry Pattern:** Implemented `TreasureRoundRegistry` to manage the lifecycle and sequencing of rounds within a specific `Treasure` instance.
*   **Data Aggregation:** `TreasureData` now dynamically aggregates mobs and rewards from linked `RoundData` objects for display in menus.

#### ⚔️ Combat & Reward Lifecycle
*   **`TreasureRoundController`:** Centralized logic for transitioning between rounds. It now handles the automatic distribution of previous-round rewards when a new round is triggered.
*   **Overloaded Reward Methods:** `Treasure.java` now features overloaded `giveRewards(RoundData)` methods to allow scoped reward distribution.
*   **Decoupled Debuffs:** Removed `isEnabled` and `respawn-mobs` flags from `TreasureDebuff`. Logic is now implicitly driven by the presence of a `debuff` section.

#### 🧪 Testing & CI/CD
*   **Unit Testing Suite:** Integrated JUnit 5 and MockBukkit. Added comprehensive tests for plugin lifecycle, distance logic, reward distribution, and round sequencing.
*   **Automated Workflows:** Added GitHub Actions for automated Maven builds (JDK 21) and Qodana code quality scans.
*   **Mock Server Script:** Added `launch-test-server.ps1` for rapid local testing in a controlled environment.

#### 🛡️ Interaction & Optimization
*   **Mob Safety Logic:** Added `onSuffocation` listener in `TreasureEvents` to handle guardian teleportation. Refactored `Utils` to include `getNearLocation` and `getNearLocationInside` for centralized safe location finding.
*   **`TreasureEvents` Refactor:** Significant updates to `handleInteraction` and `onBreak`. Key consumption and `LOCKED` state transitions shifted to the start of the interaction chain.
*   **State Persistence:** The `Treasure` instance now survives inter-round transitions, only executing `remove()` after the final round's rewards are processed.
*   **Modern Java:** Project now targets JDK 21 while maintaining high engineering standards through SpotBugs and Checkstyle enforcement.
