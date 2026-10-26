package dev.kwlew.kCombatLog.managers.hooks;

import dev.kwlew.kCombatLog.kernel.LifecycleComponent;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldGuardHook implements LifecycleComponent {

    private final JavaPlugin plugin;

    private WorldGuardBridge bridge;

    public WorldGuardHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            bridge = new WorldGuardBridge();

            plugin.getLogger().info("\u001B[36mHooked to WorldGuard!\u001B[0m");
        }
    }

    public boolean isPvpDenied(Location location) {
        if (bridge == null) return false;

        return bridge.isPvpDenied(location);
    }
}
