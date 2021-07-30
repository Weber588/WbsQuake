package wbs.quake;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import wbs.quake.powerups.PowerUp;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.plugin.WbsSettings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QuakeSettings extends WbsSettings {

    private final WbsQuake plugin;

    public QuakeSettings(WbsQuake plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    private YamlConfiguration arenaConfig = null;
    private YamlConfiguration playersConfig = null;
    private YamlConfiguration config = null;
    private YamlConfiguration itemsConfig = null;

    @Override
    public void reload() {
        config = loadDefaultConfig("config.yml");
   //    itemsConfig = loadConfigSafely(genConfig("items.yml"));

        loadItems();
        loadPowerUps();

        File arenaFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (arenaFile.exists()) {
            arenaConfig = loadConfigSafely(arenaFile);
        } else {
            plugin.logger.info("Arena file missing.");
        }

        if (config.contains("options.furthest-spawnpoint")) {
            findFurthestSpawnpoint = config.getBoolean("options.furthest-spawnpoint", true);
        }
        if (config.contains("options.show-leaderboard-in-game")) {
            showLeaderboardInGame = config.getBoolean("options.show-leaderboard-in-game", true);
        }
    }

    public boolean findFurthestSpawnpoint;
    public boolean showLeaderboardInGame;

    public Map<String, ItemStack> items = new HashMap<>();

    public final Map<String, PowerUp> powerUps = new HashMap<>();

    public void loadItems() {
        items.clear();

        if (itemsConfig == null) {
            logError("Items config missing! Regenerate your config, or re-add the power ups section!", "config.yml/power-ups");
            return;
        }

        int i = 0;
        for (String key : itemsConfig.getKeys(false)) {
            ItemStack item = itemsConfig.getItemStack(key);

            items.put(key, item);
            i++;
        }
        
        plugin.logger.info(i + " items loaded!");
    }

    public void loadPowerUps() {
        powerUps.clear();
        ConfigurationSection powerUpsConfig = config.getConfigurationSection("power-ups");

        if (powerUpsConfig == null) {
            logError("Power ups section missing! Regenerate your config, or re-add the power ups section!", "config.yml/power-ups");
            return;
        }

        int i = 0;
        for (String key : powerUpsConfig.getKeys(false)) {
            ConfigurationSection thisPowerUpSection = powerUpsConfig.getConfigurationSection(key);
            assert thisPowerUpSection != null;
            PowerUp powerUp = PowerUp.createPowerUp(thisPowerUpSection, "config.yml/power-ups/" + key);

            powerUps.put(key, powerUp);
            i++;
        }
        plugin.logger.info(i + " power ups loaded!");
    }

    public void loadPlayers() {
        if (playersConfig != null) {
            PlayerManager.loadPlayers(playersConfig);
        }
    }


    public void savePlayers() {
        playersConfig = saveYamlData(playersConfig, "players.yml", "player", PlayerManager::savePlayers);
    }

    public void loadArenas() {
        if (arenaConfig != null) {
            ArenaManager.loadArenas(arenaConfig);
            plugin.logger.info(ArenaManager.getAllArenas().size() + " arenas loaded.");
        } else {
            plugin.logger.info("No arenas found.");
        }
    }

    public void saveArenas() {
        arenaConfig = saveYamlData(arenaConfig, "arena.yml", "arena", ArenaManager::saveArenas);
    }
}
