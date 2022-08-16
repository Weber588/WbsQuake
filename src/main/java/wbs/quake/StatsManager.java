package wbs.quake;

import wbs.quake.player.QuakePlayer;
import wbs.utils.util.database.WbsDatabase;
import wbs.utils.util.database.WbsField;
import wbs.utils.util.database.WbsRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public final class StatsManager {
    private StatsManager() {}

    public enum TrackedStat {
        KILLS, HEADSHOTS, WINS, MONEY, DEATHS;

        public WbsField getField() {
            switch (this) {
                case KILLS:
                    return QuakeDB.killsField;
                case HEADSHOTS:
                    return QuakeDB.headshotsField;
                case WINS:
                    return QuakeDB.winsField;
                case MONEY:
                    return QuakeDB.moneyField;
                case DEATHS:
                    return QuakeDB.deathsField;
            }

            return null;
        }

        public double of(QuakePlayer player) {
            switch (this) {
                case KILLS:
                    return player.getKills();
                case HEADSHOTS:
                    return player.getHeadshots();
                case WINS:
                    return player.getWins();
                case MONEY:
                    return player.getMoney();
                case DEATHS:
                    return player.getDeaths();
            }

            return -1;
        }
    }

    public static int topListSize = 25;

    private static final Map<TrackedStat, List<QuakePlayer>> stats = new HashMap<>();

    public static void recalculateAllAsync() {
        WbsQuake.getInstance().runAsync(StatsManager::recalculateAll);
    }

    public static void recalculateAll() {
        for (TrackedStat stat : TrackedStat.values()) {
            recalculate(stat);
        }
        WbsQuake.getInstance().logger.info("Recalculated leaderboards!");
    }

    public static int recalculateAsync(TrackedStat stat, Consumer<List<QuakePlayer>> consumer) {
        return WbsQuake.getInstance().getAsync(() -> recalculate(stat), consumer);
    }

    public static List<QuakePlayer> recalculate(TrackedStat stat) {
        List<QuakePlayer> topList = new LinkedList<>();

        String query = "SELECT * FROM " + QuakeDB.playerTable.getName() + " " +
                "ORDER BY " + Objects.requireNonNull(stat.getField()).getFieldName() + " DESC " +
                "LIMIT " + topListSize;

        WbsDatabase db = QuakeDB.getDatabase();
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            List<WbsRecord> selected = db.select(statement);

            for (WbsRecord record : selected) {
                topList.add(new QuakePlayer(record));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        stats.put(stat, topList);
        return topList;
    }

    public static List<QuakePlayer> getTop(TrackedStat stat) {
        List<QuakePlayer> top = stats.get(stat);

        if (top != null) {
            return top;
        }

        return recalculate(stat);
    }

    public static int getTopAsync(TrackedStat stat, Consumer<List<QuakePlayer>> callback) {
        List<QuakePlayer> top = stats.get(stat);

        if (top != null) {
            callback.accept(top);
            return -1;
        }

        return WbsQuake.getInstance().runAsync(
                () -> recalculate(stat),
                () -> callback.accept(stats.get(stat))
        );
    }

    public static List<QuakePlayer> getTopCached(TrackedStat stat) {
        return stats.getOrDefault(stat, new LinkedList<>());
    }
}
