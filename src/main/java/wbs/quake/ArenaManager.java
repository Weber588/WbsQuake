package wbs.quake;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public final class ArenaManager {
    private ArenaManager() {}

    private static WbsQuake plugin;
    public static void setPlugin(WbsQuake plugin) {
        ArenaManager.plugin = plugin;
    }

    private static final Map<String, Arena> arenas = new HashMap<>();

    @Nullable
    public static Arena getArena(String name) {
        return arenas.get(name);
    }

    public static void registerArena(Arena newArena) {
        arenas.put(newArena.getName(), newArena);
    }

    public static void writeArenas(ConfigurationSection section) {
        arenas.values().forEach(arena -> arena.writeToConfig(section));
    }

    public static void loadArenas(YamlConfiguration arenaConfig) {
        arenas.clear();

        for (String key : arenaConfig.getKeys(false)) {
            ConfigurationSection section = arenaConfig.getConfigurationSection(key);

            assert section != null;

            registerArena(new Arena(section));
        }
    }

    public static List<Arena> getAllArenas() {
        return new LinkedList<>(arenas.values());
    }

    public static List<String> getArenaNames() {
        return arenas.values().stream().map(Arena::getName).collect(Collectors.toList());
    }

    public static YamlConfiguration saveArenas(YamlConfiguration arenaConfig) {
        boolean needsSaving = arenas.values().stream().anyMatch(Arena::needsSaving);

        if (needsSaving) {
            plugin.logger.info("An arena needed saving.");
            arenas.values().forEach(arena -> {
                if (arena.needsSaving()) {
                    plugin.logger.info(arena.getDisplayName() + " needed saving.");
                }
            });
            arenaConfig = plugin.settings.writeArenas();
            arenas.values().forEach(Arena::unmarkForSaving);
        }

        return arenaConfig;
    }
}
