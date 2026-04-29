package dev.kwlew.kCombatLog.listeners.players;

import dev.kwlew.kCombatLog.kernel.Inject;
import dev.kwlew.kCombatLog.listeners.ListenerComponent;
import dev.kwlew.kCombatLog.managers.MessageManager;
import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import dev.kwlew.kCombatLog.managers.permission.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerHitListener implements ListenerComponent {

    private final JavaPlugin plugin;
    private final PermissionManager perms;
    private final CombatManager combat;
    private final ConfigManager config;
    private final MessageManager messages;

    @Inject
    public PlayerHitListener(JavaPlugin plugin, PermissionManager perms, ConfigManager config, CombatManager combat, MessageManager messages) {
        this.plugin = plugin;
        this.perms = perms;
        this.combat = combat;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        combat.setExpirationCallback(uuid ->
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        messages.send(player, "combat.expired");
                    }
                })
        );
        
        if (config.getActionBarEnabled()) {
            plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateActionBars, 0L, 20L);
        }
    }
    
    private void updateActionBars() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (combat.isTagged(player)) {
                long remaining = Math.max(0, (combat.getRemaining(player) + 999) / 1000);
                messages.sendActionBar(player, "combat.action-bar",
                        messages.placeholder("remaining", String.valueOf(remaining))
                );
            }
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!config.getCombatLogEnabled()) return;

        Entity victim = event.getEntity();
        Entity damager = event.getDamager();

        if (!(victim instanceof Player damagedPlayer)) return;

        Player attacker = null;

        if (damager instanceof Player) {
            attacker = (Player) damager;
        }

        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        }

        if (attacker == null) return;
        if (attacker == damagedPlayer) return;

        if (perms.combat(damagedPlayer) && !perms.admin(damagedPlayer)) {
            boolean wasTagged = combat.isTagged(damagedPlayer);
            combat.tag(damagedPlayer);
            if (!wasTagged) {
                messages.send(damagedPlayer, "combat.tagged");
            }
        }

        if (perms.combat(attacker) && !perms.admin(attacker)) {
            boolean wasTagged = combat.isTagged(attacker);
            combat.tag(attacker);
            if (!wasTagged) {
                messages.send(attacker, "combat.tagged");
            }
        }

    }
}
