package dev.kwlew.kCombatLog.managers.hooks;

import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import dev.kwlew.kCombatLog.kernel.LifecycleComponent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlaceholderAPIHook implements LifecycleComponent {

    private final JavaPlugin plugin;
    private final CombatManager combatManager;

    public PlaceholderAPIHook(JavaPlugin plugin, CombatManager combatManager) {
        this.plugin = plugin;
        this.combatManager = combatManager;
    }

    @Override
    public void start() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPI(combatManager).register();

            plugin.getLogger().info("\u001B[36mHooked to PlaceholderAPI!\u001B[0m");
        }
    }
}
