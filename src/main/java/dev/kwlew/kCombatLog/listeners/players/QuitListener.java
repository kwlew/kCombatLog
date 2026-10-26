package dev.kwlew.kCombatLog.listeners.players;

import dev.kwlew.kCombatLog.listeners.ListenerComponent;
import dev.kwlew.kCombatLog.managers.MessageManager;

import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import dev.kwlew.kCombatLog.managers.display.BossBarManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class QuitListener implements ListenerComponent {

    private final JavaPlugin plugin;
    private final MessageManager messages;
    private final CombatManager combat;
    private final ConfigManager config;
    private final BossBarManager bossBars;

    public QuitListener(JavaPlugin plugin, MessageManager messages, CombatManager combat, ConfigManager config, BossBarManager bossBars) {
        this.plugin = plugin;
        this.messages = messages;
        this.combat = combat;
        this.config = config;
        this.bossBars = bossBars;
    }


    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        boolean tagged = combat.isTagged(player);
        combat.remove(player);
        bossBars.hide(player);

        if (!tagged) return;
        if (!config.getCombatLogEnabled()) return;
        if (plugin.getServer().isStopping()) return;
        if (config.getPunishmentExemptKicks() && event.getReason() == PlayerQuitEvent.QuitReason.KICKED) return;

        if (config.getMessageChatEnabled()) {
            messages.broadcast("combat.logged",
                    messages.placeholder("player", player.getName())
            );
        }

        punish(player);
    }

    private void punish(Player player) {
        if (config.getPunishmentKill()) {
            combat.CombatLog(player);
            player.setHealth(0.0);
        } else if (config.getPunishmentDropInventory()) {
            PlayerInventory inventory = player.getInventory();
            for (ItemStack item : inventory.getContents()) {
                if (item == null || item.getType().isAir()) continue;
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            inventory.clear();
        }

        for (String command : config.getPunishmentCommands()) {
            String line = command.replace("<player>", player.getName());
            if (line.startsWith("/")) line = line.substring(1);
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), line);
        }
    }
}
