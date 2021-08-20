package wbs.quake.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SelectableSlot<T extends SelectableCosmetic<T>> extends MenuSlot {

    private final WbsQuake plugin;

    private final SelectableCosmetic<T> cosmetic;

    @SuppressWarnings("unchecked")
    public SelectableSlot(CosmeticsSubmenu<T> menu, SelectableCosmetic<T> cosmetic) {
        super(WbsQuake.getInstance(), cosmetic.material, cosmetic.display, cosmetic.description.toArray(new String[0]));

        this.cosmetic = cosmetic;
        plugin = WbsQuake.getInstance();

        setClickAction(event -> {
            menu.getPlayer().getCosmetics().setCosmetic(cosmetic);
            menu.setCurrent((T) cosmetic);

            menu.updateSelected(this);
        });
    }

    @Override
    public ItemStack getFormattedItem(@Nullable Player player) {
        ItemStack item = super.getFormattedItem(player);

        if (player != null) {
            ItemMeta meta = Objects.requireNonNull(item.getItemMeta());

            List<String> lore = cosmetic.description.stream().map(line -> "&7" + line).collect(Collectors.toList());

            QuakePlayer quakePlayer = PlayerManager.getPlayer(player);
            PlayerCosmetics cosmetics = quakePlayer.getCosmetics();
            if (cosmetics.getCosmetic(cosmetic.getCosmeticType()).equals(cosmetic)) {
                lore.add("&aSelected.");
                meta.addEnchant(Enchantment.LOYALTY, 1, true);
            } else {
                if (quakePlayer.getPlayer().hasPermission(cosmetic.permission)) {
                    lore.add("&6Click to enable!");
                } else {
                    lore.add("&6Cost: &h$" + cosmetic.price);
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
