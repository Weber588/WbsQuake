package wbs.quake.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;

public class MenuListener extends WbsMessenger implements Listener {
    public MenuListener(WbsPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        InventoryView view = event.getView();


    }

}
