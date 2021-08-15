package wbs.quake.menus;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
import wbs.quake.WbsQuake;
import wbs.utils.util.menus.MenuSlot;

import java.util.HashMap;
import java.util.Map;

public final class MenuManager {
    private MenuManager() {}

    private static WbsQuake plugin;
    private static WbsQuake getPlugin() {
        if (plugin == null) {
            plugin = WbsQuake.getInstance();
        }
        return plugin;
    }

    // Menus

    private static final Map<QuakePlayer, ShopMenu> shopMenus = new HashMap<>();
    public static ShopMenu getShopMenu(HumanEntity player) {
        return getShopMenu(PlayerManager.getPlayer((Player) player));
    }
    public static ShopMenu getShopMenu(QuakePlayer player) {
        if (shopMenus.containsKey(player)) return shopMenus.get(player);

        ShopMenu playerShopMenu = new ShopMenu(getPlugin(), player);
        shopMenus.put(player, playerShopMenu);
        return playerShopMenu;
    }

    private static final Map<QuakePlayer, UpgradesMenu> upgradesMenus = new HashMap<>();
    public static UpgradesMenu getUpgradesMenu(HumanEntity player) {
        return getUpgradesMenu(PlayerManager.getPlayer((Player) player));
    }
    public static UpgradesMenu getUpgradesMenu(QuakePlayer player) {
        if (upgradesMenus.containsKey(player)) return upgradesMenus.get(player);

        UpgradesMenu playerUpgradesMenu = new UpgradesMenu(getPlugin(), player);
        upgradesMenus.put(player, playerUpgradesMenu);
        return playerUpgradesMenu;
    }

    private static CosmeticsMenu cosmeticMenu;
    public static CosmeticsMenu getCosmeticMenu() {
        if (cosmeticMenu != null) return cosmeticMenu;

        cosmeticMenu = new CosmeticsMenu(plugin);
        return cosmeticMenu;
    }


    // Common slots

    private static MenuSlot backToShopSlot;
    public static MenuSlot getBackToShopSlot() {
        if (backToShopSlot != null) return backToShopSlot;

        backToShopSlot = new MenuSlot(WbsQuake.getInstance(), Material.CLOCK, "&7Back to Shop");

        backToShopSlot.setClickAction(inventoryClickEvent ->
                {
                    Player player = (Player) inventoryClickEvent.getWhoClicked();
                    getShopMenu(player).showTo(player);
                }
        );

        return backToShopSlot;
    }

    private static MenuSlot balSlot;
    public static MenuSlot getBalSlot() {
        if (balSlot != null) return balSlot;

        MenuSlot balSlot = new MenuSlot(plugin, Material.SUNFLOWER,
                "&6&lBalance",
                "&e$%vault_eco_balance_commas%");
        balSlot.setFillPlaceholders(true);

        return balSlot;
    }
}
