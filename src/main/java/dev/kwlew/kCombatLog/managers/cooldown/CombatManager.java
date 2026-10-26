package dev.kwlew.kCombatLog.managers.cooldown;

import dev.kwlew.kCombatLog.kernel.Inject;
import dev.kwlew.kCombatLog.kernel.LifecycleComponent;
import dev.kwlew.kCombatLog.managers.MessageManager;
import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import dev.kwlew.kCombatLog.managers.permission.PermissionManager;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CombatManager implements LifecycleComponent {

    private final Map<UUID, Long> combatMap = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> opponents = new ConcurrentHashMap<>();
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final MessageManager messages;
    private final PermissionManager perms;
    private final NamespacedKey combatLoggedKey;

    private ScheduledExecutorService executor;
    private Consumer<UUID> expirationCallback;
    private static final long CLEANUP_INTERVAL_SECONDS = 5;

    @Inject
    public CombatManager(JavaPlugin plugin, ConfigManager config, MessageManager messages, PermissionManager perms) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.perms = perms;
        this.combatLoggedKey = new NamespacedKey(plugin, "combat_logged");
    }

    @Override
    public void init() {
        executor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "CombatManager-Cleanup");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void start() {
        executor.scheduleAtFixedRate(
                this::cleanupExpiredEntries,
                CLEANUP_INTERVAL_SECONDS,
                CLEANUP_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setExpirationCallback(Consumer<UUID> callback) {
        this.expirationCallback = callback;
    }

    public void tag(Player player) {
        combatMap.put(player.getUniqueId(), System.currentTimeMillis() + config.getCombatDuration() * 1000L);
    }

    public void sendTagged(Player player) {
        messages.send(player, "combat.tagged");
        messages.playSound(player, config.getTagSound());

        if (config.getTitlesEnabled()) {
            messages.sendTitle(player, "combat.title-tagged", "combat.subtitle-tagged");
        }

        stopMovementAbilities(player);
    }

    private void stopMovementAbilities(Player player) {
        if (perms.admin(player)) return;

        if (config.getElytraBlockedInCombat() && player.isGliding()) {
            player.setGliding(false);
        }

        if (!config.getFlightBlockedInCombat() || !player.isFlying()) return;

        GameMode mode = player.getGameMode();
        if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
            player.setFlying(false);
        }
    }

    public void setOpponent(Player player, Entity opponent) {
        opponents.put(player.getUniqueId(), opponent.getUniqueId());
    }

    public Entity getOpponent(Player player) {
        UUID uuid = opponents.get(player.getUniqueId());
        if (uuid == null) return null;

        return plugin.getServer().getEntity(uuid);
    }

    public void sendExpired(Player player) {
        messages.send(player, "combat.expired");
        messages.playSound(player, config.getUntagSound());
    }

    public boolean isTagged(Player player) {
        UUID uuid = player.getUniqueId();
        Long end = combatMap.get(uuid);
        if (end == null) return false;

        if (System.currentTimeMillis() > end) {
            if (combatMap.remove(uuid, end)) {
                opponents.remove(uuid);
                notifyExpiration(uuid);
            }
            return false;
        }

        return true;
    }

    public void CombatLog(Player player) {
        player.getPersistentDataContainer().set(combatLoggedKey, PersistentDataType.BYTE, (byte) 1);
    }

    public boolean isCombatLogged(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        return data.has(combatLoggedKey, PersistentDataType.BYTE);
    }

    public void clearCombatLog(Player player) {
        player.getPersistentDataContainer().remove(combatLoggedKey);
    }

    public long getRemaining(Player player) {
        Long end = combatMap.get(player.getUniqueId());
        if (end == null) return 0;

        long remaining = end - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public void remove(Player player) {
        combatMap.remove(player.getUniqueId());
        opponents.remove(player.getUniqueId());
    }

    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<UUID, Long> entry : combatMap.entrySet()) {
            if (currentTime <= entry.getValue()) continue;

            if (combatMap.remove(entry.getKey(), entry.getValue())) {
                opponents.remove(entry.getKey());
                notifyExpiration(entry.getKey());
            }
        }
    }

    private void notifyExpiration(UUID uuid) {
        if (expirationCallback != null) {
            try {
                expirationCallback.accept(uuid);
            } catch (Exception e) {
                plugin.getLogger().warning("Expiration callback failed for " + uuid + ": " + e);
            }
        }
    }
}
