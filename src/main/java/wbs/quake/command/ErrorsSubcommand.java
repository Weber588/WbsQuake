package wbs.quake.command;

import wbs.quake.WbsQuake;
import wbs.utils.util.commands.WbsErrorsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.plugin.WbsSettings;

public class ErrorsSubcommand extends WbsErrorsSubcommand {
    public ErrorsSubcommand(WbsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected WbsSettings getSettings() {
        return WbsQuake.getInstance().settings;
    }
}
