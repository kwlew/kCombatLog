package dev.kwlew.kCombatLog.managers.hooks;

import dev.kwlew.kCombatLog.managers.BuildInfo;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class PlaceholderAPI extends PlaceholderExpansion {

    private final CombatManager combatManager;

    public PlaceholderAPI(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return BuildInfo.PLUGIN_NAME.toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull String getAuthor() {
        return BuildInfo.AUTHOR;
    }

    @Override
    public @NotNull String getVersion() {
        return BuildInfo.VERSION;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("in_combat")) {
            return String.valueOf(combatManager.isTagged(player));
        }

        if (params.equalsIgnoreCase("remaining_time") || params.equalsIgnoreCase("remaining")) {
            long remainingSeconds = Math.max(0, (combatManager.getRemaining(player) + 999) / 1000);
            return String.valueOf(remainingSeconds);
        }

        return null;
    }

}
