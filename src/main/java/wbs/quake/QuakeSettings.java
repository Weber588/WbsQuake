package wbs.quake;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.cosmetics.SelectableCosmetic;
import wbs.quake.cosmetics.ShootSound;
import wbs.quake.cosmetics.trails.StandardTrail;
import wbs.quake.cosmetics.trails.Trail;
import wbs.quake.killperks.KillPerk;
import wbs.quake.player.PlayerManager;
import wbs.quake.upgrades.UpgradePath;
import wbs.quake.upgrades.UpgradePathType;
import wbs.quake.upgrades.UpgradeableOption;
import wbs.quake.powerups.PowerUp;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.particles.LineParticleEffect;
import wbs.utils.util.plugin.WbsSettings;
import wbs.utils.util.string.WbsStringify;

import java.io.File;
import java.util.*;

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
    private YamlConfiguration shopConfig = null;
    private YamlConfiguration miscConfig = null;

    private final String arenaFileName = "arenas.yml";
    private final String playerFileName = "players.yml";
    private final String miscFileName = "misc.yml";

    @Override
    public void reload() {
        errors.clear();
        config = loadDefaultConfig("config.yml");
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

        ArenaManager.setPlugin(plugin);
        Arena.setPlugin(plugin);

        // To initialize the class in case it's never called before the plugin disables
        PlayerManager.initialize();

        loadArenas();
        loadPlayers();
    }

    public boolean findFurthestSpawnpoint;
    public boolean showLeaderboardInGame;
    public String killFormat;
    public String headshotFormat;
    public double moneyPerKill = 10;
    public double headshotBonus = 5;

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
        plugin.logger.info(i + " power up" + (i != 1 ? "s" : "") + " loaded!");
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
                KillPerk shootSound = KillPerk.getPerk(perkSection, directory + "/" + key);

                unsortedPerks.add(shootSound);
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

        ConfigurationSection quakeSection = miscConfig.getConfigurationSection("quake");

        if (quakeSection == null) {
            logger.info("Quake section missing from misc.yml! Using default items.");
        } else {

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
            int amount = ArenaManager.getAllArenas().size();
            plugin.logger.info(amount + " arena" + (amount != 1 ? "s" : "") + " loaded.");
        } else {
            plugin.logger.info("No arenas found.");
        }
    }

    public void saveArenas() {
        arenaConfig = saveYamlData(arenaConfig, arenaFileName, "arena", ArenaManager::saveArenas);
    }
}
