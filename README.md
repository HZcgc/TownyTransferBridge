# TownyTransferBridge

TownyTransferBridge is a tiny Paper 26.2 plugin for the Towny Reborn Hub. It exposes a safe player command that transfers the player to one fixed Velocity backend through the BungeeCord-compatible plugin messaging channel.

## Requirements

- Paper 26.2
- Java 25
- Velocity with `bungee-plugin-message-channel = true`
- A backend named `TownyReborn` in `velocity.toml`

## Installation

1. Put `TownyTransferBridge-1.0.0.jar` into the Hub's `plugins/` directory.
2. Restart the Hub completely.
3. Leave `target-server: TownyReborn` in `plugins/TownyTransferBridge/config.yml`.
4. Test `/townytransfer` while connected through Velocity.

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

The target server is intentionally read only from the configuration. Players cannot supply an arbitrary backend name.

## Build

```bash
mvn -B clean verify
```

The compiled plugin is written to `target/TownyTransferBridge-1.0.0.jar`.
