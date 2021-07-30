package wbs.quake;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static void saveArenas(ConfigurationSection section) {
        arenas.values().forEach(arena -> arena.writeToConfig(section));
    }

    public static void loadArenas(YamlConfiguration arenaConfig) {
        for (String key : arenaConfig.getKeys(false)) {
            ConfigurationSection section = arenaConfig.getConfigurationSection(key);

            assert section != null;

            registerArena(new Arena(section));
        }
    }

    public static Collection<Arena> getAllArenas() {
        return arenas.values();
    }

    public static List<String> getArenaNames() {
        return arenas.values().stream().map(Arena::getName).collect(Collectors.toList());
    }
}
