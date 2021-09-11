package wbs.quake.menus;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.CosmeticsStore;
import wbs.quake.killperks.KillPerksMenu;
import wbs.quake.player.QuakePlayer;
import wbs.quake.upgrades.UpgradesMenu;
import wbs.utils.util.menus.MenuSlot;

import java.util.Objects;
import java.util.stream.Collectors;

public class ShopMenu extends PlayerSpecificMenu {

    public ShopMenu(WbsQuake plugin, QuakePlayer player) {
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
                "&3gameplay!"
        );
        upgradesMenu.setClickAction(inventoryClickEvent ->
                MenuManager.getMenu(player, UpgradesMenu.class).showTo(player.getPlayer())
        );
        setSlot(2, 2, upgradesMenu);

        MenuSlot cosmeticsMenu;
        if (CosmeticsStore.getInstance().getCosmeticTypesLoaded() > 0) {
            cosmeticsMenu = new MenuSlot(plugin, Material.CAKE, "&5&lCosmetics", "&dExpress yourself!");

            cosmeticsMenu.setClickAction(inventoryClickEvent ->
                    MenuManager.getCosmeticMenu().showTo(player.getPlayer())
            );

        } else {
            cosmeticsMenu = new MenuSlot(plugin, Material.CAKE, "&8&lCosmetics", "&cCosmetics are disabled.");
        }

        setSlot(2, 4, cosmeticsMenu);

        MenuSlot killPerkMenu;
        if (plugin.settings.allKillPerks().size() > 0) {
            killPerkMenu = new MenuSlot(plugin, Material.DIAMOND_SWORD,
                    "&4&lKill Perks",
                    "&cBuy perks for when you",
                    "&cget a kill!"
            );

            killPerkMenu.setClickAction(inventoryClickEvent ->
                    MenuManager.getMenu(player, KillPerksMenu.class).showTo(player.getPlayer())
            );
        } else {
            killPerkMenu = new MenuSlot(plugin, Material.DIAMOND_SWORD, "&8&lKill Perks", "&cKill Perks are disabled.");
        }

        setSlot(2, 6, killPerkMenu);
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
