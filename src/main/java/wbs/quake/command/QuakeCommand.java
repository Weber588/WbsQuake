package wbs.quake.command;

import org.bukkit.command.PluginCommand;

import wbs.quake.WbsQuake;
import wbs.quake.command.arena.ArenaSubcommand;
import wbs.utils.util.commands.WbsCommand;

public class QuakeCommand extends WbsCommand {
    public QuakeCommand(WbsQuake plugin, PluginCommand command) {
        super(plugin, command);

        String perm = command.getPermission();

        addSubcommand(new ArenaSubcommand(plugin), perm + ".arena");
        addSubcommand(new JoinSubcommand(plugin), perm + ".join");
        addSubcommand(new VoteSubcommand(plugin), perm + ".join");
        addSubcommand(new LeaveSubcommand(plugin), perm + ".join");
        addSubcommand(new ShopSubcommand(plugin), perm + ".shop");
        addSubcommand(new SetLobbySubcommand(plugin), perm + ".setlobby");
        addSubcommand(new ForceStartSubcommand(plugin), perm + ".forcestart");
    }
}
