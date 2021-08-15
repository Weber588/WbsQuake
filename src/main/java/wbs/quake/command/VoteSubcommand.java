package wbs.quake.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.Arena;
import wbs.quake.player.PlayerManager;
import wbs.quake.QuakeLobby;
import wbs.quake.player.QuakePlayer;
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

        if (args.length < 2) {
            sendMessage("Choose an arena. Usage: &h/" + label + " vote <id>", sender);
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

        QuakeLobby lobby = QuakeLobby.getInstance();

        if (!lobby.getPlayers().contains(quakePlayer)) {
            sendMessage("You must be in the lobby to vote!", sender);
            return true;
        }

        if (lobby.getState() != QuakeLobby.GameState.VOTING) {
            sendMessage("Not in voting! Wait for voting to begin.", sender);
            return true;
        }

        Arena chosen = lobby.playerVote(quakePlayer, id);

        if (chosen != null) {
            lobby.messagePlayers("&h" + player.getName() + "&r voted for &h" + chosen.getName());
        } else {
            sendMessage("&wInvalid ID!", sender);
        }

        return true;
    }
}
