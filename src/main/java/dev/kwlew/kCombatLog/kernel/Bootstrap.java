package dev.kwlew.kCombatLog.kernel;

import dev.kwlew.kCombatLog.listeners.entities.MobHitPlayerListener;
import dev.kwlew.kCombatLog.listeners.players.*;
import dev.kwlew.kCombatLog.managers.MessageManager;
import dev.kwlew.kCombatLog.managers.bStats;
import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import dev.kwlew.kCombatLog.managers.hooks.PlaceholderAPIHook;
import dev.kwlew.kCombatLog.managers.permission.PermissionManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Bootstrap {

    private final Registry registry = new Registry();

    public Bootstrap(JavaPlugin plugin) {
        registry.register(JavaPlugin.class, plugin);
        registry.register(Registry.class, registry);

        initManagers();

        initHooks();

        initListeners();
    }

    public void init() {
        lifecycle(LifecycleComponent::init);
        lifecycle(LifecycleComponent::start);
    }

    public void shutdown() {
        lifecycle(LifecycleComponent::shutdown);
    }

    private void initManagers() {
        registry.resolve(MessageManager.class);
        registry.resolve(ConfigManager.class);

        registry.resolve(CombatManager.class);
        registry.resolve(PermissionManager.class);
    }

    private void initHooks() {
        registry.resolve(PlaceholderAPIHook.class);
        registry.resolve(bStats.class);
    }

    private void initListeners() {
        registry.resolve(JoinListener.class);
        registry.resolve(PlayerHitListener.class);
        registry.resolve(CommandListener.class);
        registry.resolve(QuitListener.class);
        registry.resolve(DeathListener.class);

        registry.resolve(MobHitPlayerListener.class);
    }

    private void lifecycle(Consumer<LifecycleComponent> action) {
        for (Object obj : new ArrayList<>(registry.getAll())) {
            if (obj instanceof LifecycleComponent component) {
                action.accept(component);
            }
        }
    }
}
