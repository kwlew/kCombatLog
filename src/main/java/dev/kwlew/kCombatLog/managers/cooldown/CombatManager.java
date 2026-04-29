package dev.kwlew.kCombatLog.managers.cooldown;

import dev.kwlew.kCombatLog.kernel.LifecycleComponent;
import dev.kwlew.kCombatLog.managers.config.ConfigManager;
import dev.kwlew.kCombatLog.managers.exceptions.ExpirationCallbackException;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CombatManager implements LifecycleComponent {

    private final Map<UUID, Long> combatMap = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> combatLogged = new ConcurrentHashMap<>();
    private final long combatDurationMillis;
    private final ConfigManager config;

    private ScheduledExecutorService executor;
    private Consumer<UUID> expirationCallback;
    private static final long CLEANUP_INTERVAL_SECONDS = 5;

    public CombatManager(ConfigManager config) {
        this.config = config;
        this.combatDurationMillis = config.getCombatDuration() * 1000L;
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
        combatMap.put(player.getUniqueId(), System.currentTimeMillis() + combatDurationMillis);
    }

    public boolean isTagged(Player player) {
        UUID uuid = player.getUniqueId();
        Long end = combatMap.get(uuid);
        if (end == null) return false;

        if (System.currentTimeMillis() > end) {
            combatMap.remove(uuid);
            notifyExpiration(uuid);
            return false;
        }

        return true;
    }

    public void CombatLog(Player player) {
        UUID playerUUID = player.getUniqueId();
        combatLogged.put(playerUUID, true);
    }

    public boolean isCombatLogged(Player player) {
        return combatLogged.getOrDefault(player.getUniqueId(), false);
    }

    public void clearCombatLog(Player player) {
        combatLogged.remove(player.getUniqueId());
    }

    public long getRemaining(Player player) {
        Long end = combatMap.get(player.getUniqueId());
        if (end == null) return 0;

        long remaining = end - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public void remove(Player player) {
        combatMap.remove(player.getUniqueId());
    }

    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        combatMap.entrySet().removeIf(entry -> {
            if (currentTime > entry.getValue()) {
                notifyExpiration(entry.getKey());
                return true;
            }
            return false;
        });
    }

    private void notifyExpiration(UUID uuid) {
        if (expirationCallback != null) {
            try {
                expirationCallback.accept(uuid);
            } catch (Exception e) {
                throw new ExpirationCallbackException(uuid);
            }
        }
    }
}
