package wbs.quake.command.arena.set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

public class ArenaSetMinSubcommand extends WbsSubcommand {
    public ArenaSetMinSubcommand(WbsPlugin plugin) {
        super(plugin, "min");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {

        if (args.length <= start) {
            sendUsage("<min players> <arena>", sender, label, args);
            return true;
        }

        int minPlayers;
        try {
            minPlayers = Integer.parseInt(args[start]);
        } catch (NumberFormatException e) {
            sendMessage("Invalid integer: " + args[start], sender);
            return true;
        }

        if (minPlayers < 2) {
            sendMessage("Min players cannot be less than 2.", sender);
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

        if (minPlayers > arena.getMaxPlayers()) {
            sendMessage("Min must be less than or equal to max. Max: " + arena.getMaxPlayers(), sender);
            return true;
        }

        arena.setMinPlayers(minPlayers);
        sendMessage("Set min players to " + minPlayers + " for arena &h" + arena.getDisplayName(), sender);

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
