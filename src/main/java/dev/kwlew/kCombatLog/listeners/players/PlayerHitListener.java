package dev.kwlew.kCombatLog.listeners.players;

import dev.kwlew.kCombatLog.kernel.Inject;
import dev.kwlew.kCombatLog.listeners.ListenerComponent;
import dev.kwlew.kCombatLog.managers.MessageManager;
import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import dev.kwlew.kCombatLog.managers.display.BossBarManager;
import dev.kwlew.kCombatLog.managers.hooks.WorldGuardHook;
import dev.kwlew.kCombatLog.managers.permission.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerHitListener implements ListenerComponent {

    private final JavaPlugin plugin;
    private final PermissionManager perms;
    private final CombatManager combat;
    private final ConfigManager config;
    private final MessageManager messages;
    private final BossBarManager bossBars;
    private final WorldGuardHook worldGuard;

    @Inject
    public PlayerHitListener(JavaPlugin plugin, PermissionManager perms, ConfigManager config, CombatManager combat, MessageManager messages, BossBarManager bossBars, WorldGuardHook worldGuard) {
        this.plugin = plugin;
        this.perms = perms;
        this.combat = combat;
        this.config = config;
        this.messages = messages;
        this.bossBars = bossBars;
        this.worldGuard = worldGuard;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        combat.setExpirationCallback(uuid ->
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        bossBars.hide(player);
                        combat.sendExpired(player);
                    }
                })
        );

        plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateDisplays, 0L, 20L);
    }

    private void updateDisplays() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!combat.isTagged(player)) {
                bossBars.hide(player);
                continue;
            }

            long remaining = combat.getRemaining(player);

            if (config.getActionBarEnabled()) {
                messages.sendActionBar(player, "combat.action-bar",
                        messages.placeholder("remaining", String.valueOf(Math.max(0, (remaining + 999) / 1000)))
                );
            }

            bossBars.update(player, remaining);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
        if (damagedPlayer.hasMetadata("NPC") || attacker.hasMetadata("NPC")) return;
        if (config.getDisabledWorlds().contains(damagedPlayer.getWorld().getName())) return;
        if (worldGuard.isPvpDenied(damagedPlayer.getLocation()) || worldGuard.isPvpDenied(attacker.getLocation())) return;

        if (perms.combat(damagedPlayer) && !perms.admin(damagedPlayer)) {
            boolean wasTagged = combat.isTagged(damagedPlayer);
            combat.tag(damagedPlayer);
            combat.setOpponent(damagedPlayer, attacker);
            if (!wasTagged) {
                combat.sendTagged(damagedPlayer);
            }
        }

        if (perms.combat(attacker) && !perms.admin(attacker)) {
            boolean wasTagged = combat.isTagged(attacker);
            combat.tag(attacker);
            combat.setOpponent(attacker, damagedPlayer);
            if (!wasTagged) {
                combat.sendTagged(attacker);
            }
        }

    }
}
