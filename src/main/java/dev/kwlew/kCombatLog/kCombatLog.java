package dev.kwlew.kCombatLog;

import org.bukkit.plugin.java.JavaPlugin;
import dev.kwlew.kCombatLog.kernel.Bootstrap;

public final class kCombatLog extends JavaPlugin {

    private long start;
    private Bootstrap bootstrap;

    // yay
    @Override
    public void onEnable() {
        start = System.currentTimeMillis();
        saveDefaultConfig();

        bootstrap = new Bootstrap(this);
        bootstrap.init();

        logStartupTime();
    }

    @Override
    public void onDisable() {
        getLogger().info("\u001B[36mDisabling kCombatLog...\u001B[0m");

        if (bootstrap != null) {
            bootstrap.shutdown();
        }
    }

    private void logStartupTime() {
        long time = System.currentTimeMillis() - start;

        getLogger().info("\u001B[36mkCombatLog enabled! \u001B[90m(Took \u001B[32m"
                + time + "ms\u001B[90m)\u001B[0m");
    }

}
