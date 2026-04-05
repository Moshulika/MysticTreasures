# 💎 MysticTreasures

![Version](https://img.shields.io/badge/version-3.1-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
![Java](https://img.shields.io/badge/java-8+-orange.svg)
![Build Status](https://github.com/YourUsername/MysticTreasures/actions/workflows/build.yml/badge.svg)
![Platform](https://img.shields.io/badge/platform-Paper%20%7C%20Spigot-yellow.svg)

A high-performance, feature-rich Treasure Hunt plugin for Minecraft servers. Create engaging, round-based challenges with custom rewards, guardians, and Discord integration.

---

## ✨ Key Features

- **🚀 Performance Optimized:** Minimal CPU impact with intelligent task scheduling and distance calculations.
- **🛡️ Multi-Round System:** Configurable treasure rounds with increasing difficulty and custom guardians.
- **🎁 Flexible Rewards:** Support for Vanilla, ItemsAdder, Nexo, Oraxen, and MMOItems.
- **🔗 Discord Integration:** Real-time webhooks for treasure spawns and claims with clean formatting.
- **📉 Dynamic Debuffs:** Apply effects to players during treasure interactions to spice up the challenge.
- **🗺️ Advanced Spawning:** Precise coordinate support or smart random generation with border/liquid checks.
- **🔄 Auto-Merging Configs:** Missing fields from updates are automatically added while keeping your settings.

## 🛠️ Requirements

- **Paper or Spigot** (1.13+)
- **Java 8 or higher**
- *(Optional)* ItemsAdder, Nexo, Oraxen, MMOItems, DecentHolograms, FancyHolograms, WorldGuard, ProtocolLib.

## 📥 Installation

1. Download the latest `MysticTreasures.jar`.
2. Place it in your server's `plugins` folder.
3. Restart the server to generate configuration files.
4. Customize `config.yml`, `treasure.yml`, and `messages.yml` to your liking.

## 🧪 Development & Testing

We maintain high code quality standards. You can run the following to verify your local changes:

### Quality Checks
- **Windows:** `powershell ./scripts/quality-check.ps1`
- **Linux/macOS:** `./scripts/quality-check.sh`
- Runs JUnit 5 tests via **MockBukkit**, performs **Checkstyle** analysis, and executes **SpotBugs**.

### Mock Server Testing
- **Windows:** `powershell ./scripts/launch-test-server.ps1 "C:/Path/To/Server"`
- **Linux/macOS:** `./scripts/launch-test-server.sh "/path/to/server"`
- Automatically builds the plugin and monitors logs for successful enablement.

## 📜 Commands & Permissions

- `/mt spawn <id>` - Manually spawn a treasure (`mystictreasures.admin`)
- `/mt stop <id>` - Stop an active hunt (`mystictreasures.admin`)
- `/mt menu` - Open the active hunts GUI (`mystictreasures.player`)

---

Developed with ❤️ by **Moshu**.
