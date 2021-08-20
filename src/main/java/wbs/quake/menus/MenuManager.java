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

    private static final Map<Class<? extends PlayerSpecificMenu>, PlayerMenuManager<PlayerSpecificMenu>> managers = new HashMap<>();

    public static <T extends PlayerSpecificMenu> T getMenu(HumanEntity player, Class<T> clazz) {
        return getMenu(PlayerManager.getPlayer((Player) player), clazz);
    }
    @SuppressWarnings("unchecked")
    public static <T extends PlayerSpecificMenu> T getMenu(QuakePlayer player, Class<T> clazz) {
        if (!managers.containsKey(clazz)) {
            PlayerMenuManager<T> manager = new PlayerMenuManager<>();
            managers.put(clazz, (PlayerMenuManager<PlayerSpecificMenu>) manager);
        }
        return clazz.cast(managers.get(clazz).getMenu(player, (Class<PlayerSpecificMenu>) clazz));
    }



    public static void openMenuFor(HumanEntity player, Class<? extends PlayerSpecificMenu> clazz) {
        openMenuFor(PlayerManager.getPlayer((Player) player), clazz);
    }

    public static void openMenuFor(QuakePlayer player, Class<? extends PlayerSpecificMenu> clazz) {
        getMenu(player, clazz).showTo(player.getPlayer());
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
                    getMenu(player, ShopMenu.class).showTo(player);
                }
        );

        return backToShopSlot;
    }

    private static MenuSlot balSlot;
    public static MenuSlot getBalSlot() {
        if (balSlot != null) return balSlot;

        MenuSlot balSlot = new MenuSlot(getPlugin(), Material.SUNFLOWER,
                "&6&lBalance",
                "&e$%vault_eco_balance_commas%");
        balSlot.setFillPlaceholders(true);

        return balSlot;
    }
}
