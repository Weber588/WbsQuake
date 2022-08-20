package wbs.quake.command.arena.set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

public class ArenaSetMaxSubcommand extends WbsSubcommand {
    public ArenaSetMaxSubcommand(WbsPlugin plugin) {
        super(plugin, "max");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {

        if (args.length <= start) {
            sendUsage("<max players> <arena>", sender, label, args);
            return true;
        }

        int maxPlayers;
        try {
            maxPlayers = Integer.parseInt(args[start]);
        } catch (NumberFormatException e) {
            sendMessage("Invalid integer: &w" + args[start], sender);
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

        if (maxPlayers < arena.getMinPlayers()) {
            sendMessage("Max must be greater than or equal to min. Min: " + arena.getMinPlayers(), sender);
            return true;
        }

        arena.setMaxPlayers(maxPlayers);
        arena.markForSaving();
        sendMessage("Set max players to " + maxPlayers + " for arena &h" + arena.getDisplayName(), sender);

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
