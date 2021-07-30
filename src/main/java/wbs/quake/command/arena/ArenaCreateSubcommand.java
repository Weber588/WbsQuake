package wbs.quake.command.arena;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class ArenaCreateSubcommand extends WbsSubcommand {
    public ArenaCreateSubcommand(WbsPlugin plugin) {
        super(plugin, "create");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length > start) {
            String arenaName = args[start];
            if (ArenaManager.getArena(arenaName) != null) {
                sendMessage("&wThat arena name is already in use.", sender);
                return true;
            }

            Arena newArena = new Arena(arenaName);
            ArenaManager.registerArena(newArena);
            sendMessage("Arena &h" + arenaName + "&r created!", sender);
        } else {
            sendUsage("<name>", sender, label, args);
        }

        return true;
    }
}
