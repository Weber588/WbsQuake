package wbs.quake.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wbs.quake.QuakeLobby;

import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class SetLobbySubcommand extends WbsSubcommand {
    public SetLobbySubcommand(WbsPlugin plugin) {
        super(plugin, "setlobby");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        Player player = (Player) sender;
        QuakeLobby.getInstance().setLobbySpawn(player.getLocation());
        sendMessage("Lobby set to your location!", sender);

        return true;
    }
}
