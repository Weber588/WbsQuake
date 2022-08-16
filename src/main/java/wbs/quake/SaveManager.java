package wbs.quake;

import org.bukkit.scheduler.BukkitRunnable;
import wbs.quake.player.QuakePlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class SaveManager {
    private SaveManager() {}

    public static SaveMode saveMode = SaveMode.ROUND_END;

    private static Set<QuakePlayer> toSave = new HashSet<>();

    // in ticks
    public static int saveFrequency = 300 * 20;

    public static void startTimer() {
        if (saveMode != SaveMode.TIMER) {
            throw new IllegalStateException("Timer cannot be scheduled when save mode is not " + SaveMode.TIMER + ".");
        }

        if (saveFrequency <= 0) {
            saveFrequency = 300;
        }

        WbsQuake plugin = WbsQuake.getInstance();

        new BukkitRunnable() {
            @Override
            public void run() {
                save();
            }
        }.runTaskTimerAsynchronously(plugin, saveFrequency, saveFrequency);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static int save() {
        int saved = toSave.size();

        if (saved > 0) {
            QuakeDB.getPlayerManager().save(toSave);
            toSave.clear();

            WbsQuake plugin = WbsQuake.getInstance();
            plugin.logger.info("Saved " + saved + " player record(s). Recalculating leaderboards...");

            StatsManager.recalculateAll();
        }

        return saved;
    }

    public static void markToSave(QuakePlayer player) {
        toSave.add(player);
    }

    public static void markToSave(Collection<QuakePlayer> players) {
        toSave.addAll(players);
    }

    public static void saveAsync() {
        WbsQuake.getInstance().runAsync(SaveManager::save);
    }

    public enum SaveMode {
        ROUND_END,
        TIMER,
        ;
    }
}
