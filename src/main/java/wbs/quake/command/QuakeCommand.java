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
        addSubcommand(new StatsSubcommand(plugin), perm + ".stats");
        addSubcommand(new TopSubcommand(plugin), perm + ".top");

        String adminPerm = perm + ".admin";
        addSubcommand(new ForceStartSubcommand(plugin), adminPerm + ".forcestart");
        addSubcommand(new MoneyCommand(plugin), adminPerm + ".money");
        addSubcommand(new SetLobbySubcommand(plugin), adminPerm + ".setlobby");
        addSubcommand(new GunSubcommand(plugin), adminPerm + ".gun");
        addSubcommand(new ReloadSubcommand(plugin), adminPerm + ".reload");
        addSubcommand(new ErrorsSubcommand(plugin), adminPerm + ".reload");
    }
}
