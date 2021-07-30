package wbs.quake.command.arena;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.quake.PlayerManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class ArenaTpSubcommand extends WbsSubcommand {
    public ArenaTpSubcommand(WbsPlugin plugin) {
        super(plugin, "tp");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length > start) {
            String arenaName = args[start];
            Arena arena = ArenaManager.getArena(arenaName);

            if (arena == null) {
                sendMessage("Invalid arena: &w" + args[start]
                        + "&r. Please choose from the following: &h"
                        + String.join(", ", ArenaManager.getArenaNames()), sender);
                return true;
            }
            arena.respawn(PlayerManager.getPlayer((Player) sender));
            sendMessage("Teleporting...", sender);
        } else {
            sendUsage("<name>", sender, label, args);
        }

        return true;
    }
}
