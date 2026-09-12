# TotemPowers

Production-oriented Paper 1.21.11 / Java 21 plugin implementing 11 permanent, randomly assigned Totems.

## Requirements
- Paper 1.21.11
- Java 21
- Gradle 8.10+ for local builds

## Installation
1. Build the project with `gradle clean build`, or download the JAR artifact from GitHub Actions.
2. Put `TotemPowers.jar` into your server's `plugins/` folder.
3. Restart the server.
4. Give players the included `resource-pack/` as your server resource pack if desired.

## First join
A player without an assigned Totem receives one uniformly random Totem from the enabled list. The assignment is stored in the player's PersistentDataContainer and is not rerolled on relog, restart, death, or world changes. The physical custom Totem is added to the player's inventory.

## Equipment
Abilities work only while the player's assigned Totem is held in the main hand or off-hand. Moving it into another inventory location disables abilities immediately.

## Controls
- Right Click: Active Skill I
- Shift + Right Click: Active Skill II

## Commands
- `/totem info`
- `/totem info <player>`
- `/totem reroll <player>`
- `/totem give <player> <totem>`
- `/totem remove <player>`
- `/totem restore <player>`
- `/totem reload`

Permission: `totempowers.admin`

## Totems and abilities
### Ender
Passives: Ender Step; Void Sense. Skills: Void Blink (12s); Ender Rift (28s).
### Warden
Passives: Sculk Sense; Warden's Resolve. Skills: Sonic Boom (18s); Dark Pulse (30s).
### Wither
Passives: Witherborn; Decay Aura. Skills: Wither Blast (16s); Wither Nova (35s).
### Blaze
Passives: Flameborn; Burning Touch. Skills: Flame Burst (12s); Inferno Ring (30s).
### Storm
Passives: Stormcharged; Static Guard. Skills: Lightning Strike (15s); Thunder Dash (25s).
### Frost
Passives: Frozen Heart; Frost Aura. Skills: Frost Bolt (10s); Absolute Zero (32s).
### Tide
Passives: Ocean's Blessing; Tidal Armor. Skills: Tidal Spear (12s); Tidal Wave (28s).
### Shadow
Passives: Shadowstep; Umbral Protection. Skills: Shadow Veil (25s); Shadow Strike (22s).
### Dragon
Passives: Dragon's Might; Dragon's Fury. Skills: Dragon Breath (25s); Dragon Dive (40s).
### Soul
Passives: Soulbound; Soul Guardian. Skills: Soul Flame (14s); Soul Rebirth (45s).
### Magma
Passives: Magma Core; Molten Movement. Skills: Magma Burst (12s); Magma Eruption (32s).

## Configuration
`config.yml` controls first-join roll, skill enabling, each Totem's enabled state, and cooldowns. Disabled Totems are automatically excluded from random selection. If all are disabled, the roll fails safely.

## GitHub Actions
Push the repository to GitHub, open **Actions**, select the build workflow, and download the `TotemPowers` artifact.

## Resource pack
The pack contains eleven original pixel-art item textures and item-model definitions. The plugin assigns each item an `item_model` component using its internal Totem ID. Do not rename a normal vanilla Totem to turn it into a custom Totem; identification uses PDC.
