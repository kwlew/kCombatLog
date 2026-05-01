package dev.kwlew.kCombatLog.listeners.entities;

import dev.kwlew.kCombatLog.kernel.Inject;
import dev.kwlew.kCombatLog.listeners.ListenerComponent;
import dev.kwlew.kCombatLog.managers.MessageManager;
import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import dev.kwlew.kCombatLog.managers.permission.PermissionManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MobHitPlayerListener implements ListenerComponent {

    private final CombatManager combat;
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final PermissionManager perms;
    private final MessageManager messages;

    @Inject
    public MobHitPlayerListener(CombatManager combat, JavaPlugin plugin, ConfigManager config, PermissionManager perms, MessageManager messages) {
        this.combat = combat;
        this.plugin = plugin;
        this.config = config;
        this.perms = perms;
        this.messages = messages;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMobHit(EntityDamageByEntityEvent event) {
        if (!config.getMobsCombatEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        Entity damager = event.getDamager();

        LivingEntity attacker = null;

        if (damager instanceof LivingEntity living && !(living instanceof Player)) {
            attacker = living;
        }

        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof LivingEntity living && !(living instanceof Player)) {
                attacker = living;
            }
        }

        if (attacker == null) return;

        if (perms.combat(player) && !perms.admin(player)) {
            boolean wasTagged = combat.isTagged(player);
            combat.tag(player);

            if (!wasTagged) {
                messages.send(player, "combat.tagged");
            }
        }
    }
}
