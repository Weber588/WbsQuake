package wbs.quake.powerups;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import wbs.quake.QuakeLobby;
import wbs.quake.WbsQuake;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.string.WbsStringify;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ArenaPowerUp {
    public static final NamespacedKey POWER_UP_KEY = new NamespacedKey(WbsQuake.getInstance(), "power_up");


    private static final Map<Item, ArenaPowerUp> powerUps = new HashMap<>();
    public static ArenaPowerUp getArenaPowerUp(Item itemEntity) {
        return powerUps.get(itemEntity);
    }

    private final WbsQuake plugin;

    private final Location location;
    private final PowerUp powerUp;

    private Item itemEntity;

    private boolean active;
    private int respawnId = -1;
    private int removeId = -1;

    private List<QuakePlayer> lastAffected = new LinkedList<>();

    public ArenaPowerUp(Location location, PowerUp powerUp) {
        this.location = location;
        this.powerUp = powerUp;
        plugin = powerUp.plugin;
    }

    public final void spawn() {
        ItemStack itemStack = powerUp.getItem();

        World world = location.getWorld();
        assert world != null;

        itemEntity = location.getWorld().dropItem(location, itemStack);
        itemEntity.setGravity(false);
        itemEntity.setPickupDelay(0);
        itemEntity.setVelocity(new Vector(0, 0, 0));

        powerUps.put(itemEntity, this);

        active = true;
    }

    public void run(QuakePlayer player) {
        itemEntity.remove();
        lastAffected.add(player);

        QuakeLobby.getInstance().sendActionBars("&e" + player.getName() + " used &b" + powerUp.getDisplay() + "&e!");

        respawnId = new BukkitRunnable() {
            @Override
            public void run() {
                spawn();
                respawnId = -1;
            }
        }.runTaskLater(plugin, powerUp.getCooldown()).getTaskId();

        removeId = new BukkitRunnable() {
            @Override
            public void run() {
                remove(player);
                removeId = -1;
                lastAffected.remove(player);
            }
        }.runTaskLater(plugin, powerUp.getDuration()).getTaskId();

        powerUp.apply(player);
    }

    public void remove(QuakePlayer player) {
        if (removeId != -1 && lastAffected.contains(player)) {
            powerUp.removeFrom(player);
            Bukkit.getScheduler().cancelTask(removeId);
            lastAffected.remove(player);
        }
    }

    public boolean removeAndCancel() {
        if (itemEntity != null) {
            itemEntity.remove();
        }

        if (respawnId != -1) {
            Bukkit.getScheduler().cancelTask(respawnId);
        }

        lastAffected.clear();

        return itemEntity != null || respawnId != -1;
    }

    public String getId() {
        return powerUp.id;
    }
}
