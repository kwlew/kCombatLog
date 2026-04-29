package dev.kwlew.kCombatLog.listeners.players;

import dev.kwlew.kCombatLog.kernel.Inject;
import dev.kwlew.kCombatLog.listeners.ListenerComponent;
import dev.kwlew.kCombatLog.managers.MessageManager;
import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DeathListener implements ListenerComponent {

    private final JavaPlugin plugin;
    private final CombatManager combat;
    private final MessageManager messages;
    private final ConfigManager config;

    @Inject
    public DeathListener(JavaPlugin plugin, CombatManager combat, MessageManager messages, ConfigManager config) {
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
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (combat.isCombatLogged(player)) {
            event.deathMessage(null);
            return;
        }

        if (combat.isTagged(player)) {
            combat.remove(player);
            messages.send(player, "combat.expired");
        }
    }
}
