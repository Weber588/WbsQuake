package wbs.quake;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.killperks.KillPerk;
import wbs.quake.powerups.PowerUp;
import wbs.quake.upgrades.UpgradePath;
import wbs.quake.upgrades.UpgradePathType;
import wbs.quake.upgrades.UpgradeableOption;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.plugin.WbsSettings;

import java.io.File;
import java.util.*;

public class QuakeSettings extends WbsSettings {

    private final WbsQuake plugin;

    public QuakeSettings(WbsQuake plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    private YamlConfiguration arenaConfig = null;
    private YamlConfiguration playersConfig = null;
    private YamlConfiguration config = null;
    private YamlConfiguration shopConfig = null;
    private YamlConfiguration miscConfig = null;

    private final String arenaFileName = "arenas.yml";
    private final String miscFileName = "misc.yml";
    private final String configName = "config.yml";

    @Override
    public void reload() {
        errors.clear();
        config = loadDefaultConfig(configName);
        shopConfig = loadConfigSafely(genConfig("shop.yml"));
        miscConfig = loadConfigSafely(genConfig(miscFileName));

        QuakeLobby.reload();

        loadPowerUps();
        setupShop();
        loadMisc();

        File arenaFile = new File(plugin.getDataFolder(), arenaFileName);
        if (arenaFile.exists()) {
            arenaConfig = loadConfigSafely(arenaFile);
        } else {
            plugin.logger.info("Arena file missing.");
        }

        String directory = configName + "/options";

        if (config.contains("options.save-mode")) {
            String saveModeString = config.getString("options.save-mode", SaveManager.saveMode.name());
            SaveManager.SaveMode mode = WbsEnums.getEnumFromString(SaveManager.SaveMode.class, saveModeString);
            if (mode != null) {
                SaveManager.saveMode = mode;
            } else {
                logError("Invalid mode: " + saveModeString + ". Please choose from the following: "
                        + WbsEnums.joiningPrettyStrings(SaveManager.SaveMode.class, ", "),
                        directory + "/save-mode");
            }
        }
        if (SaveManager.saveMode == SaveManager.SaveMode.TIMER && config.contains("options.save-frequency")) {
            // in seconds
            int frequency = config.getInt("options.save-frequency", 300);
            if (frequency < 30) {
                logError("Save frequency must be greater than 30 seconds.", directory + "/save-frequency");
            } else {
                SaveManager.saveFrequency = frequency * 20;
                SaveManager.startTimer();
            }
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
        if (config.contains("options.player-cache-size")) {
            QuakeDB.getPlayerManager().setCacheSize(config.getInt("options.player-cache-size", 25));
        }
        if (config.contains("options.max-arenas-per-vote")) {
            maxArenasPerVote = config.getInt("options.max-arenas-per-vote", maxArenasPerVote);
        }
        if (config.contains("options.use-economy")) {
            useEconomy = config.getBoolean("options.use-economy", useEconomy);
        }
        if (config.contains("options.economy-format")) {
            economyFormat = config.getString("options.economy-format", economyFormat);
        }

        String killScalingDirectory = directory + "/kill-scaling";

        if (config.contains("options.kill-scaling.enabled")) {
            doKillScaling = config.getBoolean("options.kill-scaling.enabled", doKillScaling);
        }
        if (config.contains("options.kill-scaling.min-players")) {
            minPlayersForExtraKills = config.getInt("options.kill-scaling.min-players", minPlayersForExtraKills);
            if (minPlayersForExtraKills < 1) {
                logError("Min players must be at least 1.", killScalingDirectory + "/min-players");
            }
        }
        if (config.contains("options.kill-scaling.player-increment")) {
            killScalingPlayerIncrement = config.getInt("options.kill-scaling.player-increment", killScalingPlayerIncrement);
            if (killScalingPlayerIncrement < 1) {
                logError("Player increment must be at least 1.", killScalingDirectory + "/player-increment");
                killScalingPlayerIncrement = 1;
            }
        }
        if (config.contains("options.kill-scaling.point-increment")) {
            killScalingPointIncrement = config.getInt("options.kill-scaling.point-increment", killScalingPointIncrement);
        }

        ArenaManager.setPlugin(plugin);
        Arena.setPlugin(plugin);

        // Save arenas before reloading - this will only override config changes if changes were made in game first.
        saveArenas();

        loadArenas();
    }

    public boolean useEconomy = false;
    private String economyFormat = "";

    public String formatMoney(double amount) {
        String amountString = String.valueOf(amount);
        if (amount == (int) amount) {
            amountString = amountString.substring(0, amountString.length() - 2);
        }
        return economyFormat.replace("%money%", amountString);
    }

    public boolean findFurthestSpawnpoint;
    public boolean showLeaderboardInGame;
    public String killFormat;
    public String headshotFormat;
    public double moneyPerKill = 10;
    public double headshotBonus = 5;
    public int maxArenasPerVote = 5;

    public double headshotThreshold = 0.65;
    public boolean headshotsGiveBonusPoints = false;

    public boolean doKillScaling = true;
    public int minPlayersForExtraKills = 5;
    public int killScalingPlayerIncrement = 2;
    public int killScalingPointIncrement = 5;

    public final Map<String, PowerUp> powerUps = new HashMap<>();

    private void loadPowerUps() {
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

        PowerUp.validateAll();

        int loaded = PowerUp.allPowerUps().size();
        plugin.logger.info( loaded + " power up" + (loaded != 1 ? "s" : "") + " loaded!");
    }

    /* ============================ */
    //            SHOP              //
    /* ============================ */

    private void setupShop() {
        CosmeticsStore cosmetics = CosmeticsStore.getInstance();

        ConfigurationSection cosmeticsSection = shopConfig.getConfigurationSection("cosmetics");

        String directory = "shop.yml/cosmetics";
        if (cosmeticsSection == null) {
            logError("Cosmetics section missing!", directory);
        } else {
            cosmetics.loadCosmetics(cosmeticsSection, directory);
        }

        loadUpgradePaths();
        loadKillPerks();
    }

    private final Map<String, UpgradePath> paths = new HashMap<>();
    public UpgradeableOption getOption(String key, int currentProgress) {
        if (paths.get(key) == null) {
            plugin.logger.warning("A null path was retrieved! Key: " + key);
        }
        return new UpgradeableOption(paths.get(key), currentProgress);
    }

    private void loadUpgradePaths() {
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
        configurePath(upgradePaths, "piercing", UpgradePathType.NUMBER);
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

    private final Map<String, KillPerk> killPerks = new LinkedHashMap<>();
    public KillPerk getKillPerk(String key) {
        return killPerks.get(key);
    }

    public Collection<KillPerk> allKillPerks() {
        return killPerks.values();
    }

    public void loadKillPerks() {
        killPerks.clear();
        ConfigurationSection killPerksSection = shopConfig.getConfigurationSection("kill-perks");

        String directory = "shop.yml/kill-perks";

        if (killPerksSection == null) {
            logError("kill-perks missing in shop.yml!", directory);
            return;
        }

        List<KillPerk> unsortedPerks = new LinkedList<>();
        int perksFound = 0;
        for (String key : killPerksSection.getKeys(false)) {
            ConfigurationSection perkSection = killPerksSection.getConfigurationSection(key);
            if (perkSection == null) {
                logError(key + " must be a section.", directory + "/" + key);
                continue;
            }

            try {
                KillPerk newPerk = new KillPerk(perkSection, directory + "/" + key);

                unsortedPerks.add(newPerk);
                perksFound++;
            } catch (InvalidConfigurationException ignored) {}
        }

        logger.info("Loaded " + perksFound + " perk" + (perksFound != 1 ? "s" : ""));

        unsortedPerks.stream()
                .sorted(Comparator.comparingDouble(a -> a.price))
                .forEach(cosmetic -> killPerks.put(cosmetic.getId(), cosmetic));
    }

    /* ============================ */
    //            MISC              //
    /* ============================ */

    private void loadMisc() {
        ItemManager.loadItems(miscConfig, "misc.yml");

        ConfigurationSection lobbySection = miscConfig.getConfigurationSection("lobby");

        if (lobbySection == null) {
            logger.info("Lobby section missing from misc.yml! It is recommended that you regenerate the file.");
        } else {
            String directory = "misc.yml/lobby";
            String locString = lobbySection.getString("lobby-spawn");

            if (locString != null) {
                Location lobbySpawn = locationFromString(locString, directory + "/lobby-spawn");

                QuakeLobby.getInstance().setLobbySpawn(lobbySpawn);
            }
        }
    }

    private Location locationFromString(String locString, String directory) {
        String[] args = locString.split(",");
        if (args.length != 6) {
            logError("Invalid lobby-spawn string: " + locString, directory + "/lobby-spawn");
            return null;
        }

        double x, y, z;
        float pitch, yaw;
        try {
            x = Double.parseDouble(args[0]);
            y = Double.parseDouble(args[1]);
            z = Double.parseDouble(args[2]);
            pitch = Float.parseFloat(args[3]);
            yaw = Float.parseFloat(args[4]);
        } catch (NumberFormatException e) {
            logError("Invalid location: " + locString, directory);
            return null;
        }

        World world = Bukkit.getWorld(args[5]);
        if (world == null) {
            logError("Invalid world: " + args[5], directory);
            return null;
        }

        return new Location(world, x, y, z, yaw, pitch);
    }

    public void saveLobbySpawn() {
        Location lobbySpawn = QuakeLobby.getInstance().getLobbySpawn();
        if (lobbySpawn != null) {
            String lobbySpawnString =
                    lobbySpawn.getX() + "," +
                            lobbySpawn.getY() + "," +
                            lobbySpawn.getZ() + "," +
                            lobbySpawn.getPitch() + "," +
                            lobbySpawn.getYaw() + "," +
                            Objects.requireNonNull(lobbySpawn.getWorld()).getName();

            saveYamlData(miscConfig,
                    miscFileName,
                    "Lobby Spawn",
                    yaml ->
                            yaml.set("lobby.lobby-spawn", lobbySpawnString)
            );
        }
    }

    public void loadArenas() {
        if (arenaConfig != null) {
            ArenaManager.loadArenas(arenaConfig);
            int amount = ArenaManager.getAllArenas().size();
            plugin.logger.info(amount + " arena" + (amount != 1 ? "s" : "") + " loaded.");
        } else {
            plugin.logger.info("No arenas found.");
        }
    }

    public void saveArenas() {
        arenaConfig = ArenaManager.saveArenas(arenaConfig);
    }

    public YamlConfiguration writeArenas() {
        return saveYamlData(arenaConfig, arenaFileName, "arena", ArenaManager::writeArenas);
    }
}
