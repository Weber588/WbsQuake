package wbs.quake.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.quake.QuakeDB;
import wbs.quake.WbsQuake;
import wbs.utils.util.database.WbsRecord;

import java.util.*;
import java.util.function.Consumer;

public final class PlayerManager {
    private PlayerManager() {}

    private static final WbsQuake plugin = WbsQuake.getInstance();

    private static int cacheSize = 25;
    private final static Map<UUID, QuakePlayer> cache = new LinkedHashMap<UUID, QuakePlayer>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, QuakePlayer> eldest) {
            return size() > cacheSize;
        }
    };
    public static void setCacheSize(int size) {
        cacheSize = size;
    }

    @NotNull
    public static QuakePlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    @NotNull
    public static QuakePlayer getPlayer(UUID uuid) {
        if (cache.containsKey(uuid)) return cache.get(uuid);

        List<WbsRecord> records = QuakeDB.playerTable.selectOnField(QuakeDB.uuidField, uuid);

        QuakePlayer player;
        if (records.size() > 0) {
            WbsRecord record = records.get(0);
            player = new QuakePlayer(record);
        } else {
            player = new QuakePlayer(uuid);
        }

        cache.put(uuid, player);
        return player;
    }

    @Nullable
    public static QuakePlayer getCachedPlayer(Player player) {
        return getCachedPlayer(player.getUniqueId());
    }

    @Nullable
    public static QuakePlayer getCachedPlayer(UUID uuid) {
        return cache.get(uuid);
    }

    public static int getPlayerAsync(Player player, @NotNull Consumer<QuakePlayer> callback) {
        return getPlayerAsync(player.getUniqueId(), callback);
    }

    public static int getPlayerAsync(UUID uuid, @NotNull Consumer<QuakePlayer> callback) {
        // Don't bother doing it async if we can get it instantly
        if (cache.containsKey(uuid)) callback.accept(cache.get(uuid));
        return plugin.getAsync(() -> getPlayer(uuid), callback);
    }

    public static void saveCacheAsync() {
        savePlayers(new LinkedList<>(cache.values()));
    }

    public static void savePlayers(Collection<QuakePlayer> players) {
        plugin.runAsync(
                () -> {
                    for (QuakePlayer player : players) {
                        player.upsert();
                    }
                },
                () -> plugin.logger.info("Saved " + players.size() + " players!")
        );
    }
}
