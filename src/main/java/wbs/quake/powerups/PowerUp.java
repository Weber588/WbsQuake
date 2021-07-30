package wbs.quake.powerups;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import wbs.quake.*;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.utils.util.string.WbsStringify;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class PowerUp {
    public static final NamespacedKey POWER_UP_KEY = new NamespacedKey(WbsQuake.getInstance(), "power_up");

    public static PowerUp createPowerUp(ConfigurationSection section, String directory) {
        PowerUpType type = WbsEnums.getEnumFromString(PowerUpType.class, section.getString("type", "POTION"));

        switch (type) {
            case MULTISHOT:
                return new Multishot(WbsQuake.getInstance(), section, directory);
            case POTION:
                return new PotionPowerUp(WbsQuake.getInstance(), section, directory);
            case RAPID_FIRE:
                return new RapidFire(WbsQuake.getInstance(), section, directory);
            case BOUNCESHOT:
                return new BouncePowerUp(WbsQuake.getInstance(), section, directory);
        }

        return null;
    }

    private static final Map<Integer, PowerUp> powerups = new HashMap<>();

    public static PowerUp getPowerUp(int id) {
        return powerups.get(id);
    }

    public static void addPowerUp(PowerUp powerUp) {
        powerups.put(powerUp.hashCode(), powerUp);
    }

    public static void runOn(QuakePlayer player, int id) {
        powerups.get(id).runOn(player);
    }

    public enum PowerUpType {
        MULTISHOT, POTION, RAPID_FIRE, BOUNCESHOT
    }

    protected WbsQuake plugin;
    protected QuakeSettings settings;
    public PowerUp(WbsQuake plugin, ConfigurationSection section, String directory) {
        this.plugin = plugin;
        settings = plugin.settings;

        id = section.getName();

        WbsConfigReader.requireNotNull(section, "cooldown", settings, directory);
        cooldown = section.getInt("cooldown");

        duration = section.getInt("duration", 60);

        display = section.getString("display", getDefaultDisplay());

        item = WbsEnums.materialFromString(section.getString("item"), getDefaultItem());
    }

    protected final String id;
    protected final int cooldown;
    protected final int duration;
    protected boolean active;
    protected String display;
    protected Material item;

    private Item itemEntity;
    private int respawnRunnableId = -1;

    public void disable() {
        if (itemEntity != null) {
            itemEntity.remove();
            itemEntity = null;
        }
        if (respawnRunnableId != -1) {
            Bukkit.getScheduler().cancelTask(respawnRunnableId);
            respawnRunnableId = -1;
        }
    }

    protected abstract Material getDefaultItem();
    protected abstract String getDefaultDisplay();

    public final boolean apply(Location location, QuakePlayer player) {
        if (!active) return false;

        active = false;
        itemEntity = null;
        runOn(player);

        QuakeLobby.getInstance().sendActionBars("&e" + player.getName() + " used &b" + getDisplay() + "&e!");

        respawnRunnableId = new BukkitRunnable() {
            @Override
            public void run() {
                spawnAt(location);
                respawnRunnableId = -1;
            }
        }.runTaskLater(plugin, cooldown).getTaskId();

        new BukkitRunnable() {
            @Override
            public void run() {
                remove(player);
            }
        }.runTaskLater(plugin, duration);

        return true;
    }

    public final void spawnAt(Location location) {
        ItemStack itemStack = getItem();

        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(POWER_UP_KEY, PersistentDataType.STRING, id);

        itemStack.setItemMeta(meta);

        World world = location.getWorld();
        assert world != null;

        itemEntity = location.getWorld().dropItem(location, itemStack);
        itemEntity.setGravity(false);
        itemEntity.setPickupDelay(0);
        itemEntity.setVelocity(new Vector(0, 0, 0));

        active = true;
    }

    protected ItemStack getItem() {
        ItemStack itemStack = new ItemStack(item);

        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(item);
        if (meta == null) {
            return itemStack;
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    protected abstract void runOn(QuakePlayer player);
    protected void remove(QuakePlayer player) {

    }

    public String getId() {
        return id;
    }

    public String getDisplay() {
        return display;
    }

    public void writeToConfig(ConfigurationSection section, String path, Location location) {
        section.set(path + ".cooldown", cooldown);
        section.set(path + ".location", WbsStringify.toString(location, true));
    }
}
