package wbs.quake.menus;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.quake.cosmetics.menus.CosmeticsMenu;
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

    @SuppressWarnings("unchecked")
    public static <T extends PlayerSpecificMenu> T getMenu(@NotNull QuakePlayer player, Class<T> clazz) {
        if (!managers.containsKey(clazz)) {
            PlayerMenuManager<T> manager = new PlayerMenuManager<>();
            managers.put(clazz, (PlayerMenuManager<PlayerSpecificMenu>) manager);
        }
        return clazz.cast(managers.get(clazz).getMenu(player, (Class<PlayerSpecificMenu>) clazz));
    }

    public static void openMenuFor(QuakePlayer player, Class<? extends PlayerSpecificMenu> clazz) {
        getMenu(player, clazz).showTo(player.getPlayer());
    }

    // Common slots

    public static MenuSlot getBackToShopSlot(QuakePlayer player) {
        MenuSlot backToShopSlot = new MenuSlot(getPlugin(), Material.CLOCK, "&7Back to Shop");

        backToShopSlot.setClickAction(inventoryClickEvent ->
                getMenu(player, ShopMenu.class).showTo(player.getPlayer())
        );

        return backToShopSlot;
    }

    public static MenuSlot getBackToCosmeticsSlot(QuakePlayer player) {
        MenuSlot backToCosmeticsSlot = new MenuSlot(getPlugin(), Material.CLOCK, "&bBack to Cosmetics");

        backToCosmeticsSlot.setClickAction(inventoryClickEvent ->
                getMenu(player, ShopMenu.class).showTo(player.getPlayer())
        );

        return backToCosmeticsSlot;
    }

    private static MenuSlot balSlot;
    public static MenuSlot getBalSlot() {
        if (balSlot != null) return balSlot;

        balSlot = new MenuSlot(getPlugin(), Material.SUNFLOWER,
                "&6&lBalance",
                "&e$%vault_eco_balance_commas%");
        balSlot.setFillPlaceholders(true);

        return balSlot;
    }
}
