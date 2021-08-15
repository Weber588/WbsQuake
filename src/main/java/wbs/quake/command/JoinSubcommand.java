package wbs.quake.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.*;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
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
        QuakePlayer quakePlayer = PlayerManager.getPlayer(player);

        boolean success = QuakeLobby.getInstance().join(quakePlayer);

        if (!success) {
            sendMessage("You were already in the lobby!", player);
        }

        return true;
    }
}
