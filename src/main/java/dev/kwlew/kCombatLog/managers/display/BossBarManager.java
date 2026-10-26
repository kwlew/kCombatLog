package dev.kwlew.kCombatLog.managers.display;

import dev.kwlew.kCombatLog.kernel.Inject;
import dev.kwlew.kCombatLog.kernel.LifecycleComponent;
import dev.kwlew.kCombatLog.managers.MessageManager;
import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarManager implements LifecycleComponent {

    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final MessageManager messages;

    @Inject
    public BossBarManager(JavaPlugin plugin, ConfigManager config, MessageManager messages) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public void shutdown() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            hide(player);
        }
        bars.clear();
    }

    public void update(Player player, long remainingMillis) {
        if (!config.getBossBarEnabled()) {
            hide(player);
            return;
        }

        long remaining = Math.max(0, (remainingMillis + 999) / 1000);
        float progress = Math.max(0f, Math.min(1f, remainingMillis / (config.getCombatDuration() * 1000f)));

        BossBar bar = bars.get(player.getUniqueId());
        if (bar == null) {
            bar = BossBar.bossBar(Component.empty(), progress, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
            bars.put(player.getUniqueId(), bar);
            player.showBossBar(bar);
        }

        bar.name(messages.getRaw("combat.boss-bar",
                messages.placeholder("remaining", String.valueOf(remaining))
        ));
        bar.progress(progress);
    }

    public void hide(Player player) {
        BossBar bar = bars.remove(player.getUniqueId());
        if (bar == null) return;

        player.hideBossBar(bar);
    }
}
