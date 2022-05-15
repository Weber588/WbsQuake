package wbs.quake.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.QuakeDB;
import wbs.utils.util.database.AbstractDataManager;
import wbs.utils.util.database.CollateFunction;
import wbs.utils.util.database.WbsRecord;
import wbs.utils.util.database.WbsTable;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlayerManager extends AbstractDataManager<QuakePlayer, UUID> {
    public PlayerManager(WbsPlugin plugin, WbsTable table) {
        super(plugin, table);
    }

    @Override
    protected @NotNull QuakePlayer fromRecord(@NotNull WbsRecord record) {
        return new QuakePlayer(record);
    }

    @Override
    protected @NotNull QuakePlayer produceDefault(UUID uuid) {
        return new QuakePlayer(uuid);
    }

    public int getAsync(Player player, @NotNull Consumer<QuakePlayer> callback) {
        return super.getAsync(player.getUniqueId(), callback);
    }

    @NotNull
    public List<UUID> getUUIDs(String username) {
        List<WbsRecord> records = QuakeDB.playerTable.selectOnField(QuakeDB.nameField, username, CollateFunction.NOCASE);

        return records.stream()
                .map(record ->
                        record.getValue(QuakeDB.uuidField, String.class))
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }

    public void getUUIDsAsync(String username, Consumer<List<UUID>> callback) {
        plugin.getAsync(() -> getUUIDs(username), callback);
    }
}
