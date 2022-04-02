package wbs.quake.command.arena;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.ArenaManager;
import wbs.utils.util.WbsTime;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.WbsStringify;
import wbs.utils.util.string.WbsStrings;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

public class ArenaInfoSubcommand extends WbsSubcommand {
    public ArenaInfoSubcommand(WbsPlugin plugin) {
        super(plugin, "info");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length <= start) {
            sendUsage("<name>", sender, label, args);
            return true;
        }

        String arenaName = args[start];
        Arena arena = ArenaManager.getArena(arenaName);

        if (arena == null) {
            sendMessage("Invalid arena: &w" + args[start]
                    + "&r. Please choose from the following: &h"
                    + String.join(", ", ArenaManager.getArenaNames()), sender);
            return true;
        }

        sendMessage("&m                             ", sender); // line break

        sendMessage("Name: &h" + arena.getName(), sender);
        sendMessage("Kills to win: &h" + arena.getKillsToWin(), sender);
        int secondsInRound = arena.getSecondsInRound();
        String timeString = WbsStringify.toString(Duration.ofSeconds(secondsInRound), true);
        sendMessage("Duration: &h" + timeString + " (" + secondsInRound + " seconds)", sender);
        sendMessage("Min players: &h" + arena.getMinPlayers(), sender);
        sendMessage("Max players: &h" + arena.getMaxPlayers(), sender);
        sendMessage("Number of spawnpoints: &h" + arena.getSpawnpoints().size(), sender);
        sendMessage("Number of powerups: &h" + arena.getPowerUps().size(), sender);

        sendMessage("&m                             ", sender); // line break

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
