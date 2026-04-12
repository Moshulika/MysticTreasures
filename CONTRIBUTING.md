# Contributing to MysticTreasures 💎

We're excited that you're interested in contributing to **MysticTreasures**! This guide will help you get started with the development process and ensure your contributions meet our standards.

---

## 🛠️ Prerequisites

Before you begin, ensure you have the following installed:
- **Java Development Kit (JDK) 8 or higher**
- **Maven 3.8+**
- A Git client

## 🚀 Getting Started

1.  **Fork the repository** on GitHub.
2.  **Clone your fork** locally:
    ```bash
    git clone https://github.com/YOUR_USERNAME/MysticTreasures.git
    cd MysticTreasures
    ```
3.  **Create a new branch** for your feature or bug fix:
    ```bash
    git checkout -b feature/your-feature-name
    ```

## 📜 Coding Standards

To maintain a consistent and high-quality codebase, please adhere to these standards:

- **Naming:** Follow standard Java naming conventions (e.g., `CamelCase` for classes, `camelCase` for variables and methods).
- **Style:** Use 4 spaces for indentation.
- **License Headers:** All new Java files **MUST** include the `Apache License 2.0 + Commons Clause` header. You can find this header in existing files or use the provided scripts to add it automatically.
- **Quality Checks:** Before submitting, run our quality scripts:
  - **Windows:** `powershell ./scripts/quality-check.ps1`
  - **Linux/macOS:** `./scripts/quality-check.sh`
  - These scripts run JUnit tests, Checkstyle, and SpotBugs. Your PR should not have any linting errors or failing tests.

## ✍️ Contributor License Agreement (CLA)

By contributing to MysticTreasures, you agree that your contributions will be licensed under the project's Apache License 2.0 + Commons Clause. 

We use a **CLA bot** to manage our agreements. When you submit your first Pull Request, the bot will automatically check if you have signed the CLA. If not, it will provide a link for you to review and sign it electronically. This is a one-time process for all your contributions to this repository.

## 🧪 Testing

We value test-driven development. If you're adding a new feature or fixing a bug:
- **Write unit tests** in the `src/test/java` directory.
- Use **MockBukkit** for simulating the Bukkit environment where possible.
- Ensure all existing tests pass before submitting.

## 📥 Submitting a Pull Request

1.  **Commit your changes** with a clear and concise message.
2.  **Push your branch** to your fork:
  ```bash
  git push origin feature/your-feature-name
  ```
3.  **Open a Pull Request** (PR) on the main repository.
4.  Fill out the **PR Template** provided (it should load automatically).
5.  Wait for review. We may ask for changes or clarifications.

---
Thank you for helping make **MysticTreasures** better! 🚀
