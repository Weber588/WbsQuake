package wbs.quake.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.QuakeDB;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
import wbs.quake.WbsQuake;
import wbs.utils.util.commands.WbsSubcommand;

public class StatsSubcommand extends WbsSubcommand {
    public StatsSubcommand(WbsQuake plugin) {
        super(plugin, "stats");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        Player player = (Player) sender;
        PlayerManager.getPlayerAsync(player, this::showStats);
        return true;
    }

    private void showStats(QuakePlayer player) {
        sendMessage("&m                             ", player.getPlayer()); // line break

        for (String line : player.getStatsDisplay()) {
            sendMessage(line, player.getPlayer());
        }

        sendMessage("&m                             ", player.getPlayer());
    }
}
