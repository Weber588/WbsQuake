package wbs.quake.command.arena.set;

import wbs.utils.util.commands.WbsCommandNode;
import wbs.utils.util.plugin.WbsPlugin;

public class ArenaSetOptionSubcommand extends WbsCommandNode {
    public ArenaSetOptionSubcommand(WbsPlugin plugin) {
        super(plugin, "set");

        addChild(new ArenaSetMinSubcommand(plugin));
        addChild(new ArenaSetMaxSubcommand(plugin));
        addChild(new ArenaSetDescriptionSubcommand(plugin));
        addChild(new ArenaSetKillsSubcommand(plugin));
        addChild(new ArenaSetDurationSubcommand(plugin));
        addChild(new ArenaSetDisplaySubcommand(plugin));
    }
}
