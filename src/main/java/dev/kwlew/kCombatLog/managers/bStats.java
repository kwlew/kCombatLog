package dev.kwlew.kCombatLog.managers;


import dev.kwlew.kCombatLog.kernel.LifecycleComponent;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class bStats implements LifecycleComponent {

    private final JavaPlugin plugin;

    public bStats(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        int pluginId = 30998;
        Metrics metrics = new Metrics(plugin, pluginId);
        plugin.getLogger().info("Connected to bStats.");
    }
}
