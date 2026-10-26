package dev.kwlew.kCombatLog.managers.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Location;

public class WorldGuardBridge {

    public boolean isPvpDenied(Location location) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                .queryState(BukkitAdapter.adapt(location), null, Flags.PVP) == StateFlag.State.DENY;
    }
}
