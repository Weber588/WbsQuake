package wbs.quake.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.PlayerManager;
import wbs.quake.QuakeLobby;
import wbs.quake.QuakePlayer;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class VoteSubcommand extends WbsSubcommand {
    public VoteSubcommand(WbsPlugin plugin) {
        super(plugin, "vote");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sendMessage("&wInvalid ID: " + args[1], sender);
            return true;
        }

        Player player = (Player) sender;
        QuakePlayer quakePlayer = PlayerManager.getPlayer(player);

        Arena chosen = QuakeLobby.getInstance().playerVote(quakePlayer, id);

        if (chosen != null) {
            QuakeLobby.getInstance().messagePlayers("&h" + player.getName() + "&r voted for &h" + chosen.getName());
        } else {
            sendMessage("&wInvalid ID!", sender);
        }

        return true;
    }
}
