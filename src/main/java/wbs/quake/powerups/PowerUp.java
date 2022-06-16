package wbs.quake.powerups;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import wbs.quake.*;
import wbs.quake.player.QuakePlayer;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;

import java.util.*;

public abstract class PowerUp {
    public static final int DIGITS_TO_ROUND = 2;

    public static PowerUp createPowerUp(ConfigurationSection section, String directory) {
        String typeString = section.getString("type", "POTION");
        PowerUpType type = WbsEnums.getEnumFromString(PowerUpType.class, typeString);

        if (type == null) {
            WbsQuake.getInstance().settings.logError("Invalid power up type: " + typeString, directory + "/type");
            throw new InvalidConfigurationException("Invalid type: " + typeString);
        }

        switch (type) {
            case MULTISHOT:
                return new Multishot(WbsQuake.getInstance(), section, directory);
            case POTION:
                return new PotionPowerUp(WbsQuake.getInstance(), section, directory);
            case RAPID_FIRE:
                return new RapidFire(WbsQuake.getInstance(), section, directory);
            case BOUNCESHOT:
                return new BouncePowerUp(WbsQuake.getInstance(), section, directory);
            case INSTANT_RELOAD:
                return new ReloadPowerUp(WbsQuake.getInstance(), section, directory);
            case RANDOM:
                return new RandomPowerUp(WbsQuake.getInstance(), section, directory);
            case SCATTERSHOT:
                return new Scattershot(WbsQuake.getInstance(), section, directory);
        }

        return null;
    }

    private static final Map<String, PowerUp> powerUps = new HashMap<>();
    public static PowerUp getPowerUp(String id) {
        return powerUps.get(id);
    }

    public static void validateAll() {
        List<String> toRemove = new LinkedList<>();
        for (PowerUp powerUp : powerUps.values()) {
            if (!powerUp.validate()) {
                toRemove.add(powerUp.getId());
            }
        }

        toRemove.forEach(powerUps::remove);
    }

    public static Collection<PowerUp> allPowerUps() {
        return powerUps.values();
    }

    public int getCooldown() {
        return cooldown;
    }

    public enum PowerUpType {
        MULTISHOT, POTION, RAPID_FIRE, BOUNCESHOT, INSTANT_RELOAD, RANDOM, SCATTERSHOT
    }

    protected WbsQuake plugin;
    protected QuakeSettings settings;
    public PowerUp(WbsQuake plugin, ConfigurationSection section, String directory) {
        this.plugin = plugin;
        settings = plugin.settings;

        id = section.getName();

        duration = section.getInt("duration", 60);

        cooldown = section.getInt("cooldown", duration * 3);

        display = section.getString("display", getDefaultDisplay());

        item = WbsEnums.materialFromString(section.getString("item"), getDefaultItem());

        powerUps.put(id, this);
    }

    protected final String id;
    protected final int cooldown;
    protected final int duration;
    protected String display;
    protected Material item;

    protected abstract Material getDefaultItem();
    protected abstract String getDefaultDisplay();

    protected ItemStack getItem() {
        ItemStack itemStack = new ItemStack(item);

        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(item);
        if (meta == null) {
            return itemStack;
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public abstract void apply(QuakePlayer player);
    public abstract void removeFrom(QuakePlayer player);

    public String getId() {
        return id;
    }

    public String getDisplay() {
        return display;
    }
    public int getDuration() {
        return duration;
    }

    /**
     * Verifies that this powerup is valid after all powerups are created
     * @return True if the powerup is valid, false if it should be disabled
     */
    protected boolean validate() {
        return true;
    }
}
