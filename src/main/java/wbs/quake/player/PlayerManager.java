package wbs.quake.player;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import wbs.quake.WbsQuake;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerManager {
    private PlayerManager() {}

    public static void initialize() {}

    private static final Map<UUID, QuakePlayer> players = new HashMap<>();

    public static QuakePlayer getPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (players.get(uuid) == null) {
            players.put(uuid, new QuakePlayer(player.getUniqueId()));
        }
        return players.get(uuid);
    }

    public static int loadPlayers(ConfigurationSection section) {
        players.clear();
        int i = 0;

        for (String key : section.getKeys(false)) {
            ConfigurationSection playerSection = section.getConfigurationSection(key);
            if (playerSection == null) {
                WbsQuake.getInstance().logger.warning("Invalid player section! (" + key + ")");
                continue;
            }

            QuakePlayer quakePlayer = new QuakePlayer(playerSection);

            players.put(quakePlayer.getUUID(), quakePlayer);
            i++;
        }

        return i;
    }

    public static void savePlayers(ConfigurationSection section) {
        players.values().forEach(p -> p.writeToConfig(section));
    }
}