package wbs.quake.command.arena;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.utils.util.plugin.WbsPlugin;

public class ArenaTpSubcommand extends AbstractArenaCommand {
    public ArenaTpSubcommand(WbsPlugin plugin) {
        super(plugin, "tp");
    }

    @Override
    public boolean onArenaCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Arena arena) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        arena.respawn((Player) sender);
        sendMessage("Teleporting...", sender);

        return true;
    }
}
