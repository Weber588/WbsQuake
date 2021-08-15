package wbs.quake.command.arena;

import wbs.quake.WbsQuake;
import wbs.quake.command.arena.powerup.ArenaPowerUpSubcommand;
import wbs.quake.command.arena.set.ArenaSetOptionSubcommand;
import wbs.quake.command.arena.spawnpoint.ArenaSpawnpointSubcommand;
import wbs.utils.util.commands.WbsCommandNode;
import wbs.utils.util.plugin.WbsPlugin;

public class ArenaSubcommand extends WbsCommandNode {

    public ArenaSubcommand(WbsQuake plugin) {
        super(plugin, "arena");

        String perm = getPermission();

        addChild(new ArenaCreateSubcommand(plugin), perm + ".create");
        addChild(new ArenaSpawnpointSubcommand(plugin), perm + ".spawnpoint");
        addChild(new ArenaPowerUpSubcommand(plugin), perm + ".powerup");
        addChild(new ArenaTpSubcommand(plugin), perm + ".tp");
        addChild(new ArenaSetTpSubcommand(plugin), perm + ".settp");

        addChild(new ArenaInfoSubcommand(plugin), perm + ".info");

        addChild(new ArenaSetOptionSubcommand(plugin), perm + ".set");
    }
}
