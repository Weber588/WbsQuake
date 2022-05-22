package wbs.quake.command.arena;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

public class ArenaInfoSubcommand extends AbstractArenaCommand {
    public ArenaInfoSubcommand(WbsPlugin plugin) {
        super(plugin, "info");
    }

    @Override
    public boolean onArenaCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Arena arena) {
        sendMessage("&m                             ", sender); // line break

        sendMessage("Name: &h" + arena.getName(), sender);
        sendMessage("Display name: &h" + arena.getDisplayName(), sender);
        sendMessage("Kills to win: &h" + arena.getKillsToWin(), sender);
        int secondsInRound = arena.getSecondsInRound();
        String timeString = WbsStringify.toString(Duration.ofSeconds(secondsInRound), true);
        sendMessage("Duration: &h" + timeString + " (" + secondsInRound + " seconds)", sender);
        sendMessage("Min players: &h" + arena.getMinPlayers(), sender);
        sendMessage("Max players: &h" + arena.getMaxPlayers(), sender);
        String spawnpointListCommand = getAlternateCommand(label, args, "spawnpoint list", start, arena);
        plugin.buildMessage("Number of spawnpoints: &h" + arena.getSpawnpoints().size())
                .append(" &6&l[LIST]")
                    .setFormatting("&h")
                    .addClickCommand(spawnpointListCommand)
                    .addHoverText("Click to show list!")
                .send(sender);
        String powerUpListCommand = getAlternateCommand(label, args, "powerup list", start, arena);
        plugin.buildMessage("Number of powerups: &h" + arena.getPowerUps().size())
                .append(" &6&l[LIST]")
                    .setFormatting("&h")
                    .addClickCommand(powerUpListCommand)
                    .addHoverText("Click to show list!")
                .send(sender);

        if (sender instanceof Player) {
            String tpCommand = getAlternateCommand(label, args, "tp", start, arena);

            plugin.buildMessage("&6&l[TP]")
                        .setFormatting("&h")
                    .addHoverText("Click to tp!")
                    .addClickCommand(tpCommand)
                    .build()
                    .send(sender);
        }

        sendMessage("&m                             ", sender); // line break

        return true;
    }

    private String getAlternateCommand(String label, String[] args, String altArgs, int start, Arena arena) {
        return "/" + label + " "
                + WbsStrings.combineFirst(args, start - 1, " ") + " "
                + altArgs + " " + arena.getName();
    }
}
