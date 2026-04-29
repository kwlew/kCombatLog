package dev.kwlew.kCombatLog.listeners.players;

import dev.kwlew.kCombatLog.kernel.Inject;
import dev.kwlew.kCombatLog.listeners.ListenerComponent;
import dev.kwlew.kCombatLog.managers.MessageManager;
import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandListener implements ListenerComponent {

    private final JavaPlugin plugin;
    private final CombatManager combat;
    private final MessageManager messages;
    private final ConfigManager config;

    @Inject
    public CommandListener(JavaPlugin plugin, CombatManager combat, MessageManager messages, ConfigManager config) {
        this.plugin = plugin;
        this.combat = combat;
        this.messages = messages;
        this.config = config;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (combat.isTagged(player) && !config.getCommandsInCombatEnabled()) {
            messages.send(player, "combat.in-combat");
            event.setCancelled(true);
        }
    }
}
