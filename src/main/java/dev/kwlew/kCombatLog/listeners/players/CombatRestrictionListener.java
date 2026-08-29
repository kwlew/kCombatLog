package dev.kwlew.kCombatLog.listeners.players;

import dev.kwlew.kCombatLog.kernel.Inject;
import dev.kwlew.kCombatLog.listeners.ListenerComponent;
import dev.kwlew.kCombatLog.managers.MessageManager;
import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class CombatRestrictionListener implements ListenerComponent {

    private final JavaPlugin plugin;
    private final CombatManager combat;
    private final ConfigManager config;
    private final MessageManager messages;

    @Inject
    public CombatRestrictionListener(JavaPlugin plugin, CombatManager combat, ConfigManager config, MessageManager messages) {
        this.plugin = plugin;
        this.combat = combat;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnderPearlLaunch(ProjectileLaunchEvent event) {
        if (!config.getEnderPearlsBlockedInCombat()) return;
        if (!(event.getEntity() instanceof EnderPearl enderPearl)) return;
        if (!(enderPearl.getShooter() instanceof Player player)) return;
        if (!combat.isTagged(player)) return;

        event.setCancelled(true);
        messages.send(player, "combat.ender-pearl-blocked");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRiptideTridentUse(PlayerInteractEvent event) {
        if (!config.getRiptideBlockedInCombat()) return;
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getItem();
        if (item == null || !item.containsEnchantment(Enchantment.RIPTIDE)) return;
        if (!combat.isTagged(event.getPlayer())) return;

        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);
        resetRiptideState(event.getPlayer());
        messages.send(event.getPlayer(), "combat.riptide-blocked");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRiptide(PlayerRiptideEvent event) {
        if (!config.getRiptideBlockedInCombat()) return;
        if (!combat.isTagged(event.getPlayer())) return;

        event.setCancelled(true);
        event.getPlayer().setVelocity(event.getVelocity().zero());
        resetRiptideState(event.getPlayer());
        messages.send(event.getPlayer(), "combat.riptide-blocked");
    }

    private void resetRiptideState(Player player) {
        clearRiptideState(player);
        player.setCooldown(Material.TRIDENT, 5);
        player.updateInventory();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                clearRiptideState(player);
            }
        });
    }

    private void clearRiptideState(Player player) {
        player.clearActiveItem();
        player.setRiptiding(false);
    }
}
