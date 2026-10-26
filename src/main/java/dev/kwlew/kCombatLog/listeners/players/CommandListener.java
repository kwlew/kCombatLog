package dev.kwlew.kCombatLog.listeners.players;

import dev.kwlew.kCombatLog.kernel.Inject;
import dev.kwlew.kCombatLog.listeners.ListenerComponent;
import dev.kwlew.kCombatLog.managers.MessageManager;
import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import dev.kwlew.kCombatLog.managers.permission.PermissionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandListener implements ListenerComponent {

    private final JavaPlugin plugin;
    private final CombatManager combat;
    private final MessageManager messages;
    private final ConfigManager config;
    private final PermissionManager perms;

    @Inject
    public CommandListener(JavaPlugin plugin, CombatManager combat, MessageManager messages, ConfigManager config, PermissionManager perms) {
        this.plugin = plugin;
        this.combat = combat;
        this.messages = messages;
        this.config = config;
        this.perms = perms;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!config.getCommandBlockingEnabled()) return;
        if (perms.admin(player)) return;
        if (!combat.isTagged(player)) return;

        String command = event.getMessage().split(" ")[0];
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        command = command.toLowerCase();

        int namespace = command.indexOf(':');
        if (namespace != -1) {
            command = command.substring(namespace + 1);
        }

        boolean listed = config.getCommandBlockingList().contains(command);
        if (config.getCommandBlockingWhitelist() == listed) return;

        messages.send(player, "combat.in-combat");
        event.setCancelled(true);
    }
}
