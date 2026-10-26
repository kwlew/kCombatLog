package dev.kwlew.kCombatLog.managers.permission;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionManager {

    public boolean admin(CommandSender sender) {
        return sender.hasPermission("kCombatLog.admin");
    }

    public boolean reload(CommandSender sender) {
        return sender.hasPermission("kCombatLog.reload");
    }

    public boolean combat(Player player) {
        return player.hasPermission("kCombatLog.player");
    }
}
