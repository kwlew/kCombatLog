package dev.kwlew.kCombatLog.listeners.players;

import dev.kwlew.kCombatLog.kernel.Inject;
import dev.kwlew.kCombatLog.listeners.ListenerComponent;
import dev.kwlew.kCombatLog.managers.BuildInfo;
import dev.kwlew.kCombatLog.managers.MessageManager;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import dev.kwlew.kCombatLog.managers.permission.PermissionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinListener implements ListenerComponent {

    private final JavaPlugin plugin;
    private final PermissionManager perms;
    private final CombatManager combat;
    private final MessageManager messages;

    @Inject
    public JoinListener(JavaPlugin plugin, PermissionManager perms,  CombatManager combat,  MessageManager messages) {
        this.plugin = plugin;
        this.perms = perms;
        this.combat = combat;
        this.messages = messages;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();

        if (combat.isCombatLogged(player)) {
            messages.send(player, "combat.you-logged");
            combat.clearCombatLog(player);
        }

        if (perms.admin(player)) {
            Component message = serializer.deserialize(
                    "&aYou are using &bv" + BuildInfo.VERSION + " &aof kCombatLog!\n&aCheck for updates on my "
            ).append(
                    Component.text("GitHub")
                            .color(NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.openUrl(BuildInfo.GITHUB_URL))
            ).append(Component.text("\n You can disable this message on the config.yml")
                    .color(NamedTextColor.RED)
            );

            player.sendMessage(message);
        }
    }
}
