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
            sendMessage("Invalid stat: " + args[1] + ". Please choose from the following: &h" +
                    WbsEnums.joiningPrettyStrings(StatsManager.TrackedStat.class, ", "), sender);
            return true;
        }

        int amount = 5;
        if (args.length > 2) {
            String amountString = args[1];

            try {
                amount = Integer.parseInt(amountString);
            } catch (NumberFormatException e) {
                sendMessage("Invalid amount: " + amountString + ". Use a number between 1 and " + StatsManager.topListSize + ".", sender);
                return true;
            }
        }

        int finalAmount = amount;
        StatsManager.getTopAsync(stat, (top) -> showTop(top, finalAmount, stat, sender));

        return true;
    }

    private void showTop(List<QuakePlayer> top, int amount, StatsManager.TrackedStat stat, CommandSender sender) {
        sendMessage("Top " + Math.min(amount, top.size()) + " players (" + WbsEnums.toPrettyString(stat) + "):", sender);

        int i = 1;
        for (QuakePlayer player : top) {
            sendMessage("&6" + (i++) + ") &h" + player.getName() + "&r> &h" + formatDouble(stat.of(player)), sender);
            if (i > amount) break;
        }
    }

    private String formatDouble(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        }

        return String.valueOf(value);
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> choices = new LinkedList<>();

        if (args.length == 2) {
            choices.addAll(WbsEnums.toStringList(StatsManager.TrackedStat.class));
        }

        return choices;
    }
}
