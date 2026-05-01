package dev.kwlew.kCombatLog.managers.config;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final ConfigFile config;
    private final ConfigFile messages;

    public ConfigManager(JavaPlugin plugin) {
        this.config = new ConfigFile(plugin, "config.yml");
        this.messages = new ConfigFile(plugin, "messages.yml");
    }

    public ConfigFile config() {
        return config;
    }

    public ConfigFile messages() {
        return messages;
    }

    public void reloadAll() {
        config.reload();
        messages.reload();
    }

    public void saveAll() {
        config.save();
        messages.save();
    }

    public boolean getMessageChatEnabled() {
        return config.get().getBoolean("messages.combat-log", true);
    }

    public boolean getActionBarEnabled() {
        return config.get().getBoolean("messages.action-bar", true);
    }

    public boolean getCombatLogEnabled() {
        return config.get().getBoolean("combat-log.enable", true);
    }

    public int getCombatDuration() {
        return config.get().getInt("combat-log.delay", 15);
    }

    public boolean getCommandsInCombatEnabled() {
        return config.get().getBoolean("combat-log.commands", false);
    }

    public boolean getMobsCombatEnabled() {
        return config.get().getBoolean("combat-log.enable-entities", false);
    }
}
