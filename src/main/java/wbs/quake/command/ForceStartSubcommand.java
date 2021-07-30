package wbs.quake.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.*;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class ForceStartSubcommand extends WbsSubcommand {
    public ForceStartSubcommand(WbsPlugin plugin) {
        super(plugin, "forcestart");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("This command is only usable by players", sender);
            return true;
        }

        QuakeLobby.getInstance().forceStart();

        return true;
    }
}
