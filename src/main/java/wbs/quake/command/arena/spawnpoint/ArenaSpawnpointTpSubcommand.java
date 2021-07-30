package wbs.quake.command.arena.spawnpoint;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

public class ArenaSpawnpointTpSubcommand extends WbsSubcommand {
    public ArenaSpawnpointTpSubcommand(WbsPlugin plugin) {
        super(plugin, "tp");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        if (args.length <= start) {
            sendUsage("<arena>", sender, label, args);
            return true;
        }

        Arena arena = ArenaManager.getArena(args[start]);

        if (arena == null) {
            sendMessage("Invalid arena: &w" + args[start]
                    + "&r. Please choose from the following: &h"
                    + String.join(", ", ArenaManager.getArenaNames()), sender);
            return true;
        }

        if (args.length <= start + 1) {
            sendMessage("Use &h/" + label + " arena spawnpoint list " + arena.getName() + "&r to auto-use this command.", sender);
            return true;
        }

        int hash;
        try {
            hash = Integer.parseInt(args[start + 1]);
        } catch (NumberFormatException e) {
            sendMessage("Use &h/" + label + " arena spawnpoint list " + arena.getName() + "&r to auto-use this command.", sender);
            return true;
        }

        Location location = arena.getSpawnPoint(hash);

        if (location != null) {
            sendMessage("Teleported...", sender);
            ((Player) sender).teleport(location);
        } else {
            sendMessage("Use &h/" + label + " arena spawnpoint list " + arena.getName() + "&r to auto-use this command.", sender);
        }

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
