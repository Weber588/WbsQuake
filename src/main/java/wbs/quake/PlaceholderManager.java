package wbs.quake;

import org.bukkit.OfflinePlayer;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.pluginhooks.PlaceholderAPIWrapper;

import java.util.*;

public final class PlaceholderManager {
    private PlaceholderManager() {}

    private static final String BALANCE_KEY = "balance";
    private static final String TOP_KEY = "top";

    private static final String NOT_FOUND = "N/A";

    public static void registerPlaceholders() {
        PlaceholderAPIWrapper.registerSimplePlaceholder(WbsQuake.getInstance(), "Weber588", PlaceholderManager::parseParams);
    }

    private static String parseParams(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase(BALANCE_KEY)) {
            QuakePlayer cached = QuakeDB.getPlayerManager().getCached(player.getUniqueId());

            if (cached != null) {
                return EconomyUtil.formatMoneyFor(cached);
            } else {
                return NOT_FOUND;
            }
        } else if (params.toLowerCase().startsWith(BALANCE_KEY.toLowerCase() + "_")) {
            String lookup = params.substring(BALANCE_KEY.length() + 1);

            Map<UUID, QuakePlayer> cache = QuakeDB.getPlayerManager().getCache();
            for (UUID uuid : cache.keySet()) {
                QuakePlayer cached = cache.get(uuid);

                if (cached.getName().equalsIgnoreCase(lookup)) {
                    return EconomyUtil.formatMoneyFor(cached);
                }
            }

            return NOT_FOUND;
        } else if (params.startsWith(TOP_KEY + "_")) {
            String[] args = params.split("_");

            if (args.length < 3) {
                return null;
            }

            String statName = args[1];

            StatsManager.TrackedStat stat = WbsEnums.getEnumFromString(StatsManager.TrackedStat.class, statName);

            if (stat == null) {
                return "[Invalid stat: " + args[1] + "]";
            }

            int place;
            try {
                place = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                return "[Invalid place number: " + args[2] + "]";
            }

            if (place <= 0) {
                return "[Invalid place number: " + args[2] + "]";
            }

            PlayerProperty property;
            if (args.length >= 4) {
                property = WbsEnums.getEnumFromString(PlayerProperty.class, args[3]);
                if (property == null) {
                    return "[Invalid property: " + args[3] + "]";
                }
            } else {
                property = PlayerProperty.NAME;
            }

            List<QuakePlayer> top = StatsManager.getTopCached(stat);
            if (top.size() < place) {
                return NOT_FOUND;
            }

            return property.getProperty(top.get(place - 1));
        }

        return null;
    }

    private enum PlayerProperty {
        NAME,
        WINS,
        KILLS,
        HEADSHOTS,
        DEATHS,
        KD,
        MONEY,
        ;

        public String getProperty(QuakePlayer player) {
            switch (this) {
                case NAME:
                    return player.getName();
                case WINS:
                    return player.getWins() + "";
                case KILLS:
                    return player.getKills() + "";
                case HEADSHOTS:
                    return player.getHeadshots() + "";
                case DEATHS:
                    return player.getDeaths() + "";
                case KD:
                    return player.getKills() / (double) player.getDeaths() + "";
                case MONEY:
                    return player.getMoney() + "";
            }

            return null;
        }
    }
}
