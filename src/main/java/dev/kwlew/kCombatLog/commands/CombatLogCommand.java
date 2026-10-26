package dev.kwlew.kCombatLog.commands;

import dev.kwlew.kCombatLog.kernel.Bootstrap;
import dev.kwlew.kCombatLog.kernel.Inject;
import dev.kwlew.kCombatLog.kernel.LifecycleComponent;
import dev.kwlew.kCombatLog.managers.MessageManager;
import dev.kwlew.kCombatLog.managers.cooldown.CombatManager;
import dev.kwlew.kCombatLog.managers.display.BossBarManager;
import dev.kwlew.kCombatLog.managers.permission.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CombatLogCommand implements CommandExecutor, TabCompleter, LifecycleComponent {

    private final JavaPlugin plugin;
    private final Bootstrap bootstrap;
    private final CombatManager combat;
    private final PermissionManager perms;
    private final MessageManager messages;
    private final BossBarManager bossBars;

    @Inject
    public CombatLogCommand(JavaPlugin plugin, Bootstrap bootstrap, CombatManager combat, PermissionManager perms, MessageManager messages, BossBarManager bossBars) {
        this.plugin = plugin;
        this.bootstrap = bootstrap;
        this.combat = combat;
        this.perms = perms;
        this.messages = messages;
        this.bossBars = bossBars;
    }

    @Override
    public void start() {
        PluginCommand command = plugin.getCommand("combatlog");
        if (command == null) {
            plugin.getLogger().warning("Command 'combatlog' is not declared in plugin.yml.");
            return;
        }

        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            messages.send(sender, "combat.usage");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> reload(sender);
            case "tag" -> tag(sender, args);
            case "untag" -> untag(sender, args);
            case "status" -> status(sender, args);
            default -> messages.send(sender, "combat.usage");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            if (perms.reload(sender)) completions.add("reload");
            if (perms.admin(sender)) {
                completions.add("tag");
                completions.add("untag");
            }
            completions.add("status");
            completions.removeIf(name -> !name.startsWith(prefix));
            return completions;
        }

        if (args.length != 2 || !perms.admin(sender)) return completions;

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (!sub.equals("tag") && !sub.equals("untag") && !sub.equals("status")) return completions;

        String prefix = args[1].toLowerCase(Locale.ROOT);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                completions.add(online.getName());
            }
        }

        return completions;
    }

    private void reload(CommandSender sender) {
        if (!perms.reload(sender)) {
            messages.send(sender, "combat.no-permission");
            return;
        }

        bootstrap.reload();
        messages.send(sender, "combat.reloaded");
    }

    private void tag(CommandSender sender, String[] args) {
        if (!perms.admin(sender)) {
            messages.send(sender, "combat.no-permission");
            return;
        }

        if (args.length < 2) {
            messages.send(sender, "combat.usage");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            messages.send(sender, "combat.player-not-found", messages.placeholder("player", args[1]));
            return;
        }

        boolean wasTagged = combat.isTagged(target);
        combat.tag(target);
        messages.send(sender, "combat.admin-tagged", messages.placeholder("player", target.getName()));
        if (!wasTagged) {
            combat.sendTagged(target);
        }
    }

    private void untag(CommandSender sender, String[] args) {
        if (!perms.admin(sender)) {
            messages.send(sender, "combat.no-permission");
            return;
        }

        if (args.length < 2) {
            messages.send(sender, "combat.usage");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            messages.send(sender, "combat.player-not-found", messages.placeholder("player", args[1]));
            return;
        }

        combat.remove(target);
        bossBars.hide(target);
        messages.send(sender, "combat.admin-untagged", messages.placeholder("player", target.getName()));
    }

    private void status(CommandSender sender, String[] args) {
        Player target;

        if (args.length < 2) {
            if (!(sender instanceof Player self)) {
                messages.send(sender, "combat.usage");
                return;
            }
            target = self;
        } else {
            if (!perms.admin(sender)) {
                messages.send(sender, "combat.no-permission");
                return;
            }

            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                messages.send(sender, "combat.player-not-found", messages.placeholder("player", args[1]));
                return;
            }
        }

        if (!combat.isTagged(target)) {
            messages.send(sender, "combat.status-clear", messages.placeholder("player", target.getName()));
            return;
        }

        long remaining = Math.max(0, (combat.getRemaining(target) + 999) / 1000);
        messages.send(sender, "combat.status-tagged",
                messages.placeholder("player", target.getName()),
                messages.placeholder("remaining", String.valueOf(remaining))
        );
    }
}
