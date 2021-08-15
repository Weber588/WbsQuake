package wbs.quake.command.arena;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.quake.ArenaManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

public class ArenaSetTpSubcommand extends WbsSubcommand {
    public ArenaSetTpSubcommand(WbsPlugin plugin) {
        super(plugin, "settp");
    }

    // TODO: Allow user to set the point that /wbsquake arena tp <arena> takes you to

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length == start) {
            return ArenaManager.getArenaNames();
        }

        return new LinkedList<>();
    }
}
