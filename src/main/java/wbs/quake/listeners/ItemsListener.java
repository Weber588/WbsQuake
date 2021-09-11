package wbs.quake.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.quake.QuakeLobby;
import wbs.quake.WbsQuake;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.plugin.WbsMessenger;

import java.util.Objects;

public class ItemsListener extends WbsMessenger implements Listener {
    public ItemsListener(@NotNull WbsQuake plugin) {
        super(plugin);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        ItemStack heldItem = player.getInventory().getItemInMainHand();

        ItemMeta meta = heldItem.getItemMeta();

        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String leaveItemCheck = container.get(QuakeLobby.LEAVE_ITEM_KEY, PersistentDataType.STRING);

        if (leaveItemCheck != null) {
            player.performCommand("wbsquake leave");
            return;
        }

        String shopItemCheck = container.get(QuakeLobby.SHOP_ITEM_KEY, PersistentDataType.STRING);
        if (shopItemCheck != null) {
            player.performCommand("wbsquake shop");
        }
    }

}
