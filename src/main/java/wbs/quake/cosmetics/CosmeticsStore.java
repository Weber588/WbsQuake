package wbs.quake.cosmetics;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.quake.QuakeSettings;
import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.trails.StandardTrail;
import wbs.quake.cosmetics.trails.Trail;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;

import java.util.*;

public class CosmeticsStore {

    private static CosmeticsStore instance;
    public static CosmeticsStore getInstance() {
        if (instance == null) instance = new CosmeticsStore(WbsQuake.getInstance());

        return instance;
    }

    /* ============================ */
    //            SETUP             //
    /* ============================ */

    private final WbsQuake plugin;
    private final QuakeSettings settings;
    private CosmeticsStore(WbsQuake plugin) {
        this.plugin = plugin;
        settings = plugin.settings;
    }

    private int cosmeticTypesLoaded = 0;

    public void loadCosmetics(ConfigurationSection cosmeticsSection, String directory) {
        String key = "trails";

        cosmeticTypesLoaded = 0;

        ConfigurationSection trailsSection = cosmeticsSection.getConfigurationSection(key);
        if (trailsSection == null) {
            trails.put("default", buildDefaultTrail());
            plugin.settings.logError("Trails section missing! Trails will be disabled in the shop.", directory + "/" + key);
        } else {
            loadTrails(trailsSection, directory + "/" + key);
            if (!trailsEnabled) {
                plugin.logger.info("No valid trails found! Disabling trails in shop.");
            } else {
                cosmeticTypesLoaded++;
            }
        }

        key = "gun-skins";
        ConfigurationSection skinsSection = cosmeticsSection.getConfigurationSection(key);
        if (skinsSection == null) {
            GunSkin defaultSkin = buildGunSkin(
                    "default",
                    DEFAULT_SKIN_MATERIAL,
                    "",
                    0);
            skins.put("default", defaultSkin);
            plugin.settings.logError("Skins section missing! Skins will be disabled in the shop.", directory + "/" + key);
        } else {
            loadSkins(skinsSection, directory + "/" + key);
            if (!skinsEnabled) {
                plugin.logger.info("No valid gun skins found! Disabling gun skins in shop.");
            } else {
                cosmeticTypesLoaded++;
            }
        }

        key = "death-sounds";
        ConfigurationSection deathSoundsSection = cosmeticsSection.getConfigurationSection(key);
        if (deathSoundsSection == null) {
            deathSounds.putIfAbsent("default", buildDefaultDeathSound());
            plugin.settings.logError("Death sounds section missing! Death sounds will be disabled in the shop.", directory + "/" + key);
        } else {
            loadDeathSounds(deathSoundsSection, directory + "/" + key);
            if (!deathSoundsEnabled) {
                plugin.logger.info("No valid death sounds found! Disabling death sounds in shop.");
            } else {
                cosmeticTypesLoaded++;
            }
        }

        key = "shoot-sounds";
        ConfigurationSection shootSoundsSection = cosmeticsSection.getConfigurationSection(key);
        if (shootSoundsSection == null) {
            shootSounds.putIfAbsent("default", buildDefaultShootSound());
            plugin.settings.logError("Shoot sounds section missing! Shoot sounds will be disabled in the shop.", directory + "/" + key);
        } else {
            loadShootSounds(shootSoundsSection, directory + "/" + key);
            if (!shootSoundsEnabled) {
                plugin.logger.info("No valid shoot sounds found! Disabling shoot sounds in shop.");
            } else {
                cosmeticTypesLoaded++;
            }
        }
    }

    public boolean trailsEnabled;
    private final LinkedHashMap<String, Trail> trails = new LinkedHashMap<>();

    public boolean skinsEnabled;
    public boolean requirePreviousSkin;
    private final LinkedHashMap<String, GunSkin> skins = new LinkedHashMap<>();

    public boolean armourEnabled;
    private final LinkedHashMap<String, SelectableCosmetic> armourSets = new LinkedHashMap<>();

    public boolean killMessagesEnabled;
    private final LinkedHashMap<String, SelectableCosmetic> killMessages = new LinkedHashMap<>();

    public boolean deathEffectsEnabled;
    private final LinkedHashMap<String, SelectableCosmetic> deathEffects = new LinkedHashMap<>();

    public boolean shootSoundsEnabled;
    private final LinkedHashMap<String, ShootSound> shootSounds = new LinkedHashMap<>();

    public boolean deathSoundsEnabled;
    private final LinkedHashMap<String, DeathSound> deathSounds = new LinkedHashMap<>();

    private <T extends SelectableCosmetic> void populateOrdered(List<T> unsorted, LinkedHashMap<String, T> map) {
        unsorted.stream()
                .sorted(Comparator.comparingDouble(a -> a.price))
                .forEach(cosmetic -> map.put(cosmetic.getId(), cosmetic));
    }

    /* ============================ */
    //            TRAILS            //
    /* ============================ */

    private void loadTrails(@NotNull ConfigurationSection section, String directory) {
        trails.clear();

        int trailsAdded = 0;
        List<Trail> unsortedTrails = new LinkedList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection trailSection = section.getConfigurationSection(key);
            if (trailSection == null) {
                settings.logError(key + " must be a section.", directory + "/" + key);
                continue;
            }

            try {
                Trail trail = Trail.buildTrail(trailSection, directory + "/" + key);

                if (trail != null) {
                    unsortedTrails.add(trail);
                    trailsAdded++;
                }
            } catch (InvalidConfigurationException ignored) {}
        }

        trailsEnabled = trailsAdded > 0;

        if (trails.get("default") == null) {
            trails.put("default", buildDefaultTrail());
        }

        populateOrdered(unsortedTrails, trails);
    }

    public Collection<Trail> allTrails() {
        return trails.values();
    }

    @NotNull
    public Trail getTrail(String id) {
        Trail trail = trails.get(id);
        return trail == null ? trails.get("default") : trail;
    }

    public Trail getDefaultTrail() {
        return trails.get("default");
    }

    private Trail buildDefaultTrail() {
        StandardTrail trail = new StandardTrail("default",
                Particle.REDSTONE,
                Material.STICK,
                "&c&lDefault",
                "",
                Collections.singletonList("The default trail."),
                0);

        Particle.DustOptions options = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1f);

        trail.setOption(options);
        trail.setAmountPerBlock(6);

        return trail;
    }

    /* ============================ */
    //            SKINS             //
    /* ============================ */

    private static final Material DEFAULT_SKIN_MATERIAL = Material.WOODEN_HOE;

    private void loadSkins(@NotNull ConfigurationSection section, String directory) {
        skins.clear();

        requirePreviousSkin = section.getBoolean("require-previous", true);

        List<GunSkin> unsortedSkins = new LinkedList<>();

        ConfigurationSection shopSection = section.getConfigurationSection("shop");
        if (shopSection != null) {
            skinsEnabled = false;

            int skinsAdded = 0;
            for (String key : shopSection.getKeys(false)) {
                ConfigurationSection skinSection = WbsConfigReader.getRequiredSection(shopSection, key, settings, directory);

                double price = skinSection.getDouble("price");

                String itemString = skinSection.getString("item");
                if (itemString == null) {
                    settings.logError("Item is a required field for gun skins.", directory + "/" + key + "/item");
                    continue;
                }

                Material material = WbsEnums.getEnumFromString(Material.class, itemString);

                if (material == null) {
                    settings.logError("Invalid material: " + itemString, directory + "/" + key);
                    continue;
                }

                String permission = "wbsquake.cosmetics.gun-skin." + key;

                unsortedSkins.add(buildGunSkin(key, material, permission, price));
                skinsAdded++;
            }

            skinsEnabled = skinsAdded > 0;
        }

        String defaultMaterialString = section.getString("default-skin", Material.WOODEN_HOE.name());

        Material defaultMaterial = WbsEnums.getEnumFromString(Material.class, defaultMaterialString);
        if (defaultMaterial == null) {
            settings.logError("Invalid material: " + defaultMaterialString, directory + "/default-skin");
            defaultMaterial = DEFAULT_SKIN_MATERIAL;
        }

        GunSkin defaultSkin = buildGunSkin("default", defaultMaterial, "", 0);
        skins.put("default", defaultSkin);

        populateOrdered(unsortedSkins, skins);
    }

    private GunSkin buildGunSkin(String key, Material material, String permission, double price) {
        String prettyMaterialName = WbsEnums.toPrettyString(material);

        String displayName = plugin.dynamicColourise("&b" +
                prettyMaterialName + " Skin");

        List<String> description =
                Collections.singletonList("Use " + prettyMaterialName + " as your gun!");

        return new GunSkin(key, material, displayName, permission, description, price);
    }

    public Collection<GunSkin> allSkins() {
        return skins.values();
    }

    @NotNull
    public GunSkin getSkin(String id) {
        GunSkin skin = skins.get(id);
        return skin == null ? skins.get("default") : skin;
    }

    /* ============================ */
    //         Death Sounds         //
    /* ============================ */

    private DeathSound buildDefaultDeathSound() {
        return new DeathSound("default",
                Material.STICK,
                "&c&lDefault",
                "",
                null,
                0,
                Sound.ENTITY_GENERIC_DEATH,
                1,
                1);
    }

    private void loadDeathSounds(@NotNull ConfigurationSection section, String directory) {
        deathSounds.clear();

        int deathSoundsAdded = 0;
        List<DeathSound> unsortedDeathSounds = new LinkedList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection deathSoundSection = section.getConfigurationSection(key);
            if (deathSoundSection == null) {
                settings.logError(key + " must be a section.", directory + "/" + key);
                continue;
            }

            try {
                DeathSound deathSound = new DeathSound(deathSoundSection, directory + "/" + key);

                unsortedDeathSounds.add(deathSound);
                deathSoundsAdded++;
            } catch (InvalidConfigurationException ignored) {}
        }

        deathSoundsEnabled = deathSoundsAdded > 0;

        deathSounds.putIfAbsent("default", buildDefaultDeathSound());

        populateOrdered(unsortedDeathSounds, deathSounds);
    }

    public Collection<DeathSound> allDeathSounds() {
        return deathSounds.values();
    }

    @NotNull
    public DeathSound getDeathSound(String id) {
        DeathSound deathSound = deathSounds.get(id);
        return deathSound == null ? deathSounds.get("default") : deathSound;
    }

    /* ============================ */
    //         Shoot Sounds         //
    /* ============================ */

    private ShootSound buildDefaultShootSound() {
        return new ShootSound("default",
                Material.STICK,
                "&c&lDefault",
                "",
                null,
                0,
                Sound.ENTITY_GENERIC_EXPLODE,
                2,
                2);

    }

    private void loadShootSounds(@NotNull ConfigurationSection section, String directory) {
        shootSounds.clear();

        int shootSoundsAdded = 0;
        List<ShootSound> unsortedShootSounds = new LinkedList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection shootSoundSection = section.getConfigurationSection(key);
            if (shootSoundSection == null) {
                settings.logError(key + " must be a section.", directory + "/" + key);
                continue;
            }

            try {
                ShootSound shootSound = new ShootSound(shootSoundSection, directory + "/" + key);

                unsortedShootSounds.add(shootSound);
                shootSoundsAdded++;
            } catch (InvalidConfigurationException ignored) {}
        }

        shootSoundsEnabled = shootSoundsAdded > 0;

        shootSounds.putIfAbsent("default", buildDefaultShootSound());

        populateOrdered(unsortedShootSounds, shootSounds);
    }

    public Collection<ShootSound> allShootSounds() {
        return shootSounds.values();
    }

    @NotNull
    public ShootSound getShootSound(String id) {
        ShootSound shootSound = shootSounds.get(id);
        return shootSound == null ? shootSounds.get("default") : shootSound;
    }

    public int getCosmeticTypesLoaded() {
        return cosmeticTypesLoaded;
    }
}
