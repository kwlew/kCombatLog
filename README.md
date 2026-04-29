# kCombatLog

**kCombatLog** is a Paper plugin that adds combat-tagging to prevent players from escaping fights by logging out.

When two players engage in combat, both are tagged for a configurable duration. If a tagged player quits, they are killed and a server-wide message is broadcast.

## Features

- Combat tagging on melee and projectile PvP hits
- Configurable combat duration
- Optional command blocking while in combat
- Optional action-bar countdown while tagged
- Logout punishment (combat log kill)
- PlaceholderAPI expansion support
- bStats integration

## Requirements

- Paper API `26.1.2`
- Java `25` toolchain
- Optional: PlaceholderAPI

## Installation

1. Build the plugin JAR.
2. Put the built JAR in your server `plugins/` folder.
3. Start/restart the server.

## Configuration

`config.yml`:

```yml
messages:
  combat-log: true
  action-bar: true

combat-log:
  enable: true
  commands: false
  delay: 15
```

- `messages.combat-log`: enables chat messages related to combat logging
- `messages.action-bar`: shows combat timer in the action bar
- `combat-log.enable`: enables/disables combat-log punishment logic
- `combat-log.commands`: allows/disallows commands while tagged
- `combat-log.delay`: combat tag duration in seconds

## Placeholders (PlaceholderAPI)

Identifier: `%kcombatlog_<placeholder>%`

- `%kcombatlog_in_combat%` -> `true` or `false`
- `%kcombatlog_remaining_time%` -> remaining seconds
- `%kcombatlog_remaining%` -> remaining seconds

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `kCombatLog.player` | `true` | Player is affected by combat logging |
| `kCombatLog.reload` | `op` | Reserved for reload/admin workflows |
| `kCombatLog.admin` | `op` | Bypasses combat restrictions and includes admin rights |

## Build from source

Windows:

```powershell
.\gradlew.bat shadowJar
```

Output JAR:

`build\libs\kCombatLog-<version>.jar`

Run local Paper test server:

```powershell
.\gradlew.bat runServer
```

## Notes

- This project currently defines permissions and configuration reload helpers, but no public plugin command is declared in `plugin.yml`.
- PlaceholderAPI and `kFriend` are soft dependencies in `plugin.yml`.
