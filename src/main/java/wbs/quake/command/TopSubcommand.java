package wbs.quake.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.quake.StatsManager;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TopSubcommand extends WbsSubcommand {
    public TopSubcommand(@NotNull WbsPlugin plugin) {
        super(plugin, "top");
    }

    // TODO: Make configurable
    private final int defaultAmount = 10;

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        String statString = "kills";

        if (args.length > 1) {
            statString = args[1];
        }

        StatsManager.TrackedStat stat = WbsEnums.getEnumFromString(StatsManager.TrackedStat.class, statString);

        if (stat == null) {
            // TODO: Make this show all available stats, for future proofing
            sendMessage("Invalid stat: " + args[1] + ".", sender);
            return true;
        }

        StatsManager.getTopAsync(stat, (top) -> showTop(top, stat, sender));

        return true;
    }

    private void showTop(List<QuakePlayer> top, StatsManager.TrackedStat stat, CommandSender sender) {

        sendMessage("Top " + top.size() + " players (" + WbsEnums.toPrettyString(stat) + "):", sender);

        int i = 1;
        for (QuakePlayer player : top) {
            sendMessage("&6" + i + ") &h" + player.getName() + "&r> &h" + stat.of(player), sender);
        }
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> choices = new LinkedList<>();

        if (args.length == 2) {
            choices.addAll(Arrays.asList(
                    "kills",
                    "headshots",
                    "wins"
            ));
        }

        return choices;
    }
}
