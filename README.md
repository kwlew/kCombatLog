# kCombatLog

**kCombatLog** is a Paper plugin that adds combat-tagging to prevent players from escaping fights by logging out.

When two players engage in combat, both are tagged for a configurable duration. If a tagged player quits, they are punished (killed by default) and a server-wide message is broadcast.

## Features

- Combat tagging on melee and projectile PvP hits
- Optional combat tagging with mobs (`combat-log.enable-entities`)
- Hits cancelled by other plugins (for example spawn or claim protection) do not tag anyone
- Disabled worlds where no tagging happens (`combat-log.disabled-worlds`)
- WorldGuard support: no tagging in regions where `pvp` is set to `deny`
- Citizens NPCs are ignored (entities carrying the `NPC` metadata flag)
- Command blocking while in combat, as a whitelist or a blacklist
- Ender pearl blocking, or an ender pearl cooldown, while in combat
- Riptide, elytra, and flight blocking while in combat
- Configurable logout punishment: kill, drop inventory, and/or run console commands
- Logouts caused by kicks (and by server shutdown) are not punished
- Action-bar countdown, boss-bar countdown, sounds, and titles while tagged
- Admins bypass tagging, command blocking, and combat restrictions
- Reloadable configuration with `/combatlog reload`; new config keys are added automatically
- PlaceholderAPI expansion support
- bStats integration

## Requirements

- Paper or Purpur `1.20.6` through `26.x`
- Java `21` or newer
- Optional: PlaceholderAPI (for placeholders)
- Optional: WorldGuard (for PvP-deny regions)

## Installation

1. Build the plugin JAR.
2. Put the built JAR in your server `plugins/` folder.
3. Start/restart the server.

## Configuration

`config.yml`:

```yml
# Sends the update notice to admins when they join.
update-notice: true

messages:
  # Sends a chat message if the player logged of in combat.
  combat-log: true
  # Shows a message on the action bar with a timer.
  action-bar: true
  # Shows a boss bar with the remaining combat time.
  boss-bar: false

combat-log:
  # Turning this to off completely disables the combat part with the players.
  enable: true
  # Enable combat with mobs. (Useful for a survival server to prevent logging to escape death.)
  enable-entities: false
  # Worlds where combat tagging is disabled.
  disabled-worlds: []
  command-blocking:
    # Block commands while in combat.
    enabled: true
    # whitelist = only listed commands are allowed; blacklist = only listed commands are blocked.
    mode: whitelist
    # Commands this applies to (without the slash).
    list:
      - msg
      - r
      - tell
  restrictions:
    # Prevent tagged players from throwing ender pearls.
    block-ender-pearls: false
    # Prevent tagged players from using Riptide-enchanted tridents.
    block-riptide: false
    # Seconds of ender pearl cooldown applied after a tagged player throws one (0 to disable).
    ender-pearl-cooldown: 0
    # Prevent tagged players from gliding with an elytra.
    block-elytra: false
    # Prevent tagged survival/adventure players from flying.
    block-flight: false
  # Amount of time the player stays in combat.
  delay: 10

effects:
  # Sound played when entering combat (empty to disable).
  tag-sound: "minecraft:entity.experience_orb.pickup"
  # Sound played when leaving combat (empty to disable).
  untag-sound: "minecraft:block.note_block.pling"
  # Shows a title when entering combat.
  titles: false

punishment:
  # Kill the player when they log out in combat.
  kill: true
  # Drop the player's inventory on logout (only used when kill is false).
  drop-inventory: false
  # Commands run from the console on logout, <player> is replaced with the player's name.
  commands: []
  # Don't punish players who were kicked.
  exempt-kicks: true
```

- `update-notice`: shows the update notice to admins on join
- `messages.combat-log`: enables the chat message broadcast when a player logs out in combat
- `messages.action-bar`: shows the combat timer in the action bar
- `messages.boss-bar`: shows a boss bar with the remaining combat time
- `combat-log.enable`: enables/disables the combat-log logic
- `combat-log.enable-entities`: also tags players hit by mobs (and mobs hit by players are not tracked)
- `combat-log.disabled-worlds`: list of world names where no tagging happens
- `combat-log.command-blocking.enabled`: enables/disables command blocking while tagged
- `combat-log.command-blocking.mode`: `whitelist` (only listed commands work) or `blacklist` (only listed commands are blocked)
- `combat-log.command-blocking.list`: command names without the slash
- `combat-log.restrictions.block-ender-pearls`: prevents tagged players from throwing ender pearls when `true`
- `combat-log.restrictions.block-riptide`: prevents tagged players from using Riptide-enchanted tridents when `true`
- `combat-log.restrictions.ender-pearl-cooldown`: seconds of ender pearl cooldown applied after a tagged player throws one
- `combat-log.restrictions.block-elytra`: prevents tagged players from gliding with an elytra when `true`
- `combat-log.restrictions.block-flight`: prevents tagged survival/adventure players from flying when `true`
- `combat-log.delay`: combat tag duration in seconds
- `effects.tag-sound` / `effects.untag-sound`: sound keys played on entering/leaving combat (empty to disable)
- `effects.titles`: shows a title when entering combat
- `punishment.kill`: kills the player when they log out in combat
- `punishment.drop-inventory`: drops the player's inventory on logout (only used when `kill` is `false`)
- `punishment.commands`: console commands run on logout, `<player>` is replaced with the player's name
- `punishment.exempt-kicks`: players who were kicked are not punished

Server shutdown is never punished, regardless of config.

Messages are configured in `messages.yml` (MiniMessage format).

### Migrating from 1.3.x

The old `combat-log.commands` key was replaced by `combat-log.command-blocking`. It is migrated automatically on startup and removed from `config.yml`.

## Commands

Base command: `/combatlog`, alias `/ct`

| Command | Permission | Description |
| --- | --- | --- |
| `/combatlog reload` | `kCombatLog.reload` | Reloads `config.yml` and `messages.yml` |
| `/combatlog tag <player>` | `kCombatLog.admin` | Puts a player in combat |
| `/combatlog untag <player>` | `kCombatLog.admin` | Removes a player from combat |
| `/combatlog status [player]` | none (self); `kCombatLog.admin` (other players) | Shows how long a player is still in combat |

## Placeholders (PlaceholderAPI)

Identifier: `%kcombatlog_<placeholder>%`

| Placeholder | Output |
| --- | --- |
| `%kcombatlog_in_combat%` | `true` or `false` |
| `%kcombatlog_remaining_time%` | remaining seconds (rounded up) |
| `%kcombatlog_remaining%` | remaining seconds (rounded up), same as `remaining_time` |
| `%kcombatlog_opponent%` | name of the player or mob the player is fighting, empty if not in combat |
| `%kcombatlog_opponent_health%` | opponent's health with one decimal, empty if not in combat or the opponent is not living |

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `kCombatLog.player` | `true` | Player is affected by combat logging |
| `kCombatLog.reload` | `op` | Allows `/combatlog reload` |
| `kCombatLog.admin` | `op` | Bypasses tagging, command blocking, and combat restrictions; allows `tag`, `untag`, and `status <player>`; includes `kCombatLog.reload` |

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

- Soft dependencies (declared in `plugin.yml`): PlaceholderAPI and WorldGuard. Both are optional.
- The plugin is compiled against Paper `1.20.6` so the JAR stays compatible with newer Paper versions up to `26.x`.
