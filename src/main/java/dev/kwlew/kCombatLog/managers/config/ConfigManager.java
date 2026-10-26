package dev.kwlew.kCombatLog.managers.config;

import dev.kwlew.kCombatLog.kernel.LifecycleComponent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager implements LifecycleComponent {
    private final ConfigFile config;
    private final ConfigFile messages;

    public ConfigManager(JavaPlugin plugin) {
        this.config = new ConfigFile(plugin, "config.yml");
        this.messages = new ConfigFile(plugin, "messages.yml");
        migrate();
    }

    @Override
    public void reload() {
        reloadAll();
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
        migrate();
    }

    public void saveAll() {
        config.save();
        messages.save();
    }

    public boolean getUpdateNoticeEnabled() {
        return config.get().getBoolean("update-notice", true);
    }

    public boolean getMessageChatEnabled() {
        return config.get().getBoolean("messages.combat-log", true);
    }

    public boolean getActionBarEnabled() {
        return config.get().getBoolean("messages.action-bar", true);
    }

    public boolean getBossBarEnabled() {
        return config.get().getBoolean("messages.boss-bar", false);
    }

    public boolean getCombatLogEnabled() {
        return config.get().getBoolean("combat-log.enable", true);
    }

    public int getCombatDuration() {
        return config.get().getInt("combat-log.delay", 10);
    }

    public List<String> getDisabledWorlds() {
        return config.get().getStringList("combat-log.disabled-worlds");
    }

    public boolean getCommandBlockingEnabled() {
        return config.get().getBoolean("combat-log.command-blocking.enabled", true);
    }

    public boolean getCommandBlockingWhitelist() {
        return config.get().getString("combat-log.command-blocking.mode", "whitelist").equalsIgnoreCase("whitelist");
    }

    public List<String> getCommandBlockingList() {
        List<String> commands = new ArrayList<>();
        for (String command : config.get().getStringList("combat-log.command-blocking.list")) {
            commands.add(command.toLowerCase());
        }
        return commands;
    }

    public boolean getEnderPearlsBlockedInCombat() {
        return config.get().getBoolean("combat-log.restrictions.block-ender-pearls", false);
    }

    public boolean getRiptideBlockedInCombat() {
        return config.get().getBoolean("combat-log.restrictions.block-riptide", false);
    }

    public int getEnderPearlCooldown() {
        return config.get().getInt("combat-log.restrictions.ender-pearl-cooldown", 0);
    }

    public boolean getElytraBlockedInCombat() {
        return config.get().getBoolean("combat-log.restrictions.block-elytra", false);
    }

    public boolean getFlightBlockedInCombat() {
        return config.get().getBoolean("combat-log.restrictions.block-flight", false);
    }

    public boolean getMobsCombatEnabled() {
        return config.get().getBoolean("combat-log.enable-entities", false);
    }

    public boolean getPunishmentKill() {
        return config.get().getBoolean("punishment.kill", true);
    }

    public boolean getPunishmentDropInventory() {
        return config.get().getBoolean("punishment.drop-inventory", false);
    }

    public List<String> getPunishmentCommands() {
        return config.get().getStringList("punishment.commands");
    }

    public boolean getPunishmentExemptKicks() {
        return config.get().getBoolean("punishment.exempt-kicks", true);
    }

    public String getTagSound() {
        return config.get().getString("effects.tag-sound", "minecraft:entity.experience_orb.pickup");
    }

    public String getUntagSound() {
        return config.get().getString("effects.untag-sound", "minecraft:block.note_block.pling");
    }

    public boolean getTitlesEnabled() {
        return config.get().getBoolean("effects.titles", false);
    }

    private void migrate() {
        if (!config.get().isSet("combat-log.commands")) return;

        config.get().set("combat-log.command-blocking.enabled", !config.get().getBoolean("combat-log.commands"));
        config.get().set("combat-log.commands", null);
        config.save();
    }
}
