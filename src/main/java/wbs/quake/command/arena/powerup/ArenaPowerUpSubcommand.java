package wbs.quake.command.arena.powerup;

import wbs.quake.WbsQuake;
import wbs.utils.util.commands.WbsCommandNode;

public class ArenaPowerUpSubcommand extends WbsCommandNode {
    public ArenaPowerUpSubcommand(WbsQuake plugin) {
        super(plugin, "powerup");

        String perm = getPermission();

        addChild(new ArenaPowerUpAddSubcommand(plugin), perm + ".add");
        addChild(new ArenaPowerUpRemoveSubcommand(plugin), perm + ".remove");
        addChild(new ArenaPowerUpListSubcommand(plugin), perm + ".list");
        addChild(new ArenaPowerUpTpSubcommand(plugin), perm + ".tp");
    }
}
