package wbs.quake;

import wbs.quake.player.QuakePlayer;
import wbs.utils.util.pluginhooks.VaultWrapper;

public final class EconomyUtil {
    private EconomyUtil() {}

    public static boolean hasMoney(QuakePlayer player, double amount) {
        if (WbsQuake.getInstance().settings.useEconomy) {
            return VaultWrapper.hasMoney(player.getPlayer(), amount);
        } else {
            return player.getMoney() >= amount;
        }
    }

    public static double getMoney(QuakePlayer player) {
        if (WbsQuake.getInstance().settings.useEconomy) {
            return VaultWrapper.getMoney(player.getPlayer());
        } else {
            return player.getMoney();
        }
    }

    public static boolean takeMoney(QuakePlayer player, double amount) {
        if (!hasMoney(player, amount)) {
            return false;
        }

        if (WbsQuake.getInstance().settings.useEconomy) {
            return VaultWrapper.takeMoney(player.getPlayer(), amount);
        } else {
            player.giveMoney(-amount);
        }

        return true;
    }

    public static String formatMoney(double amount) {
        if (WbsQuake.getInstance().settings.useEconomy) {
            return VaultWrapper.formatMoney(amount);
        } else {
            return WbsQuake.getInstance().settings.formatMoney(amount);
        }
    }

    public static String formatMoneyFor(QuakePlayer player) {
        return formatMoney(getMoney(player));
    }

    public static void giveMoney(QuakePlayer player, double moneyToGive) {
        if (WbsQuake.getInstance().settings.useEconomy) {
            VaultWrapper.giveMoney(player.getPlayer(), moneyToGive);
        } else {
            player.giveMoney(moneyToGive);
        }
    }
}
