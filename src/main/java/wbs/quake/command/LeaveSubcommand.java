package wbs.quake.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.player.PlayerManager;
import wbs.quake.QuakeLobby;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class LeaveSubcommand extends WbsSubcommand {
    public LeaveSubcommand(WbsPlugin plugin) {
        super(plugin, "leave");
    }


    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        Player player = (Player) sender;
        QuakePlayer quakePlayer = QuakeLobby.getInstance().getPlayer(player);
        if (quakePlayer != null) {
            QuakeLobby.getInstance().leave(quakePlayer);
        } else {
            sendMessage("You were not in the lobby!", player);
        }

        return true;
    }
}
