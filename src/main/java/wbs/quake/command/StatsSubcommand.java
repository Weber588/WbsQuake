package wbs.quake.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
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
        QuakePlayer quakePlayer = PlayerManager.getPlayer(player);

        sendMessage("&m                             ", sender); // line break

        for (String line : quakePlayer.getStatsDisplay()) {
            sendMessage(line, sender);
        }

        sendMessage("&m                             ", sender);

        return true;
    }
}
