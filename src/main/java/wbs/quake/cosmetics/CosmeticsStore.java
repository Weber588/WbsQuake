package wbs.quake.cosmetics;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.QuakeSettings;
import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.trails.StandardTrail;
import wbs.quake.cosmetics.trails.Trail;
import wbs.quake.player.PlayerCosmetics;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.WbsEnums;

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

    public void loadCosmetics(ConfigurationSection cosmeticsSection, String directory) {
        ConfigurationSection trailsSection = cosmeticsSection.getConfigurationSection("trails");
        if (trailsSection == null) {
            plugin.settings.logError("Trails section missing! Trails will be disabled in the shop.", directory + "/trails");
        } else {
            loadTrails(trailsSection, directory + "/trails");
            if (!trailsEnabled) {
                plugin.logger.info("No valid trails found! Disabling trails in shop.");
            }
        }

        ConfigurationSection skinsSection = cosmeticsSection.getConfigurationSection("gun-skins");
        if (skinsSection == null) {
            plugin.settings.logError("Skins section missing! Skins will be disabled in the shop.", directory + "/gun-skins");
        } else {
            loadSkins(skinsSection, directory + "/gun-skins");
            if (!skinsEnabled) {
                plugin.logger.info("No valid gun skins found! Disabling gun skins in shop.");
            }
        }
    }

    public boolean trailsEnabled;
    private final Map<String, Trail> trails = new HashMap<>();

    public boolean skinsEnabled;
    public boolean requirePreviousSkin;
    private final Map<String, GunSkin> skins = new HashMap<>();

    /* ============================ */
    //            TRAILS            //
    /* ============================ */

    private void loadTrails(@NotNull ConfigurationSection section, String directory) {
        trails.clear();

        int trailsAdded = 0;
        for (String key : section.getKeys(false)) {
            ConfigurationSection trailSection = section.getConfigurationSection(key);
            if (trailSection == null) {
                settings.logError(key + " must be a section.", directory + "/" + key);
                continue;
            }

            Trail trail = Trail.buildTrail(trailSection, directory + "/" + key);

            if (trail != null) {
                trails.put(key, trail);
                trailsAdded++;
            }
        }

        trailsEnabled = trailsAdded > 0;

        if (trails.get("default") == null) {
            trails.put("default", buildDefaultTrail());
        }
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



    private void loadSkins(@NotNull ConfigurationSection section, String directory) {
        skins.clear();

        requirePreviousSkin = section.getBoolean("require-previous", true);

        ConfigurationSection shopSection = section.getConfigurationSection("shop");
        if (shopSection != null) {
            skinsEnabled = false;

            int skinsAdded = 0;
            for (String key : shopSection.getKeys(false)) {
                double price = shopSection.getDouble(key);
                Material material = WbsEnums.getEnumFromString(Material.class, key);

                if (material == null) {
                    settings.logError("Invalid material: " + key, directory + "/" + key);
                    continue;
                }

                String permission = "wbsquake.cosmetics.gun-skin." + key;

                skins.put(key, buildGunSkin(key, material, permission, price));
                skinsAdded++;
            }

            skinsEnabled = skinsAdded > 0;
        }

        String defaultMaterialString = section.getString("default-skin", Material.WOODEN_HOE.name());

        Material defaultMaterial = WbsEnums.getEnumFromString(Material.class, defaultMaterialString);
        if (defaultMaterial == null) {
            settings.logError("Invalid material: " + defaultMaterialString, directory + "/default-skin");
            defaultMaterial = Material.WOODEN_HOE;
        }

        GunSkin defaultSkin = buildGunSkin("default", defaultMaterial, "", 0);
        skins.put("default", defaultSkin);
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

}