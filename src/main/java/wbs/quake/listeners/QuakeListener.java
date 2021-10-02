package wbs.quake.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.quake.Gun;
import wbs.quake.QuakeLobby;
import wbs.quake.WbsQuake;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
import wbs.quake.powerups.ArenaPowerUp;
import wbs.utils.util.plugin.WbsMessenger;

@SuppressWarnings("unused")
public class QuakeListener extends WbsMessenger implements Listener {
    private final WbsQuake plugin;
    public QuakeListener(WbsQuake plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        QuakePlayer player = QuakeLobby.getInstance().getPlayer(event.getPlayer());
        if (player != null)
            QuakeLobby.getInstance().leave(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        QuakePlayer quakePlayer = PlayerManager.getCachedPlayer(player);
        if (quakePlayer != null) {
            quakePlayer.setPlayer(player);
        }
    }

    @EventHandler
    public void onCombust(EntityCombustEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (QuakeLobby.getInstance().isInLobby(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPowerupDespawn(ItemDespawnEvent event) {
        Item itemEntity = event.getEntity();
        ItemMeta meta = itemEntity.getItemStack().getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();

        String powerUpId = container.get(ArenaPowerUp.POWER_UP_KEY, PersistentDataType.STRING);
        if (powerUpId != null) {
            event.setCancelled(true);
            itemEntity.setTicksLived(1);
        }
    }

    @EventHandler
    public void onPowerup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Item itemEntity = event.getItem();

        ArenaPowerUp powerUp = ArenaPowerUp.getArenaPowerUp(itemEntity);

        if (powerUp != null) {
            event.setCancelled(true);

            Player player = (Player) event.getEntity();
            QuakePlayer quakePlayer = QuakeLobby.getInstance().getPlayer(player);

            if (quakePlayer != null) {
                powerUp.run(quakePlayer);
            }
        }
    }

    @EventHandler
    public void preventDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (QuakeLobby.getInstance().isInLobby(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void leap(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        QuakePlayer quakePlayer = QuakeLobby.getInstance().getPlayer(player);
        if (quakePlayer != null) {
            if (isHoldingGun(quakePlayer)) {
                Gun gun = quakePlayer.getCurrentGun();

                gun.leap(quakePlayer);

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void triggerShoot(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        QuakePlayer quakePlayer = QuakeLobby.getInstance().getPlayer(player);
        if (quakePlayer != null) {
            if (isHoldingGun(quakePlayer)) {
                Gun gun = quakePlayer.getCurrentGun();

                gun.fire(quakePlayer);
            }
        }
    }

    private boolean isHoldingGun(QuakePlayer player) {
        ItemStack itemInHand = player.getPlayer().getInventory().getItemInMainHand();

        ItemMeta meta = itemInHand.getItemMeta();
        if (meta == null) return false;

        if (itemInHand.getType() == Material.AIR) return false;

        Gun gun = player.getCurrentGun();

        if (itemInHand.getType() != gun.getSkin()) return false;

        String gunKeyCheck = meta.getPersistentDataContainer().get(Gun.GUN_KEY, PersistentDataType.STRING);

        return gunKeyCheck != null && gunKeyCheck.equalsIgnoreCase("true");
    }
}
