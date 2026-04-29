package dev.kwlew.kCombatLog.managers.permission;

import org.bukkit.entity.Player;

public class PermissionManager {

    public boolean admin(Player player) {
        return player.hasPermission("kCombatLog.admin");
    }

    public boolean reload(Player player) {
        return player.hasPermission("kCombatLog.reload");
    }

    public boolean combat(Player player) {
        return player.hasPermission("kCombatLog.player");
    }
}
