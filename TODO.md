# 📝 MysticTreasures TODO v3.1

This list tracks planned improvements, bug fixes, and architectural changes for MysticTreasures.

## Manual testing of all features is needed
This is a major change of core plugins functionalities and this being a large update, in which AI has contributed, it is
required that manual checks are done for all systems.

## 🚀 Performance & Optimization
- [ ] **Cached Settings:** Implement full caching for all configuration values in `Settings.java` to avoid frequent `getConfig()` calls.
- [ ] **Configurable Task Intervals:** Allow users to configure the interval for `TreasureTask` (currently hardcoded to 15s).
- [ ] **Spatial Indexing:** Evaluate if spatial indexing (e.g., QuadTree) is needed for player distance checks if many treasures are active simultaneously.

## 🏗️ Architectural Refactoring
- [ ] **Decompose `Treasure.java`:** This "God Class" should be split into smaller, focused components:
  - `TreasureRenderer`: Handles particles, holograms, and furniture entities.
  - `TreasureRewardHandler`: Manages loot tables and reward distribution.
  - `TreasureEntityManager`: Manages guardians and their lifecycles.
- [ ] **Dependency Injection:** Reduce reliance on static fields (e.g., `Main.plugin`, `Settings.plugin`) and transition to proper constructor injection.
- [ ] **Robust Hooking:** Implement a more resilient abstraction layer for external plugins (ItemsAdder, Nexo, Oraxen, etc.) to handle API changes or missing dependencies gracefully.
- [ ] **Rounds:** Move Round related classes to another folder

## ✨ New Features
- [ ] **Per-Mob Debuffs:** Allow specific debuffs to be applied when fighting certain treasure guardians, rather than just global treasure debuffs.
- [ ] **Dynamic World Balancing:**
  - Improve border calculation for better random spawn logic.
  - Support for version-specific height limits (Min/Max height handling for 1.18+ worlds).
- [ ] **Treasure Tiers:** Formalize treasure tiers with inherited properties and loot tables.

## 🐛 Maybe bugs (To keep in mind, these were done by AI)
- [ ] **World Regex:** Review the world renaming regex logic.
- [ ] **Top Player Iteration:** Fix inconsistencies in how top players are calculated for rewards.
- [ ] **Chance Logic:** Refactor chance calculation to be more intuitive and consistent across different reward types.
- [ ] **Rounds Logic:** Refactor chance calculation to be more intuitive and consistent across different reward types.
- [ ] **Error Handling:** Replace `printStackTrace()` with proper logging levels and user-friendly error messages.

## 🧪 Testing & Quality
- [ ] **Expand Unit Tests:** Increase coverage for reward distribution logic and location generation.
- [ ] **Integration Tests:** Add more MockBukkit tests for complex multi-plugin interactions.

---
*Last Updated: April 2026*
