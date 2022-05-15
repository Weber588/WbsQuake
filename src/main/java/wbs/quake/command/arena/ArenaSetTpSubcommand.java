package wbs.quake.command.arena;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

public class ArenaSetTpSubcommand extends AbstractArenaCommand {
    public ArenaSetTpSubcommand(WbsPlugin plugin) {
        super(plugin, "settp");
    }

    // TODO: Allow user to set the point that /wbsquake arena tp <arena> takes you to

    @Override
    public boolean onArenaCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Arena arena) {
        sendMessage("Not implemented.", sender);
        return true;
    }
}
