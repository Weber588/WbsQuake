package wbs.quake.command.arena.spawnpoint;

import wbs.utils.util.commands.WbsCommandNode;
import wbs.utils.util.plugin.WbsPlugin;

public class ArenaSpawnpointSubcommand extends WbsCommandNode {
    public ArenaSpawnpointSubcommand(WbsPlugin plugin) {
        super(plugin, "spawnpoint");

        String perm = getPermission();

        addChild(new ArenaSpawnpointAddSubcommand(plugin), perm + ".add");
        addChild(new ArenaSpawnpointRemoveSubcommand(plugin), perm + ".remove");
        addChild(new ArenaSpawnpointListSubcommand(plugin), perm + ".list");
        addChild(new ArenaSpawnpointTpSubcommand(plugin), perm + ".tp");
    }
}
