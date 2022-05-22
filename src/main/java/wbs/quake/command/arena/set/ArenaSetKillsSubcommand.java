package wbs.quake.command.arena.set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

public class ArenaSetKillsSubcommand extends WbsSubcommand {
    public ArenaSetKillsSubcommand(WbsPlugin plugin) {
        super(plugin, "killsToWin");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {

        if (args.length <= start) {
            sendUsage("<killsToWin> <arena>", sender, label, args);
            return true;
        }

        int killsToWin;
        try {
            killsToWin = Integer.parseInt(args[start]);
        } catch (NumberFormatException e) {
            sendMessage("Invalid integer: " + args[start], sender);
            return true;
        }

        if (killsToWin < 1) {
            sendMessage("Kills to win cannot be less than 1.", sender);
            return true;
        }

        Arena arena;
        if (args.length > start + 1) {
            arena = ArenaManager.getArena(args[start + 1]);
            if (arena == null) {
                sendMessage("Invalid arena: &w" + args[start + 1], sender);
                return true;
            }
        } else {
            sendUsage("<arena>", sender, label, args);
            return true;
        }

        arena.setKillsToWin(killsToWin);
        sendMessage("Set kills needed to win to " + killsToWin + " for arena &h" + arena.getDisplayName(), sender);

        return true;
    }

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length == start + 1) {
            return ArenaManager.getArenaNames();
        }

        return new LinkedList<>();
    }
}
