package wbs.quake.command.arena.set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

public class ArenaSetDurationSubcommand extends WbsSubcommand {
    public ArenaSetDurationSubcommand(WbsPlugin plugin) {
        super(plugin, "duration");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {

        if (args.length <= start) {
            sendUsage("<seconds in round> <arena>", sender, label, args);
            return true;
        }

        int secondsInRound;
        try {
            secondsInRound = Integer.parseInt(args[start]);
        } catch (NumberFormatException e) {
            sendMessage("Invalid integer: " + args[start], sender);
            return true;
        }

        if (secondsInRound < 30) {
            sendMessage("Duration must be greater than 30.", sender);
            return true;
        }

        if (secondsInRound >= 3600) {
            sendMessage("Duration cannot be more than 59 minutes 59 seconds.", sender);
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

        arena.setSecondsInRound(secondsInRound);
        sendMessage("Set max round duration to " + secondsInRound + " for arena &h" + arena.getName(), sender);

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
