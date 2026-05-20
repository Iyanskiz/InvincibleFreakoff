# 👊 Invincible: 2D Java Fighting Engine 📝

A high-octane, custom-built 2D fighting game engine developed in Java. Experience intense local 1v1 combat featuring the powerhouse characters of the *Invincible* universe.

## 🕹️ Overview
This engine utilizes a specialized sprite-based animation system, dynamic environmental hazards, and a robust character selection UI. It is designed to handle custom physics, gravity, and frame-perfect collision detection.

---

## 🎮 Battle Controls
Designed for local multiplayer action.

| Action | Player 1 (WASD) | Player 2 (Arrows) |
| :--- | :--- | :--- |
| **Movement** | `A` / `D` | `←` / `→` |
| **Jump** | `W` | `↑` |
| **Block (Hold)** | `S` | `↓` |
| **Light Attack** | `F` | `Num 1` |
| **Heavy Attack** | `G` | `Num 2` |
| **Special Ability** | `H` | `Num 3` |
| **Restart Game** | `ENTER` | `ENTER` |

---

## 🛡️ The Roster
Each fighter features unique stats and custom attributes tailored to their lore.

* **Invincible:** The balanced hero. High speed, versatile air game, and quick recovery.
* **Omni-Man:** The heavy hitter. Massive damage output balanced with high tankiness.
* **Thragg:** The Grand Regent. Equipped with specialized armor that reduces all incoming damage by **50%**.
* **Conquest:** The berserker. Extremely aggressive with high-impact, crushing attacks.

---

## 🚀 Key Features
* **Dynamic Stages:** Battle across four random locations, including:
    * *City Under Siege:* Watch out for falling meteors!
    * *Storm's Wrath:* Avoid random lightning strikes.
* **Visual Feedback:** Enhanced gameplay feel via screen flashes, hit particles, and "BLOCKED!" popups.
* **Physics Engine:** Custom-coded logic for gravity, collision detection, and automatic sprite mirroring based on opponent position.
* **Animated UI:** Character selection screen featuring "levitating" sprite previews and dynamic stat cards.

---

## 📂 Technical Setup

### 1. Source Files (`src/`)
* `Frame.java`: The main entry point to launch the application.
* `GamePanel.java`: Manages the core game loop, stage hazards, and physics.
* `Fighter.java`: The abstract base class containing all combat and movement logic.
* `CharacterSelect.java`: The selection menu logic and UI rendering.

### 2. Image Assets (`imgs/`)
Sprites must be placed in a folder named `imgs` at the project root. For the engine to recognize a fighter, the following naming convention is required:

| File Type | Required Naming Convention |
| :--- | :--- |
| **Idle** | `[Name]Idle.png` |
| **Blocking** | `[Name]Block.png` |
| **Walk Forward** | `[Name]Forward.png` |
| **Walk Backward** | `[Name]Backward.png` |
| **Attack** | `[Name]Punch.png` |
| **Damaged** | `[Name]Hit.png` |
| **UI Preview** | `[Name]Levitating.png` |

---
*Developed in Java for high-performance 2D combat.*
