package wbs.quake;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import wbs.quake.player.PlayerManager;
import wbs.quake.upgrades.UpgradePath;
import wbs.quake.upgrades.UpgradePathType;
import wbs.quake.upgrades.UpgradeableOption;
import wbs.quake.powerups.PowerUp;
import wbs.utils.util.plugin.WbsSettings;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuakeSettings extends WbsSettings {

    private final WbsQuake plugin;
    public double headshotThreshold = 0.65;
    public boolean headshotsGiveBonusPoints = false;

    public QuakeSettings(WbsQuake plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    private YamlConfiguration arenaConfig = null;
    private YamlConfiguration playersConfig = null;
    private YamlConfiguration config = null;
    private YamlConfiguration itemsConfig = null;
    private YamlConfiguration shopConfig = null;

    private final String arenaFileName = "arenas.yml";
    private final String playerFileName = "players.yml";

    @Override
    public void reload() {
        errors.clear();
        config = loadDefaultConfig("config.yml");
   //    itemsConfig = loadConfigSafely(genConfig("items.yml"));
        shopConfig = loadConfigSafely(genConfig("shop.yml"));

        loadItems();
        loadPowerUps();
        loadUpgradePaths();

        File arenaFile = new File(plugin.getDataFolder(), arenaFileName);
        if (arenaFile.exists()) {
            arenaConfig = loadConfigSafely(arenaFile);
        } else {
            plugin.logger.info("Arena file missing.");
        }

        File playerFile = new File(plugin.getDataFolder(), playerFileName);
        if (playerFile.exists()) {
            playersConfig = loadConfigSafely(playerFile);
        } else {
            plugin.logger.info("Player file missing.");
        }

        if (config.contains("options.furthest-spawnpoint")) {
            findFurthestSpawnpoint = config.getBoolean("options.furthest-spawnpoint", true);
        }
        if (config.contains("options.show-leaderboard-in-game")) {
            showLeaderboardInGame = config.getBoolean("options.show-leaderboard-in-game", true);
        }
        if (config.contains("options.headshot-threshold")) {
            headshotThreshold = config.getDouble("options.headshot-threshold", headshotThreshold);
        }
        if (config.contains("options.headshots-give-bonus-points")) {
            headshotsGiveBonusPoints = config.getBoolean("options.headshots-give-bonus-points", headshotsGiveBonusPoints);
        }
        if (config.contains("options.kill-format")) {
            killFormat = config.getString("options.kill-format", killFormat);
        }
        if (config.contains("options.headshot-format")) {
            headshotFormat = config.getString("options.headshot-format", headshotFormat);
        }
        if (config.contains("options.money-per-kill")) {
            moneyPerKill = config.getDouble("options.money-per-kill", moneyPerKill);
        }
        if (config.contains("options.headshot-money-bonus")) {
            headshotBonus = config.getDouble("options.headshot-money-bonus", headshotBonus);
        }
    }

    public boolean findFurthestSpawnpoint;
    public boolean showLeaderboardInGame;
    public String killFormat;
    public String headshotFormat;
    public double moneyPerKill = 10;
    public double headshotBonus = 5;

    public Map<String, ItemStack> items = new HashMap<>();

    public final Map<String, PowerUp> powerUps = new HashMap<>();

    public void loadItems() {
        items.clear();

        if (itemsConfig == null) {
            logError("Items config missing! Regenerate your config, or re-add the power ups section!", "items.yml");
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

    private final Map<String, UpgradePath> paths = new HashMap<>();
    public UpgradeableOption getOption(String key, int currentProgress) {
        if (paths.get(key) == null) {
            plugin.logger.warning("A null path was retrieved! Key: " + key);
        }
        return new UpgradeableOption(paths.get(key), currentProgress);
    }

    public void loadUpgradePaths() {
        paths.clear();
        ConfigurationSection upgradePaths = shopConfig.getConfigurationSection("upgradeable-options");

        String directory = "shop.yml/upgradeable-options";

        if (upgradePaths == null) {
            logError("upgradeable-options missing in shop.yml!", directory);
            return;
        }

        configurePath(upgradePaths, "cooldown", UpgradePathType.TICKS);
        configurePath(upgradePaths, "leap-cooldown", UpgradePathType.TICKS);
        configurePath(upgradePaths, "leap-speed", UpgradePathType.NUMBER);
        configurePath(upgradePaths, "speed", UpgradePathType.PERCENT);
    }

    private void configurePath(ConfigurationSection upgradePaths, String id, UpgradePathType type) {
        List<String> pairs = upgradePaths.getStringList(id);

        String directory = "shop.yml/upgradeable-options/" + id;

        UpgradePath path = new UpgradePath(id, type);

        int pairsAdded = 0;

        int i = 0; // For error help
        for (String pair : pairs) {
            i++; // Fine to increment first as it's just used for errors
            String[] args = pair.split(":");
            if (args.length != 2) {
                logError("Malformed option pair: \"" + pair + "\"." +
                                " Options must be formatted as value:price. Example: 50:500",
                        directory);
                continue;
            }

            double value, price;

            try {
                value = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                logError("Invalid value: " + args[0] + ".", directory + ":" + i);
                continue;
            }
            try {
                price = Double.parseDouble(args[1].replaceAll("\\$", ""));
            } catch (NumberFormatException e) {
                logError("Invalid price: " + args[1] + ".", directory + ":" + i);
                continue;
            }

            path.addValuePricePair(value, price);
            pairsAdded++;
        }

        if (pairsAdded == 0) {
            logError("No values set for " + id + ".", directory);
            return;
        }

        paths.put(id, path);
    }

    public void loadPlayers() {
        if (playersConfig != null) {
            PlayerManager.loadPlayers(playersConfig);
        }
    }


    public void savePlayers() {
        playersConfig = saveYamlData(playersConfig, playerFileName, "player", PlayerManager::savePlayers);
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
        arenaConfig = saveYamlData(arenaConfig, arenaFileName, "arena", ArenaManager::saveArenas);
    }
}
