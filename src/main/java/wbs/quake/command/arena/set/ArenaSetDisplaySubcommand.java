package wbs.quake.command.arena.set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;

public class ArenaSetDisplaySubcommand extends WbsSubcommand {
    public ArenaSetDisplaySubcommand(@NotNull WbsPlugin plugin) {
        super(plugin, "display");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length <= start) {
            sendUsage("<arena> <name>", sender, label, args);
            return true;
        }

        Arena arena;
        arena = ArenaManager.getArena(args[start]);
        if (arena == null) {
            sendMessage("Invalid arena: &w" + args[start], sender);
            return true;
        }

        String display;
        if (args.length > start + 1) {
            display = WbsStrings.combineLast(args, start + 1, " ");
        } else {
            sendUsage("<name>", sender, label, args);
            return true;
        }

        arena.setDisplayName(display);
        arena.markForSaving();
        sendMessage("Set the display name of &h" + arena.getName() + "&r to &h" + display + "&r.", sender);

        return true;
    }

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length == start) {
            return ArenaManager.getArenaNames();
        }

        return new LinkedList<>();
    }
}
