package wbs.quake.command;

import wbs.quake.WbsQuake;
import wbs.utils.util.commands.WbsReloadSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.plugin.WbsSettings;

public class ReloadSubcommand extends WbsReloadSubcommand {
    public ReloadSubcommand(WbsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected WbsSettings getSettings() {
        return WbsQuake.getInstance().settings;
    }
}
