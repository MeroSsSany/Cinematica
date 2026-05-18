# 🎬 Cinematica *(Early Development)*

> **Make your stories and movies come alive in Minecraft.**

**Cinematica** is a fully configurable narrative and cinematic engine for Minecraft, designed to transform your worlds into playable stories and interactive movies.

Unlike traditional mods, Cinematica doesn’t add new blocks, items, or dimensions. Instead, it provides creators with a robust toolkit to craft custom cutscenes, text layouts, dynamic audio streams, and automated script sequences directly within the game pipeline.

> [!WARNING]
> 
> **Early Development Notice**
> This mod is currently in active, early development. Core rendering engines, math frameworks, and asset systems are being built from scratch. Feature sets are subject to change before the first official release.

---

## 🚀 Planned Features

* **Cutscenes & Intros:** Full control over camera matrices, smooth vector interpolation, and custom image slide rendering with advanced visual effects like CPU-driven box blurring.
* **Dialogue System:** Classic floating text layout systems with automated text wrap, variable typing speeds, and sub-pixel matrix translation for smooth cinematic text drift.
* **Dynamic Audio Pipeline:** Thread-isolated background music streaming featuring precise fade-in/fade-out states, multi-channel management, and ambient sound suppression.
* **Credits & Outros:** Multiple presentation styles, including classic vertical scrolling text, image galleries with subtitle overlays, or complex sequences combining both.
* **Custom Death Screens:** Replaces standard Minecraft death screens with narrative-driven slideshows, context-aware attacker statistics, and environmental data placeholders (`$player`, `$attacker`, `$dimension`).
* **Event & Trigger System:** A flexible JSON-driven command framework capable of altering entity AI, forcing camera positions, playing precise audio cues, or stepping through timeline stages.

---

## 📊 Development Progress

| Feature System               | Completion | Notes / Status                                                                            |
|------------------------------|:----------:|-------------------------------------------------------------------------------------------|
| **Independent Audio System** |   `100%`   | Forked and optimized from *Startup Music Tone*.                                           |
| **Death Screens**            |   `100%`   | Operational subset of slideshows. Bound via `/cinematica attach <entities> "<slideshow>"` |
| **Slideshow Engine**         |   `100%`   | Fully working.                                                                            |
| **Credits Screens**          |   `97%`    | Core mechanics complete; planning feature-set expansions.                                 |
| **Dialogue System**          |   `50%`    | **Active Work in Progress.**                                                              |

---

## 🛠️ Use Cases

Cinematica is designed to give maximum control to mapmakers, server networks, and storytellers:

* **Story-Driven Adventure Maps:** Build immersive RPG experiences without relying on thousands of clunky, performance-heavy command blocks.
* **Interactive Recreations:** Bring your favorite movies, books, or historical events to life inside a 3D block space.
* **Educational Simulations:** Craft engaging interactive scenarios covering history, survival, or social sciences.
* **Branching Choices:** Design fully playable cinematic visual novels where player choices dictate structural outcomes.

---

## 📜 License

This project is licensed under the terms of the **GNU Lesser General Public License v3.0 (LGPL-3.0)**.