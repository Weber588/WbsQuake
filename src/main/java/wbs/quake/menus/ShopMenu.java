package wbs.quake.menus;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Objects;
import java.util.stream.Collectors;

public class ShopMenu extends PlayerSpecifiMenu {

    public ShopMenu(WbsPlugin plugin, QuakePlayer player) {
        super(plugin, player, "&9&lShop", 5, "shop:" + player.getName());

        MenuSlot outlineSlot = new MenuSlot(plugin, Material.RED_STAINED_GLASS_PANE, "&r");
        setOutline(outlineSlot);

        ItemStack statsItem = getStatsItem();
        MenuSlot statsSlot = new MenuSlot(plugin, statsItem);
        setSlot(1, 4, statsSlot);

        setSlot(3, 4, MenuManager.getBalSlot());

        MenuSlot upgradesMenu = new MenuSlot(plugin, Material.ANVIL,
                "&9&lUpgrades",
                "&3Buy upgrades that affect",
                "&3gameplay here!"
        );
        upgradesMenu.setClickAction(inventoryClickEvent ->
                MenuManager.getUpgradesMenu(player).showTo(player.getPlayer())
        );
        setSlot(2, 2, upgradesMenu);

        MenuSlot cosmeticsMenu = new MenuSlot(plugin, Material.CAKE, "&5&lCosmetics", "&dExpress yourself here!");
        cosmeticsMenu.setClickAction(inventoryClickEvent ->
            MenuManager.getCosmeticMenu().showTo(player.getPlayer())
        );
        setSlot(2, 6, cosmeticsMenu);
    }

    private ItemStack getStatsItem() {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta meta = Objects.requireNonNull((SkullMeta) playerHead.getItemMeta());
        meta.setOwningPlayer(player.getPlayer());

        meta.setDisplayName("&c&lYour Stats");
        meta.setLore(player.getStatsDisplay().stream().map(line -> "&r" + line).collect(Collectors.toList()));

        playerHead.setItemMeta(meta);

        return playerHead;
    }
}
