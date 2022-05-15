package wbs.quake.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.EconomyUtil;
import wbs.quake.QuakeDB;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.database.AbstractDataManager;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MoneyCommand extends WbsSubcommand {
    public MoneyCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "money");
    }

    private enum MoneyArg {
        GIVE, TAKE, SET, CHECK
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length < 2) {
            sendUsage("<" + WbsEnums.joiningPrettyStrings(MoneyArg.class, "|") + "> <player> <points>", sender, label, args);
            return true;
        }

        MoneyArg arg = WbsEnums.getEnumFromString(MoneyArg.class, args[1]);
        if (arg == null) {
            sendMessage("Invalid arg: " + args[1] + "&r. Please choose from the following: "
                    + WbsEnums.joiningPrettyStrings(MoneyArg.class, ", "), sender);
            return true;
        }

        if (args.length < 3) {
            sendUsage("<player> <points>", sender, label, args);
            return true;
        }

        String playerString = args[2];

        int amount = 0;
        if (arg != MoneyArg.CHECK) {
            if (args.length < 4) {
                sendUsage("<amount>", sender, label, args);
                return true;
            }

            String amountArg = args[3];
            try {
                amount = Integer.parseInt(amountArg);
            } catch (NumberFormatException e) {
                sendMessage("Invalid amount: " + amountArg + "&r. Please use an integer.", sender);
                return true;
            }
        }

        UUID playerUUID = null;
        Player online = Bukkit.getPlayer(playerString);
        if (online != null) {
            playerUUID = online.getUniqueId();
        }

        int finalAmount = amount;
        if (playerUUID != null) {
            QuakeDB.getPlayerManager().getAsync(playerUUID, player -> apply(player, arg, finalAmount, sender));
        } else {
            QuakeDB.getPlayerManager().getUUIDsAsync(playerString, uuids -> findUUIDs(uuids, arg, finalAmount, playerString, sender));
        }

        return true;
    }

    private void findUUIDs(List<UUID> uuids, MoneyArg arg, int points, String playerString, CommandSender sender) {
        if (uuids.isEmpty()) {
            sendMessage("Player not found: " + playerString, sender);
        } else if (uuids.size() == 1) {
            QuakeDB.getPlayerManager().getAsync(uuids.get(0), player -> apply(player, arg, points, sender));
        } else {
            sendMessage("Duplicate UUIDs for username &h" + playerString + "&r found. Please choose from the following: ", sender);
            String commandTemplate = "/wbsquake money " + arg.name().toLowerCase() + " ";
            int index = 1;
            for (UUID uuid : uuids) {
                String command = commandTemplate + uuid.toString() + " " + points;
                plugin.buildMessageNoPrefix(index + ") " + uuid)
                        .setFormatting("&h")
                        .addHoverText("&6Click to run &h" + command)
                        .addClickCommand(command)
                        .send(sender);
                index++;
            }
        }
    }

    private void apply(QuakePlayer player, MoneyArg arg, int money, CommandSender sender) {
        switch (arg) {
            case GIVE:
                player.giveMoney(money);
                sendMessage("Gave " + player.getName() + " " + EconomyUtil.formatMoney(money) + ". New total: " + EconomyUtil.formatMoneyFor(player), sender);
                break;
            case TAKE:
                player.giveMoney(-money);
                sendMessage("Took " + EconomyUtil.formatMoney(money) + " from " + player.getName() + ". New total: " + EconomyUtil.formatMoneyFor(player), sender);
                break;
            case SET:
                player.setMoney(money);
                sendMessage("Set " + player.getName() + "'s total to " + EconomyUtil.formatMoney(money) + ".", sender);
                break;
            case CHECK:
                sendMessage(player.getName() + "'s money: " + EconomyUtil.formatMoneyFor(player) + ".", sender);
                break;
        }

        if (arg != MoneyArg.CHECK) {
            QuakeDB.getPlayerManager().saveAsync(Collections.singleton(player));
        }
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        switch (args.length) {
            case 2:
                return WbsEnums.toStringList(MoneyArg.class);
            case 3:
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            default:
                return null;
        }
    }
}
