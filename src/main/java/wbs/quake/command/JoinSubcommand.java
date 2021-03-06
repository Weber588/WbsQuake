package wbs.quake.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.QuakeDB;
import wbs.quake.QuakeLobby;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class JoinSubcommand extends WbsSubcommand {
    public JoinSubcommand(WbsPlugin plugin) {
        super(plugin, "join");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        Player player = (Player) sender;

        if (QuakeLobby.getInstance().isInLobby(player)) {
            sendMessage("You were already in the lobby!", player);
            return true;
        }

        QuakeDB.getPlayerManager().getAsync(player,
                (quakePlayer) -> QuakeLobby.getInstance().join(quakePlayer)
        );
        return true;
    }
}
