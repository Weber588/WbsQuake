package wbs.quake.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.quake.*;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
import wbs.quake.powerups.PowerUp;
import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.string.WbsStringify;

@SuppressWarnings("unused")
public class QuakeListener extends WbsMessenger implements Listener {
    private final WbsQuake plugin;
    public QuakeListener(WbsQuake plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        QuakePlayer player = PlayerManager.getPlayer(event.getPlayer());
        QuakeLobby.getInstance().leave(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        QuakePlayer quakePlayer = PlayerManager.getPlayer(player);
        quakePlayer.setPlayer(player);
    }

    @EventHandler
    public void onPowerupDespawn(ItemDespawnEvent event) {
        Item itemEntity = event.getEntity();
        ItemMeta meta = itemEntity.getItemStack().getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();

        String powerUpId = container.get(PowerUp.POWER_UP_KEY, PersistentDataType.STRING);
        if (powerUpId != null) {
            event.setCancelled(true);
            itemEntity.setTicksLived(1);
        }
    }

    @EventHandler
    public void onPowerup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        QuakePlayer quakePlayer = PlayerManager.getPlayer(player);

        Item itemEntity = event.getItem();
        ItemMeta meta = itemEntity.getItemStack().getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();

        String powerUpId = container.get(PowerUp.POWER_UP_KEY, PersistentDataType.STRING);
        if (powerUpId != null) {
            event.setCancelled(true);
            itemEntity.remove();

            PowerUp powerUp = plugin.settings.powerUps.get(powerUpId);

            if (powerUp == null) {
                plugin.logger.warning("An invalid powerup item was found at " + WbsStringify.toString(itemEntity.getLocation(), true));
                return;
            }

            if (!powerUp.apply(itemEntity.getLocation(), quakePlayer)) {
                System.out.println("Powerup failed to apply: " + powerUpId);
            }
        }
    }


    @EventHandler
    public void leap(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        if (isUsingGun(event)) {
            Player player = event.getPlayer();
            QuakePlayer quakePlayer = PlayerManager.getPlayer(player);
            Gun gun = quakePlayer.getCurrentGun();

            gun.leap(quakePlayer);

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void triggerShoot(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (isUsingGun(event)) {
            Player player = event.getPlayer();
            QuakePlayer quakePlayer = PlayerManager.getPlayer(player);
            Gun gun = quakePlayer.getCurrentGun();

            gun.fire(quakePlayer);
        }
    }

    private boolean isUsingGun(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) return false;

        QuakePlayer quakePlayer = PlayerManager.getPlayer(player);

        Gun gun = quakePlayer.getCurrentGun();

        if (itemInHand.getType() != gun.getSkin()) return false;

        ItemMeta meta = itemInHand.getItemMeta();
        if (meta == null) return false;

        String gunKeyCheck = meta.getPersistentDataContainer().get(Gun.GUN_KEY, PersistentDataType.STRING);

        return gunKeyCheck != null && gunKeyCheck.equalsIgnoreCase("true");
    }
}
