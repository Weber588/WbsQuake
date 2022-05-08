package wbs.quake.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.quake.ItemManager;
import wbs.quake.WbsQuake;
import wbs.utils.util.plugin.WbsMessenger;

public class ItemsListener extends WbsMessenger implements Listener {
    public ItemsListener(@NotNull WbsQuake plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        ItemStack heldItem = player.getInventory().getItemInMainHand();

        ItemMeta meta = heldItem.getItemMeta();

        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String leaveItemCheck = container.get(ItemManager.LEAVE_ITEM_KEY, PersistentDataType.STRING);

        if (leaveItemCheck != null) {
            player.performCommand("wbsquake leave");
            event.setCancelled(true);
            return;
        }

        String shopItemCheck = container.get(ItemManager.SHOP_ITEM_KEY, PersistentDataType.STRING);
        if (shopItemCheck != null) {
            player.performCommand("wbsquake shop");
            event.setCancelled(true);
        }
    }

}
