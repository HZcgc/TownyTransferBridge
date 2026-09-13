# TownyTransferBridge

TownyTransferBridge is a tiny Paper 26.2 plugin for the Towny Reborn Hub. It exposes a safe player command that transfers the player to one fixed Velocity backend through the BungeeCord-compatible plugin messaging channel. It can also keep selected Citizens NPCs permanently invisible by numeric NPC ID.

## Requirements

- Paper 26.2
- Java 25
- Velocity with `bungee-plugin-message-channel = true`
- A backend named `TownyReborn` in `velocity.toml`
- Citizens (only required for the persistent NPC invisibility feature)

## Installation

1. Put `TownyTransferBridge-1.1.0.jar` into the Hub's `plugins/` directory.
2. Restart the Hub completely.
3. Leave `target-server: TownyReborn` in `plugins/TownyTransferBridge/config.yml`.
4. Test `/townytransfer` while connected through Velocity.

## Permanent Citizens NPC invisibility

Find the Citizens NPC ID with `/npc list` or `/npc info`, then add it to `config.yml`:

```yml
invisible-npcs:
  enabled: true
  ids:
    - 12
  hide-nameplates: true
  reapply-interval-ticks: 20
```

Apply changes without restarting the Hub:

```text
/townytransfer reload
```

The plugin reapplies invisibility when the NPC spawns, when its chunk loads and during the configured safety interval. Removing an ID and reloading restores the NPC's original visibility.

## MythicMobs NPC

Add this entry beneath the relevant mob's `Skills:` section:

```yml
- command{c="townytransfer";asTarget=true;asOp=false} @trigger ~onInteract
```

Then run `/mm reload`. The command must not contain a leading slash.

## Commands and permissions

| Command | Permission | Default |
| --- | --- | --- |
| `/townytransfer` (`/joinsmp`) | `townytransfer.use` | Everyone |
| `/townytransfer reload` | `townytransfer.admin` | Operators |

The target server is intentionally read only from the configuration. Players cannot supply an arbitrary backend name.

## Build

```bash
mvn -B clean verify
```

The compiled plugin is written to `target/TownyTransferBridge-1.1.0.jar`.
