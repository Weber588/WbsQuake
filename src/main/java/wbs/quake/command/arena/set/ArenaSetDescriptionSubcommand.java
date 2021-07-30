package wbs.quake.command.arena.set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class ArenaSetDescriptionSubcommand extends WbsSubcommand {
    public ArenaSetDescriptionSubcommand(WbsPlugin plugin) {
        super(plugin, "description");
    }


    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        sendMessage("Not yet implemented.", sender);
        return true;
    }
}
