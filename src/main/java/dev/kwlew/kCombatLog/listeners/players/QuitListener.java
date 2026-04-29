package dev.kwlew.kCombatLog.listeners.players;

import dev.kwlew.kCombatLog.listeners.ListenerComponent;
import dev.kwlew.kCombatLog.managers.MessageManager;

import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class QuitListener implements ListenerComponent {

    private final JavaPlugin plugin;
    private final MessageManager messages;
    private final CombatManager combat;
    private final ConfigManager config;

    public QuitListener(JavaPlugin plugin, MessageManager messages, CombatManager combat, ConfigManager config) {
        this.plugin = plugin;
        this.messages = messages;
        this.combat = combat;
        this.config = config;
    }


    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (combat.isTagged(player) && config.getCombatLogEnabled()) {
            messages.broadcast("combat.logged",
                    messages.placeholder("player", player.getName())
            );
            combat.CombatLog(player);
            player.kill();
            combat.remove(player);
        }
    }
}
