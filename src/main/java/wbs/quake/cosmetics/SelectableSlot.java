package wbs.quake.cosmetics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import wbs.quake.WbsQuake;
import wbs.quake.cosmetics.SelectableCosmetic;
import wbs.quake.menus.cosmetics.CosmeticsSubmenu;
import wbs.quake.player.PlayerCosmetics;
import wbs.quake.player.PlayerManager;
import wbs.quake.player.QuakePlayer;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.pluginhooks.PlaceholderAPIWrapper;
import wbs.utils.util.pluginhooks.VaultWrapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SelectableSlot<T extends SelectableCosmetic<T>> extends MenuSlot {

    private final WbsQuake plugin;

    private final T cosmetic;

    public SelectableSlot(CosmeticsSubmenu<T> menu, T cosmetic) {
        super(WbsQuake.getInstance(), cosmetic.material, cosmetic.display, cosmetic.description);

        this.cosmetic = cosmetic;
        plugin = WbsQuake.getInstance();

        setClickAction(event -> onClick(menu, event));
    }

//    @SuppressWarnings("unchecked")
    public void onClick(CosmeticsSubmenu<T> menu, InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!player.hasPermission(cosmetic.permission)) {
            if (VaultWrapper.hasMoney(player, cosmetic.price)) {
                VaultWrapper.takeMoney(player, cosmetic.price);

                plugin.sendMessage("Bought cosmetic for &h" + VaultWrapper.formatMoney(cosmetic.price) + "&r!", player);

                VaultWrapper.givePermission(player, cosmetic.permission);
            } else {
                plugin.sendMessage("Not enough money! Balance: &h"
                        + VaultWrapper.formatMoneyFor(player) + "&r. Cost: &h" + VaultWrapper.formatMoney(cosmetic.price), player);
                return;
            }
        }

        menu.getPlayer().getCosmetics().setCosmetic(cosmetic);
        menu.setCurrent(cosmetic);

        menu.updateSelected(this);
    }

    @Override
    public ItemStack getFormattedItem(@Nullable Player player) {
        ItemStack item = super.getFormattedItem(player);

        if (player != null) {
            ItemMeta meta = Objects.requireNonNull(item.getItemMeta());

            List<String> lore = cosmetic.description.stream().map(line -> "&7" + line).collect(Collectors.toList());

            QuakePlayer quakePlayer = PlayerManager.getPlayer(player);
            PlayerCosmetics cosmetics = quakePlayer.getCosmetics();

            SelectableCosmetic<?> current = cosmetics.getCosmetic(cosmetic.getCosmeticType());
            if (current.equals(cosmetic)) {
                lore.add("&aSelected.");
                meta.addEnchant(Enchantment.LOYALTY, 1, true);
            } else {
                if (quakePlayer.getPlayer().hasPermission(cosmetic.permission)) {
                    lore.add("&6Click to enable!");
                } else {
                    lore.add("&6Cost: &h" + VaultWrapper.formatMoney(cosmetic.price));
                }
            }

            meta.setLore(plugin.colouriseAll(lore));
            meta.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_POTION_EFFECTS,
                    ItemFlag.HIDE_DYE);

            item.setItemMeta(meta);
        }

        return item;
    }
}
